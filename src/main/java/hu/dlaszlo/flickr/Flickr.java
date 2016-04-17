package hu.dlaszlo.flickr;

import hu.dlaszlo.flickr.domain.backup.BackupEntry;
import hu.dlaszlo.flickr.domain.size.Size;
import hu.dlaszlo.flickr.domain.size.SizeResponse;
import hu.dlaszlo.flickr.service.*;
import org.apache.commons.cli.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dlasz on 2016. 02. 27..
 */
public class Flickr
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Flickr.class);

    private static final int TIMEOUT = 30 * 1000;

    private String consumerSecret;
    private String consumerKey;
    private String sourceDir;
    private String targetDir;
    private String database;
    private int updateUrlThreadCount;
    private int downloadThreadCount;
    private int retryCount;
    private int retryDelay;
    private BackupService backupService;
    private FlickrOauth oauth;
    private FlickrApi api;

    public void download()
    {
        try {
            List<BackupEntry> entries = backupService.list();
            MultiThreadRunner<BackupEntry> multiThreadRunner =
                    new MultiThreadRunner<>(
                            "download",
                            downloadThreadCount,
                            entry -> {
                                retry(() -> {
                                    try {
                                        Path downloadFile = Paths.get(targetDir, entry.getPath());
                                        if (!Files.exists(downloadFile)) {
                                            LOGGER.info("Download: {}", downloadFile.toString());
                                            Files.createDirectories(downloadFile.getParent());
                                            RequestConfig requestConfig = RequestConfig.custom()
                                                    .setSocketTimeout(TIMEOUT)
                                                    .setConnectTimeout(TIMEOUT)
                                                    .setConnectionRequestTimeout(TIMEOUT)
                                                    .build();
                                            try (CloseableHttpClient httpClient =
                                                         HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()) {
                                                HttpGet httpGet = new HttpGet(entry.getDownloadUrl());
                                                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                                                    int statusCode = response.getStatusLine().getStatusCode();
                                                    if (statusCode != HttpStatus.SC_OK) {
                                                        throw new RuntimeException(response.getStatusLine().getReasonPhrase());
                                                    } else {
                                                        Path downloadTmp = downloadFile.resolveSibling(
                                                                downloadFile.getFileName() + ".tmp");
                                                        Files.copy(response.getEntity().getContent(), downloadTmp, StandardCopyOption.REPLACE_EXISTING);
                                                        checkMd5(entry, downloadTmp);
                                                        Files.move(downloadTmp, downloadFile);
                                                        Files.setLastModifiedTime(downloadFile, FileTime.fromMillis(entry.getFileTime().getTime()));
                                                    }
                                                }
                                            }
                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return null;
                                });
                            });

            for (BackupEntry entry : entries) {
                if (StringUtils.isNotBlank(entry.getDownloadUrl())) {
                    multiThreadRunner.submit(entry);
                }
            }
            multiThreadRunner.shutdown();

        } catch (RuntimeException e) {
            LOGGER.error("Error occured", e);
            throw e;
        }
    }

    public void downloadUrls()
    {
        try {
            List<BackupEntry> entries = backupService.list();

            MultiThreadRunner<BackupEntry> multiThreadRunner =
                    new MultiThreadRunner<>(
                            "download_url",
                            updateUrlThreadCount,
                            entry -> {
                                SizeResponse sizes =
                                        retry(() -> api.getSizes(entry.getPhotoId()));
                                String downloadUrl = null;
                                for (Size size : sizes.getSizes().getSize()) {
                                    if ("Video Original".equals(size.getLabel())) {
                                        downloadUrl = size.getSource();
                                        break;
                                    } else if (downloadUrl == null && "Original".equals(size.getLabel())) {
                                        downloadUrl = size.getSource();
                                    }
                                }
                                if (StringUtils.isNotBlank(downloadUrl)) {
                                    entry.setDownloadUrl(downloadUrl);
                                    backupService.update(entry);
                                } else {
                                    LOGGER.error("An error occurred while acquiring the download URL. Photo ID: {}", entry.getPhotoId());
                                }
                                LOGGER.info(entry.toString());
                            });

            for (BackupEntry entry : entries) {
                if (StringUtils.isBlank(entry.getDownloadUrl())) {
                    multiThreadRunner.submit(entry);
                }
            }
            multiThreadRunner.shutdown();

        } catch (RuntimeException e) {
            LOGGER.error("Error: ", e);
            throw e;
        }
    }

    public void upload()
    {
        try {
            List<BackupEntry> entries = backupService.list();
            Set<String> processed = new HashSet<>();
            for (BackupEntry entry : entries) {
                LOGGER.debug(entry.toString());
                processed.add(entry.getPath());
            }

            final Path storagePath = Paths.get(sourceDir);

            final AtomicInteger cnt = new AtomicInteger(0);

            Files.walkFileTree(storagePath, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path source, BasicFileAttributes attrs) throws IOException
                {
                    if (attrs.isRegularFile()) {
                        String mimeType = Files.probeContentType(source);
                        if (mimeType != null && (mimeType.startsWith("image") || mimeType.startsWith("video"))) {
                            Path relative = storagePath.relativize(source);
                            String title = relative.toString().replace('\\', '/');
                            if (!processed.contains(title)) {
                                String tag = "upload" + cnt.getAndIncrement() + "x" + System.currentTimeMillis();
                                String md5;
                                try (InputStream is = Files.newInputStream(source)) {
                                    md5 = DigestUtils.md5Hex(is);
                                }
                                String photoId =
                                        retry(() -> api.upload(source.toString(), title, tag, md5));
                                Validate.notNull(photoId);
                                Date fileTime = new Date(attrs.creationTime().toMillis());
                                Date backupTime = new Date();
                                BackupEntry backupEntry = new BackupEntry(photoId, title, tag, mimeType, fileTime, md5, null, backupTime);
                                backupService.save(backupEntry);
                                processed.add(title);
                                entries.add(backupEntry);
                                LOGGER.info(backupEntry.toString());
                            }
                        } else {
                            LOGGER.warn("Not supported mime type: {}, file: {}", mimeType, source.toString());
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            LOGGER.error("Hiba: ", e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadProperties()
    {
        try {
            String fileName = System.getProperty("flickr.configurationFile");
            if (StringUtils.isBlank(fileName)) {
                throw new IllegalArgumentException("A konfigur�ci�s f�jl megad�sa k�telez�: -Dflickr.configurationFile");
            }
            Path settingsPath = Paths.get(fileName);
            if (Files.notExists(settingsPath)) {
                throw new IllegalArgumentException("The configuration file does not exist: " + fileName);
            }

            Properties p = new Properties();
            try (InputStream is = Files.newInputStream(settingsPath)) {
                p.load(is);
            }

            consumerSecret = p.getProperty("consumer.secret");
            consumerKey = p.getProperty("consumer.key");
            sourceDir = p.getProperty("source.dir");
            targetDir = p.getProperty("target.dir");
            database = p.getProperty("database");
            retryCount = Integer.parseInt(p.getProperty("retry.count"));
            retryDelay = Integer.parseInt(p.getProperty("retry.delay"));
            updateUrlThreadCount = Integer.parseInt(p.getProperty("update.url.thread.count"));
            downloadThreadCount = Integer.parseInt(p.getProperty("download.url.thread.count"));

        } catch (Exception e) {
            LOGGER.error("Error occured: ", e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private void init()
    {
        backupService = BackupService.getInstance();
        backupService.init(database);

        oauth = new FlickrOauth(consumerSecret, consumerKey);
        try {
            oauth.auth();
        } catch (FlickrApiException e) {
            throw new RuntimeException(e);
        }
        api = new FlickrApi(oauth);
    }

    private <T> T retry(FlickrApiCall<T> supplier)
    {
        T ret;
        int r = 0;
        while (true) {
            try {
                ret = supplier.call();
                break;
            } catch (Throwable t) {
                r++;
                if (r >= retryCount) {
                    if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    } else {
                        throw new RuntimeException(t);
                    }
                }
                int d = r * retryDelay;
                LOGGER.error("Error occured, delay: " + d + "ms;", t);
                try {
                    Thread.sleep(d);
                } catch (InterruptedException e) {
                    //
                }
            }
        }
        return ret;
    }

    private void checkMd5(BackupEntry entry, Path path) throws IOException
    {
        String md5;
        try (InputStream is = Files.newInputStream(path)) {
            md5 = DigestUtils.md5Hex(is);
        }
        if (!StringUtils.equals(md5, entry.getMd5())) {
            throw new RuntimeException(
                    "MD5 mismatch: " + entry.getPath() + "; "
                            + entry.getMd5() + "; " + md5);
        }
    }

    public Flickr()
    {
        loadProperties();
        init();
    }

    public static void main(String[] args)
    {
        Flickr flickr = new Flickr();

        Options options = new Options();

        options.addOption("b", false, "Backup");
        options.addOption("u", false, "Update download URLs");
        options.addOption("r", false, "Restore");

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("b")) {
                flickr.upload();
            } else if (cmd.hasOption("u")) {
                flickr.downloadUrls();
            } else if (cmd.hasOption("r")) {
                flickr.download();
            } else {
                throw new ParseException("Missing parameters");
            }
        } catch (ParseException e) {
            LOGGER.error("Error occured", e);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Flickr", options);
        }
    }


}
