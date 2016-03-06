package hu.dlaszlo.flickr.fix;

import hu.dlaszlo.flickr.service.BackupService;
import hu.dlaszlo.flickr.domain.backup.BackupEntry;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Javító program: Az MD5 mező feltöltése az Sqlite adatbázisban
 * (hiba miatt a mime típus írodott bele)
 */
public class FixMd5 {

    private static final Logger LOGGER = LoggerFactory.getLogger(FixMd5.class);

    private String sourceDir = "d:/flickr";
    private String database = "d:/flickr_db/flickr_backup.db";
    private BackupService backupService;

    public void process()
    {
        try {
            List<BackupEntry> entries = backupService.list();
            Map<String, BackupEntry> processed = new HashMap<>();
            for (BackupEntry entry : entries)
            {
                processed.put(entry.getPath(), entry);
            }

            final Path storagePath = Paths.get(sourceDir);
            Files.walkFileTree(storagePath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path source, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isRegularFile()) {
                        String mimeType = Files.probeContentType(source);
                        if (mimeType != null && (mimeType.startsWith("image") || mimeType.startsWith("video"))) {
                            Path relative = storagePath.relativize(source);
                            String title = relative.toString().replace('\\', '/');
                            BackupEntry backupEntry = processed.get(title);
                            if (backupEntry != null)
                            {
                                String md5;
                                try (InputStream is = Files.newInputStream(source)) {
                                    md5 = DigestUtils.md5Hex(is);
                                }
                                backupEntry.setMd5(md5);
                                backupService.update(backupEntry);
                                LOGGER.info(backupEntry.toString());
                            }
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

    public FixMd5() {
        backupService = backupService.getInstance();
        backupService.init(database);
    }

    public static void main(String[] args) {
        if (args.length == 1 && "fixmd5".equals(args[0])) {
            FixMd5 fixMd5 = new FixMd5();
            fixMd5.process();
        }
    }

}
