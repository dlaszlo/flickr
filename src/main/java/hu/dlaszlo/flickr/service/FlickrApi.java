package hu.dlaszlo.flickr.service;

import com.google.gson.Gson;
import hu.dlaszlo.flickr.domain.search.SearchResponse;
import hu.dlaszlo.flickr.domain.size.SizeResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dlasz on 2016. 02. 27..
 */
public class FlickrApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlickrApi.class);

    private static final String FLICKR_API = "https://api.flickr.com";

    private static final int TIMEOUT = 30 * 1000;

    private FlickrOauth oauth;

    AtomicInteger cnt = new AtomicInteger(0);

    public FlickrApi(FlickrOauth oauth) {
        this.oauth = oauth;
    }

    /**
     * Create new Flickr URI builder
     *
     * @param path Service path
     * @return URI builder
     */
    public static URIBuilder newUriBuilder(String path) {
        URIBuilder builder;
        try {
            builder = new URIBuilder(FLICKR_API).setPath(path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return builder;
    }

    /**
     * Returns the available sizes for a photo
     *
     * @param photoId Photos
     * @return The available sizes for a photo
     * @throws FlickrApiException
     */
    public SizeResponse getSizes(String photoId) throws FlickrApiException {
        LOGGER.info("getSizes: {}", photoId);
        URIBuilder builder = newUriBuilder("/services/rest")
                .addParameter("nojsoncallback", "1")
                .addParameter("format", "json")
                .addParameter("method", "flickr.photos.getSizes")
                .setParameter("photo_id", photoId);

        oauth.addOauthParams("GET", builder);
        URI uri = oauth.getURI(builder);

        String responseString;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new FlickrApiException(responseString);
                }
            }
        } catch (IOException e) {
            throw new FlickrApiException("Hiba", e);
        }

        Gson gson = new Gson();
        SizeResponse sizeResponse = gson.fromJson(responseString, SizeResponse.class);

        if (!"ok".equals(sizeResponse.getStat())) {
            throw new FlickrApiException(responseString);
        }
        return sizeResponse;
    }

    /**
     * Return a list of photos.
     *
     * @param tags A comma-delimited list of tags.
     * @return Photos with one or more of the tags listed will be returned.
     * @throws FlickrApiException
     */
    public SearchResponse searchByTags(String tags) throws FlickrApiException {

        LOGGER.info("Search: {}", tags);
        URIBuilder builder = newUriBuilder("/services/rest")
                .addParameter("nojsoncallback", "1")
                .addParameter("format", "json")
                .addParameter("method", "flickr.photos.search")
                .addParameter("user_id", "me")
                .addParameter("tags", tags)
                .addParameter("extras", "url_o");
        oauth.addOauthParams("GET", builder);
        URI uri = oauth.getURI(builder);

        String responseString;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new FlickrApiException(responseString);
                }
            }
        } catch (IOException e) {
            throw new FlickrApiException("Hiba", e);
        }

        Gson gson = new Gson();
        SearchResponse searchResponse = gson.fromJson(responseString, SearchResponse.class);

        if (!"ok".equals(searchResponse.getStat())) {
            throw new FlickrApiException(responseString);
        }

        return searchResponse;
    }

    /**
     * Uploading a photo
     *
     * @param fileName File name
     * @param title Title
     * @param tag Unique tag
     * @param md5 MD5
     * @return Photo ID
     * @throws FlickrApiException
     */
    public String upload(String fileName, String title, String tag, String md5) throws FlickrApiException {

        Map<String, String> result = new HashMap<>();
        LOGGER.info("Upload: {}", fileName);

        URIBuilder builder = newUriBuilder("/services/upload")
                .addParameter("tags", tag)
                .addParameter("title", title)
                .addParameter("description", "MD5: " + md5)
                .addParameter("is_public", "0")
                .addParameter("is_friend", "0")
                .addParameter("is_family", "0")
                .addParameter("safety_level", "1")
                .addParameter("hidden", "2");

        oauth.addOauthParams("POST", builder);

        File f = new File(fileName);

        FileBody fileBody = new FileBody(f, ContentType.APPLICATION_OCTET_STREAM, title);
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("tags", new StringBody(tag, ContentType.create("text/plain", "UTF-8")))
                .addPart("title", new StringBody(title, ContentType.create("text/plain", "UTF-8")))
                .addPart("description", new StringBody("MD5: " + md5, ContentType.create("text/plain", "UTF-8")))
                .addPart("is_public", new StringBody("0", ContentType.create("text/plain", "UTF-8")))
                .addPart("is_friend", new StringBody("0", ContentType.create("text/plain", "UTF-8")))
                .addPart("is_family", new StringBody("0", ContentType.create("text/plain", "UTF-8")))
                .addPart("safety_level", new StringBody("1", ContentType.create("text/plain", "UTF-8")))
                .addPart("hidden", new StringBody("2", ContentType.create("text/plain", "UTF-8")))
                .addPart("photo", fileBody)
                .build();

        String photoId = null;
        try {
            try {
                RequestConfig requestConfig = RequestConfig.custom()
                        .setSocketTimeout(TIMEOUT)
                        .setConnectTimeout(TIMEOUT)
                        .setConnectionRequestTimeout(TIMEOUT)
                        .build();
                try (CloseableHttpClient httpClient =
                             HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()) {
                    HttpPost httpPost = new HttpPost(oauth.getURI(builder));
                    httpPost.setConfig(requestConfig);
                    httpPost.addHeader("Authorization", oauth.getAuthorizationHeader(builder));
                    httpPost.setEntity(reqEntity);
                    try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK ||
                                !StringUtils.contains(responseString, "<rsp stat=\"ok\">")) {
                            throw new FlickrApiException(responseString);
                        } else {
                            photoId = StringUtils.substringBetween(responseString, "<photoid>", "</photoid>");
                        }
                    }
                }
            } catch (SocketTimeoutException e) {
                LOGGER.warn("Timeout", e.getMessage());
            }

            // After a socket timeout, try to acquire photo id by tag
            if (photoId == null) {
                SearchResponse searchResponse = null;
                int i = 0;
                // Wait to max half an hour for photo id
                while (i < 600 && (searchResponse == null || searchResponse.getPhotos().getTotal() == 0)) {
                    searchResponse = searchByTags(tag);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1) {
                        //
                    }
                    i++;
                }
                Validate.isTrue(searchResponse.getPhotos().getPhoto().size() == 1);
                photoId = searchResponse.getPhotos().getPhoto().get(0).getId();
            }

            LOGGER.info("Photo ID: {}", photoId);

            return photoId;

        } catch (IOException e) {
            throw new FlickrApiException("Error occured", e);
        }
    }
}
