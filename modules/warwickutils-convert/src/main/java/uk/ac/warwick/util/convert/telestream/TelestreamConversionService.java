package uk.ac.warwick.util.convert.telestream;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.google.common.io.ByteSource;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import uk.ac.warwick.util.convert.ConversionException;
import uk.ac.warwick.util.convert.ConversionMedia;
import uk.ac.warwick.util.convert.ConversionService;
import uk.ac.warwick.util.convert.ConversionStatus;
import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.web.Uri;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ProxySelector;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TelestreamConversionService implements ConversionService, InitializingBean, DisposableBean {

    private static final String API_URL = "api.pandastream.com";

    private static final String API_VERSION = "v3.0";

    private static final DateTimeFormatter ISO8601_STRICT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

    private CloseableHttpClient httpClient;

    private final String accessKey;

    private final String accessSecret;

    private final String factoryId;

    private final AmazonS3 s3;

    private final String bucketName;

    public TelestreamConversionService(String tsAccessKey, String tsAccessSecret, String tsFactoryId, String awsAccessKey, String awsSecretKey, String awsBucketName) {
        this.accessKey = tsAccessKey;
        this.accessSecret = tsAccessSecret;
        this.factoryId = tsFactoryId;

        this.s3 = new AmazonS3Client(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
        this.bucketName = awsBucketName;
    }

    @Override
    public ConversionMedia upload(ByteSource source) throws IOException {
        return postMultipart("/videos.json", "file", source, new HashMap<String, String>() {{
            put("profiles", "none");
            put("path_format", ":id");
        }}, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));

                return TelestreamConversionMedia.fromJSON(json);
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Telestream", e);
            }
        });
    }

    @Override
    public ConversionMedia getMediaById(String id) throws IOException {
        return get("/videos/" + id + ".json", response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));

                return TelestreamConversionMedia.fromJSON(json);
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Telestream", e);
            }
        });
    }

    @Override
    public ConversionStatus convert(ConversionMedia media, Format format) throws IOException {
        return post("/encodings.json", new HashMap<String, String>() {{
            put("video_id", media.getId());
            put("profile_name", format.getProfileName());
            put("screenshots", "true");
        }}, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));

                return TelestreamConversionStatus.fromJSON(json);
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Telestream", e);
            }
        });
    }

    @Override
    public ConversionStatus getStatus(String id) throws IOException {
        return get("/encodings/" + id + ".json", new HashMap<String, String>() {{
            put("screenshots", "true");
        }}, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));

                return TelestreamConversionStatus.fromJSON(json);
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Telestream", e);
            }
        });
    }

    @Override
    public void delete(ConversionMedia media) throws IOException {
        delete("/videos/" + media.getId() + ".json", response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            EntityUtils.consumeQuietly(response.getEntity());
            return null;
        });
    }

    private Uri generateS3PrivateUrl(String objectKey) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey);
        request.setMethod(HttpMethod.GET);
        request.setExpiration(DateTime.now().plusHours(1).toDate());

        return Uri.fromJavaUrl(s3.generatePresignedUrl(request));
    }

    @Override
    public Uri getEncodedFileUrl(ConversionStatus status) throws IOException {
        if (status.getStatus() != ConversionStatus.Status.success || status.getFiles().isEmpty()) {
            throw new ConversionException("Can only get encoded file once encoding is successful");
        }

        return generateS3PrivateUrl(status.getFiles().iterator().next());
    }

    @Override
    public Uri getScreenshotUrl(ConversionStatus status) throws IOException {
        if (status.getStatus() != ConversionStatus.Status.success || status.getScreenshots().isEmpty()) {
            throw new ConversionException("Conversion not successful or no screenshots generated");
        }

        return generateS3PrivateUrl(status.getScreenshots().iterator().next());
    }

    private void handleS3Object(String objectKey, Consumer<InputStream> consumer) throws IOException {
        ByteSource source = getS3Object(objectKey);
        try (InputStream is = source.openBufferedStream()) {
            consumer.accept(is);
        }
    }

    private ByteSource getS3Object(String objectKey) {
        final S3Object object = s3.getObject(new GetObjectRequest(bucketName, objectKey));
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return object.getObjectContent();
            }

            @Override
            public boolean isEmpty() throws IOException {
                return object == null;
            }

            @Override
            public long size() throws IOException {
                return object.getObjectMetadata().getContentLength();
            }
        };
    }

    @Override
    public void processEncodedFile(ConversionStatus status, Consumer<InputStream> consumer) throws IOException {
        if (status.getStatus() != ConversionStatus.Status.success || status.getFiles().isEmpty()) {
            throw new ConversionException("Can only get encoded file once encoding is successful");
        }

        handleS3Object(status.getFiles().iterator().next(), consumer);
    }

    @Override
    public void processScreenshot(ConversionStatus status, Consumer<InputStream> consumer) throws IOException {
        if (status.getStatus() != ConversionStatus.Status.success || status.getScreenshots().isEmpty()) {
            throw new ConversionException("Conversion not successful or no screenshots generated");
        }

        handleS3Object(status.getScreenshots().iterator().next(), consumer);
    }

    @Override
    public ByteSource getEncodedFile(ConversionStatus status) throws IOException {
        if (status.getStatus() != ConversionStatus.Status.success || status.getFiles().isEmpty()) {
            throw new ConversionException("Can only get encoded file once encoding is successful");
        }

        return getS3Object(status.getFiles().iterator().next());
    }

    @Override
    public ByteSource getScreenshot(ConversionStatus status) throws IOException {
        if (status.getStatus() != ConversionStatus.Status.success || status.getScreenshots().isEmpty()) {
            throw new ConversionException("Conversion not successful or no screenshots generated");
        }

        return getS3Object(status.getScreenshots().iterator().next());
    }

    private <T> T get(String url, ResponseHandler<T> handler) throws IOException {
        return get(url, Collections.emptyMap(), handler);
    }

    private <T> T get(String url, Map<String, String> params, ResponseHandler<T> handler) throws IOException {
        Map<String, String> allParams = new HashMap<>();
        allParams.putAll(params);
        allParams.put("access_key", accessKey);
        allParams.put("factory_id", factoryId);
        allParams.put("timestamp", DateTime.now().withZone(DateTimeZone.UTC).toString(ISO8601_STRICT));
        String canonicalQueryString = getCanonicalQueryString(allParams);


        String signingString = StringUtils.join(Arrays.asList(
            "GET",
            API_URL,
            url,
            canonicalQueryString
        ), "\n");

        String signature = sign(accessSecret, signingString);

        HttpGet get = new HttpGet("https://" + API_URL + "/" + API_VERSION + url + "?" + canonicalQueryString + "&signature=" + signature.replace("+", "%2B"));
        return httpClient.execute(get, handler);
    }

    private String getCanonicalQueryString(Map<String, String> allParams) {// Get the canonical querystring
        List<String> queryParams = allParams.entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .map(entry -> {
                try {
                    return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8").replace("+", "%2B");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException(e);
                }
            })
            .collect(Collectors.toList());

        return StringUtils.join(queryParams, "&");
    }

    private <T> T post(String url, Map<String, String> params, ResponseHandler<T> handler) throws IOException {
        Map<String, String> allParams = new HashMap<>();
        allParams.putAll(params);
        allParams.put("access_key", accessKey);
        allParams.put("factory_id", factoryId);
        allParams.put("timestamp", DateTime.now().withZone(DateTimeZone.UTC).toString(ISO8601_STRICT));

        // Get the canonical querystring
        String canonicalQueryString = getCanonicalQueryString(allParams);

        String signingString = StringUtils.join(Arrays.asList(
            "POST",
            API_URL,
            url,
            canonicalQueryString
        ), "\n");

        String signature = sign(accessSecret, signingString);

        HttpPost post = new HttpPost("https://" + API_URL + "/" + API_VERSION + url + "?" + canonicalQueryString + "&signature=" + signature.replace("+", "%2B"));
        return httpClient.execute(post, handler);
    }

    private <T> T postMultipart(String url, String fileParam, ByteSource source, Map<String, String> params, ResponseHandler<T> handler) throws IOException {
        Map<String, String> allParams = new HashMap<>();
        allParams.putAll(params);
        allParams.put("access_key", accessKey);
        allParams.put("factory_id", factoryId);
        allParams.put("timestamp", DateTime.now().withZone(DateTimeZone.UTC).toString(ISO8601_STRICT));

        // Get the canonical querystring
        String canonicalQueryString = getCanonicalQueryString(allParams);

        String signingString = StringUtils.join(Arrays.asList(
            "POST",
            API_URL,
            url,
            canonicalQueryString
        ), "\n");

        String signature = sign(accessSecret, signingString);

        HttpPost post = new HttpPost("https://" + API_URL + "/" + API_VERSION + url);

        MultipartEntityBuilder entity = MultipartEntityBuilder.create();

        entity.addBinaryBody(fileParam, source.openStream());
        allParams.forEach(entity::addTextBody);
        entity.addTextBody("signature", signature.replace("+", "%2B"));

        post.setEntity(entity.build());

        return httpClient.execute(post, handler);
    }

    private <T> T delete(String url, ResponseHandler<T> handler) throws IOException {
        Map<String, String> allParams = new HashMap<>();
        allParams.put("access_key", accessKey);
        allParams.put("factory_id", factoryId);
        allParams.put("timestamp", DateTime.now().withZone(DateTimeZone.UTC).toString(ISO8601_STRICT));

        // Get the canonical querystring
        String canonicalQueryString = getCanonicalQueryString(allParams);

        String signingString = StringUtils.join(Arrays.asList(
            "DELETE",
            API_URL,
            url,
            canonicalQueryString
        ), "\n");

        String signature = sign(accessSecret, signingString);

        HttpDelete delete = new HttpDelete("https://" + API_URL + "/" + API_VERSION + url + "?" + canonicalQueryString + "&signature=" + signature.replace("+", "%2B"));
        return httpClient.execute(delete, handler);
    }

    private static String sign(String key, String data) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256HMAC.init(secretKey);

            return com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(sha256HMAC.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        httpClient.close();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        httpClient = HttpClientBuilder.create()
            .setDefaultConnectionConfig(
                ConnectionConfig.custom()
                    .setBufferSize(8192)
                    .setCharset(Charset.forName("UTF-8"))
                    .build()
            )
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectTimeout(30000) // 30 seconds
                    .setSocketTimeout(30000) // 30 seconds
                    .setExpectContinueEnabled(true)
                    .setCircularRedirectsAllowed(true)
                    .setRedirectsEnabled(true)
                    .setMaxRedirects(10)
                    .build()
            )
            .setDefaultSocketConfig(
                SocketConfig.custom()
                    .setTcpNoDelay(true)
                    .build()
            )
            .setMaxConnPerRoute(5)
            .setRetryHandler(new DefaultHttpRequestRetryHandler(1, false)) // Retry each request once
            .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
            .build();
    }

}
