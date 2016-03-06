package hu.dlaszlo.flickr.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static hu.dlaszlo.flickr.service.FlickrApi.newUriBuilder;

/**
 * Created by dlasz on 2016. 02. 27..
 */
public class FlickrOauth {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlickrOauth.class);

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private String consumerSecret;
    private String consumerKey;
    private boolean callbackConfirmed = false;
    private boolean authorized = false;
    private String token = null;
    private String tokenSecret = "";
    private String verifier = null;
    private String accessToken = null;
    private String accessTokenSecret = null;
    private String fullname;
    private String username;
    private String userNsid;

    public FlickrOauth(String consumerSecret, String consumerKey) {
        this.consumerSecret = consumerSecret;
        this.consumerKey = consumerKey;
    }

    private void loadProperties() throws IOException {
        Properties p = new Properties();
        String home = System.getProperty("user.home");
        File f = new File(home, "flickr.properties");
        if (f.exists() && f.isFile()) {
            try (InputStream is = new FileInputStream(f)) {
                p.load(is);
            }
        }
        accessToken = p.getProperty("accessToken");
        accessTokenSecret = p.getProperty("accessTokenSecret");
        fullname = p.getProperty("fullname");
        username = p.getProperty("username");
        userNsid = p.getProperty("userNsid");
    }

    private void storeProperies() throws IOException {
        Properties p = new Properties();
        p.setProperty("accessToken", accessToken);
        p.setProperty("accessTokenSecret", accessTokenSecret);
        p.setProperty("fullname", fullname);
        p.setProperty("username", username);
        p.setProperty("userNsid", userNsid);

        String home = System.getProperty("user.home");
        File f = new File(home, "flickr.properties");
        if (f.exists()) {
            throw new IllegalArgumentException("File " + f + " already exists.");
        }
        try (OutputStream os = new FileOutputStream(f)) {
            p.store(os, "flickr");
        }
    }

    private String oauthUrlReplace(String url) {
        String encodedUrl = StringUtils.replace(url, "+", "%20");
        encodedUrl = StringUtils.replace(encodedUrl, "*", "%2A");
        encodedUrl = StringUtils.replace(encodedUrl, "%7E", "~");
        return encodedUrl;
    }

    private void signature(String method, URIBuilder builder) throws IOException {

        List<NameValuePair> queryParams = builder.getQueryParams();
        queryParams.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        builder.clearParameters();

        try {
            String url = builder.build().toString();
            String base = method +
                    "&" + oauthUrlReplace(URLEncoder.encode(url, "UTF-8")) +
                    "&" + URLEncoder.encode(oauthUrlReplace(URLEncodedUtils.format(queryParams, "UTF-8")), "UTF-8");
            LOGGER.debug("SBS: {}", base);
            String key = consumerSecret + "&" + (accessTokenSecret == null ? tokenSecret : accessTokenSecret);
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(base.getBytes("UTF-8"));
            String signature = Base64.encodeBase64String(rawHmac);
            builder.addParameters(queryParams);
            builder.addParameter("oauth_signature", signature);
        } catch (GeneralSecurityException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTimestamp() {
        long timestamp = System.currentTimeMillis() / 1000L;
        return String.valueOf(timestamp);
    }

    private String getNonce() {
        int nonce;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            nonce = Math.abs(sr.nextInt());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return String.valueOf(nonce);
    }

    private void acquireRequestToken() throws IOException {

        URIBuilder builder = newUriBuilder("/services/oauth/request_token")
                .addParameter("oauth_nonce", getNonce())
                .addParameter("oauth_timestamp", getTimestamp())
                .addParameter("oauth_consumer_key", consumerKey)
                .addParameter("oauth_signature_method", "HMAC-SHA1")
                .addParameter("oauth_callback", "http://localhost:8765")
                .addParameter("oauth_version", "1.0");
        signature("GET", builder);

        String responseString;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(getURI(builder));
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new IllegalArgumentException(responseString);
                }
            }
        }

        String[] params = StringUtils.split(responseString, "&");
        for (String p : params) {
            String[] param = StringUtils.split(p, "=");
            if (param.length == 2) {
                switch (param[0]) {
                    case "oauth_callback_confirmed":
                        callbackConfirmed = Boolean.valueOf(param[1]);
                        break;
                    case "oauth_token":
                        token = param[1];
                        break;
                    case "oauth_token_secret":
                        tokenSecret = param[1];
                        break;
                    default:
                        throw new IllegalArgumentException(param[0]);
                }
            }
        }
    }

    private void authorize() throws IOException {
        URIBuilder builder = newUriBuilder("/services/oauth/authorize")
                .addParameter("oauth_token", token)
                .addParameter("perms", "write");

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Server server = new Server(8765);
        server.setHandler(
                new AbstractHandler() {
                    @Override
                    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                        String oauthToken = request.getParameter("oauth_token");
                        String oauthVerifier = request.getParameter("oauth_verifier");
                        if (authorized
                                || !StringUtils.equals(oauthToken, token)
                                || StringUtils.isBlank(oauthVerifier)) {
                            response.setContentType("text/html;charset=utf-8");
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            baseRequest.setHandled(true);
                            response.getWriter().println("<h1>Error</h1>");
                            baseRequest.setHandled(true);
                        } else {
                            response.setContentType("text/html;charset=utf-8");
                            response.setStatus(HttpServletResponse.SC_OK);
                            baseRequest.setHandled(true);
                            response.getWriter().println("<h1>Thank you!</h1>");
                            verifier = oauthVerifier;
                        }
                        countDownLatch.countDown();
                    }
                }
        );
        try {
            server.start();
            Thread.sleep(1000);
            URI authUri = builder.build();
            LOGGER.info("Authorization URL: {}", authUri);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(authUri);
            }
            countDownLatch.await();
            Thread.sleep(1000);
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void acquireAccessToken() throws IOException {

        URIBuilder builder = newUriBuilder("/services/oauth/access_token")
                .addParameter("oauth_nonce", getNonce())
                .addParameter("oauth_timestamp", getTimestamp())
                .addParameter("oauth_verifier", verifier)
                .addParameter("oauth_consumer_key", consumerKey)
                .addParameter("oauth_signature_method", "HMAC-SHA1")
                .addParameter("oauth_version", "1.0")
                .addParameter("oauth_token", token);
        signature("GET", builder);
        URI uri = getURI(builder);

        String responseString;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new IllegalArgumentException(responseString);
                }
            }
        }

        String[] params = StringUtils.split(responseString, "&");
        for (String p : params) {
            String[] param = StringUtils.split(p, "=");
            if (param.length == 2) {
                switch (param[0]) {
                    case "fullname":
                        fullname = param[1];
                        break;
                    case "oauth_token":
                        accessToken = param[1];
                        break;
                    case "oauth_token_secret":
                        accessTokenSecret = param[1];
                        break;
                    case "user_nsid":
                        userNsid = param[1];
                        break;
                    case "username":
                        username = param[1];
                        break;
                    default:
                        throw new IllegalArgumentException(param[0]);
                }
            }
        }

    }

    public void auth() throws FlickrApiException {
        if (StringUtils.isNotBlank(accessToken)) {
            throw new IllegalStateException();
        }
        try {
            loadProperties();
            if (StringUtils.isBlank(accessToken)) {
                acquireRequestToken();
                authorize();
                acquireAccessToken();
                storeProperies();
            }
        } catch (IOException e) {
            throw new FlickrApiException("Hiba", e);
        }
    }

    public void addOauthParams(String method, URIBuilder builder) throws FlickrApiException {
        builder.addParameter("oauth_nonce", getNonce())
                .addParameter("oauth_consumer_key", consumerKey)
                .addParameter("oauth_timestamp", getTimestamp())
                .addParameter("oauth_signature_method", "HMAC-SHA1")
                .addParameter("oauth_version", "1.0")
                .addParameter("oauth_token", accessToken);
        try {
            signature(method, builder);
        } catch (IOException e) {
            throw new FlickrApiException("Error occured", e);
        }
    }

    public String getAuthorizationHeader(URIBuilder builder) {
        StringBuilder authorization = new StringBuilder(" OAuth ");
        boolean sep = false;
        for (NameValuePair param : builder.getQueryParams()) {
            if (param.getName().startsWith("oauth_")) {
                if (sep) {
                    authorization.append(", ");
                }
                authorization.append(param.getName()).append("=\"").append(param.getValue()).append("\"");
                sep = true;
            }
        }
        return authorization.toString();
    }

    public URI getURI(URIBuilder builder) {
        URI uri;
        try {
            uri = builder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return uri;
    }

    public String getUserNsid() {
        return userNsid;
    }

    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }
}
