package uk.ac.warwick.util.convert.telestream;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.google.common.io.ByteSource;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.convert.ConversionException;
import uk.ac.warwick.util.convert.ConversionMedia;
import uk.ac.warwick.util.convert.ConversionService;
import uk.ac.warwick.util.convert.ConversionStatus;
import uk.ac.warwick.util.convert.S3ByteSource;
import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.core.jodatime.DateTimeUtils;
import uk.ac.warwick.util.web.Uri;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ProxySelector;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TelestreamConversionService implements ConversionService, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelestreamConversionService.class);

    private static final String API_URL = "api.pandastream.com";

    private static final String API_VERSION = "v3.0";

    private static final long UPLOAD_CHUNK_SIZE = 5 * 1024 * 1024; // 5mb

    private static final DateTimeFormatter ISO8601_STRICT = DateTimeFormatter.ISO_DATE_TIME;

    private final CloseableHttpClient httpClient;

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

        this.httpClient = HttpClientBuilder.create()
            .setDefaultConnectionConfig(
                ConnectionConfig.custom()
                    .setBufferSize(8192)
                    .setCharset(StandardCharsets.UTF_8)
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

    @Override
    public ConversionMedia upload(ByteSource source) throws IOException {
        // Create an upload session
        LOGGER.info("Starting an upload session for " + source);
        Pair<String, String> idAndLocation = post("/videos/upload.json", new HashMap<String, String>() {{
            put("file_name", UUID.randomUUID().toString());
            put("file_size", Long.toString(source.size()));
            put("profiles", "none");
            put("path_format", ":id");
        }}, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Response received for request to start upload session: " + responseContents);
                }

                JSONObject json = new JSONObject(responseContents);

                return Pair.of(json.getString("id"), json.getString("location"));
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Telestream", e);
            }
        });

        String uploadId = idAndLocation.getLeft();
        String uploadLocation = idAndLocation.getRight();

        LOGGER.info("[Upload " + uploadId + "] upload session started, location " + uploadLocation);

        // Upload in chunks
        long totalSize = source.size();
        long startByte = 0;
        while (totalSize > UPLOAD_CHUNK_SIZE) {
            ByteSource chunk = source.slice(startByte, UPLOAD_CHUNK_SIZE);

            HttpPut request = new HttpPut(uploadLocation);
            request.setHeader("Content-Range", "bytes " + startByte + "-" + (startByte + UPLOAD_CHUNK_SIZE - 1) + "/" + totalSize);
            request.setHeader("Content-Transfer-Encoding", "binary");
            request.setEntity(new InputStreamEntity(chunk.openStream(), UPLOAD_CHUNK_SIZE, ContentType.APPLICATION_OCTET_STREAM));

            LOGGER.info("[Upload " + uploadId + "] Uploading chunk " + startByte + "-" + (startByte + UPLOAD_CHUNK_SIZE - 1) + "/" + totalSize);
            httpClient.execute(request, response -> {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                    throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
                }

                // Chunk uploaded OK
                return null;
            });

            startByte += UPLOAD_CHUNK_SIZE;
            totalSize -= UPLOAD_CHUNK_SIZE;
        }

        // Upload the last chunk
        ByteSource chunk = (startByte == 0) ? source : source.slice(startByte, totalSize - startByte);

        HttpPut request = new HttpPut(uploadLocation);
        request.setHeader("Content-Range", "bytes " + startByte + "-" + (totalSize - 1) + "/" + totalSize);
        request.setHeader("Content-Transfer-Encoding", "binary");
        request.setEntity(new InputStreamEntity(chunk.openStream(), totalSize - startByte, ContentType.APPLICATION_OCTET_STREAM));

        LOGGER.info("[Upload " + uploadId + "] Uploading final chunk " + startByte + "-" + (totalSize - 1) + "/" + totalSize);
        return httpClient.execute(request, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[Upload " + uploadId + "] Response received for final chunk upload: " + responseContents);
                }

                JSONObject json = new JSONObject(responseContents);
                TelestreamConversionMedia media = TelestreamConversionMedia.fromJSON(json);

                LOGGER.info("[" + media.getId() + "] Upload complete for " + uploadId);

                return media;
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Telestream", e);
            }
        });
    }

    @Override
    public ConversionMedia getMediaById(String id) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + id + "] Requesting media information");
        }

        return get("/videos/" + id + ".json", response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + id + "] Response received for retrieval of media information: " + responseContents);
                }

                JSONObject json = new JSONObject(responseContents);

                return TelestreamConversionMedia.fromJSON(json);
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Telestream", e);
            }
        });
    }

    @Override
    public ConversionStatus convert(ConversionMedia media, Format format) throws IOException {
        LOGGER.info("[" + media.getId() + "] Creating encoding to " + format);
        return post("/encodings.json", new HashMap<String, String>() {{
            put("video_id", media.getId());
            put("profile_name", format.getProfileName());
            put("screenshots", "true");
        }}, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + media.getId() + "] Response received for encoding creation: " + responseContents);
                }

                JSONObject json = new JSONObject(responseContents);
                TelestreamConversionStatus status = TelestreamConversionStatus.fromJSON(json);

                LOGGER.info("[" + media.getId() + "] encoding created " + status.getId() + ", status " + status.getStatus());

                return status;
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Telestream", e);
            }
        });
    }

    @Override
    public ConversionStatus getStatus(String id) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Requesting encoding status for " + id);
        }

        return get("/encodings/" + id + ".json", new HashMap<String, String>() {{
            put("screenshots", "true");
        }}, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + id + "] Response received for encoding status: " + responseContents);
                }

                JSONObject json = new JSONObject(responseContents);
                return TelestreamConversionStatus.fromJSON(json);
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Telestream", e);
            }
        });
    }

    @Override
    public void delete(ConversionMedia media) throws IOException {
        LOGGER.info("[" + media.getId() + "] Deleting media");
        delete("/videos/" + media.getId() + ".json", response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            LOGGER.info("[" + media.getId() + "] Deleted");

            EntityUtils.consumeQuietly(response.getEntity());
            return null;
        });
    }

    private Uri generateS3PrivateUrl(String objectKey) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey);
        request.setMethod(HttpMethod.GET);
        request.setExpiration(Date.from(Instant.now(DateTimeUtils.CLOCK_IMPLEMENTATION).plus(1, ChronoUnit.HOURS)));

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
        ByteSource source = getS3ByteSource(objectKey);
        try (InputStream is = source.openBufferedStream()) {
            consumer.accept(is);
        }
    }

    private S3ByteSource getS3ByteSource(String objectKey) {
        return new S3ByteSource(s3, bucketName, objectKey);
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

        return getS3ByteSource(status.getFiles().iterator().next());
    }

    @Override
    public ByteSource getScreenshot(ConversionStatus status) throws IOException {
        if (status.getStatus() != ConversionStatus.Status.success || status.getScreenshots().isEmpty()) {
            throw new ConversionException("Conversion not successful or no screenshots generated");
        }

        return getS3ByteSource(status.getScreenshots().iterator().next());
    }

    private <T> T get(String url, ResponseHandler<T> handler) throws IOException {
        return get(url, Collections.emptyMap(), handler);
    }

    private <T> T get(String url, Map<String, String> params, ResponseHandler<T> handler) throws IOException {
        Map<String, String> allParams = new HashMap<>();
        allParams.putAll(params);
        allParams.put("access_key", accessKey);
        allParams.put("factory_id", factoryId);
        allParams.put("timestamp", ISO8601_STRICT.format(LocalDateTime.now(DateTimeUtils.CLOCK_IMPLEMENTATION).atZone(ZoneId.of("Z"))));
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
        allParams.put("timestamp", ISO8601_STRICT.format(LocalDateTime.now(DateTimeUtils.CLOCK_IMPLEMENTATION).atZone(ZoneId.of("Z"))));

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

    private <T> T delete(String url, ResponseHandler<T> handler) throws IOException {
        Map<String, String> allParams = new HashMap<>();
        allParams.put("access_key", accessKey);
        allParams.put("factory_id", factoryId);
        allParams.put("timestamp", ISO8601_STRICT.format(LocalDateTime.now(DateTimeUtils.CLOCK_IMPLEMENTATION).atZone(ZoneId.of("Z"))));

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

            return Base64.getEncoder().encodeToString(sha256HMAC.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // TeleStream-specific API calls
    public TelestreamConversionServiceStatus getServiceStatus() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Requesting a list of all encodings");
        }

        // Get a list of all encodings
        return get("/encodings.json", new HashMap<String, String>() {{
            put("status", "processing");
        }}, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Telestream: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Response received for list of all encodings: " + responseContents);
                }

                JSONArray json = new JSONArray(responseContents);
                return TelestreamConversionServiceStatus.fromJSON(json);
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Telestream", e);
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        httpClient.close();
    }

}
