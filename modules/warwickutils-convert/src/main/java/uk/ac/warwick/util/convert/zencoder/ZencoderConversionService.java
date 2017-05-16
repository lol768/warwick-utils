package uk.ac.warwick.util.convert.zencoder;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.google.common.io.ByteSource;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import uk.ac.warwick.util.convert.*;
import uk.ac.warwick.util.web.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

public class ZencoderConversionService implements ConversionService, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZencoderConversionService.class);

    private static final String API_HOST = "app.zencoder.com";

    private static final String API_VERSION = "v2";

    private final CloseableHttpClient httpClient;

    private final String apiKey;

    private final AmazonS3 s3;

    private final TransferManager transferManager;

    private final String bucketName;

    public ZencoderConversionService(String zcApiKey, String awsAccessKey, String awsSecretKey, String awsBucketName) {
        this.apiKey = zcApiKey;

        this.s3 = new AmazonS3Client(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
        this.bucketName = awsBucketName;

        this.transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3)
                .build();

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
        // Upload the file to S3
        String uploadId = UUID.randomUUID().toString();
        String key = "inputs/" + uploadId;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(source.size());
        metadata.setContentDisposition(uploadId);

        LOGGER.info("[Upload " + uploadId + "] Uploading " + source + " to Amazon S3: s3://" + bucketName + "/" + key);
        Upload upload = transferManager.upload(bucketName, key, source.openStream(), metadata);

        try {
            upload.waitForCompletion();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }

        HttpPost request = new HttpPost("https://" + API_HOST + "/api/" + API_VERSION + "/jobs");
        request.setHeader("Zencoder-Api-Key", apiKey);

        try {
            request.setEntity(
                EntityBuilder.create()
                    .setContentType(ContentType.APPLICATION_JSON)
                    .setText(new JSONObject() {{
                        put("input", "s3://" + bucketName + "/" + key);
                        put("outputs", new JSONObject() {{
                            put("type", "transfer-only"); // don't encode
                        }});
                    }}.toString())
                    .build()
            );
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        LOGGER.info("[Upload " + uploadId + "] Creating transfer-only job");
        int jobId = httpClient.execute(request, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Zencoder: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[Upload " + uploadId + "] Response received for job creation " + responseContents);
                }

                JSONObject json = new JSONObject(responseContents);
                int id = json.getInt("id");
                int outputId = json.getJSONArray("outputs").getJSONObject(0).getInt("id");

                LOGGER.info("[Upload " + uploadId + "] Job created, job ID " + id + "; output ID " + outputId);

                return id;
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Zencoder", e);
            }
        });

        // Get input information
        ConversionMedia media = getMediaById(Integer.toString(jobId));

        // Wait for the media to have completed processing
        try {
            while (media.getStatus() != ConversionMedia.Status.success && media.getStatus() != ConversionMedia.Status.fail) {
                Thread.sleep(1000);
                media = getMediaById(Integer.toString(jobId));
            }
        } catch (InterruptedException e) {
            // do nothing
        }

        return media;
    }

    private JSONObject getJob(String id) throws IOException {
        HttpGet request = new HttpGet("https://" + API_HOST + "/api/" + API_VERSION + "/jobs/" + id + ".json?api_key=" + apiKey);
        return httpClient.execute(request, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Zencoder: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + id + "] Response received for retrieval of job information: " + responseContents);
                }

                return new JSONObject(responseContents).getJSONObject("job");
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Zencoder", e);
            }
        });
    }

    @Override
    public ConversionMedia getMediaById(String id) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + id + "] Requesting job information");
        }

        try {
            return ZencoderConversionMedia.fromJobJSON(getJob(id));
        } catch (JSONException e) {
            throw new ConversionException("Invalid JSON returned from Zencoder", e);
        } catch (SocketTimeoutException e) {
            return ZencoderConversionMedia.fromTimeout(id);
        }
    }

    @Override
    public ConversionStatus convert(ConversionMedia media, Format format) throws IOException {
        String s3Url = media.getOriginalFilename();
        LOGGER.info("[" + media.getId() + "] Creating encoding job to " + format + " for " + s3Url);

        HttpPost request = new HttpPost("https://" + API_HOST + "/api/" + API_VERSION + "/jobs");
        request.setHeader("Zencoder-Api-Key", apiKey);

        try {
            request.setEntity(
                EntityBuilder.create()
                    .setContentType(ContentType.APPLICATION_JSON)
                    .setText(new JSONObject() {{
                        put("input", s3Url);
                        put("outputs", new JSONArray() {{
                            put(new JSONObject() {{
                                put("base_url", "s3://" + bucketName + "/output/");
                                switch (format) {
                                    case h264:
                                    case jpg:
                                        put("format", "mp4");
                                        break;
                                    case webm:
                                        put("format", "webm");
                                        break;
                                    case mp3:
                                        put("format", "mp3");
                                        put("skip_video", true);
                                        break;
                                    default:
                                        throw new IllegalStateException("Invalid format: " + format);
                                }

                                if (format != Format.mp3) {
                                    put("thumbnails", new JSONObject() {{
                                        put("format", "jpg");
                                        put("number", 1);
                                        put("base_url", "s3://" + bucketName + "/output/");
                                    }});
                                }
                            }});
                        }});
                    }}.toString())
                    .build()
            );
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        int jobId = httpClient.execute(request, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Zencoder: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + media.getId() + "] Response received for job creation " + responseContents);
                }

                JSONObject json = new JSONObject(responseContents);
                int id = json.getInt("id");

                LOGGER.info("[" + media.getId() + "] Job created, job ID " + id);

                return id;
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Zencoder", e);
            }
        });

        return getStatus(Integer.toString(jobId));
    }

    @Override
    public ConversionStatus getStatus(String id) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + id + "] Requesting job progress information");
        }

        HttpGet request = new HttpGet("https://" + API_HOST + "/api/" + API_VERSION + "/jobs/" + id + "/progress.json?api_key=" + apiKey);
        ConversionStatus status = httpClient.execute(request, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from Zencoder: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + id + "] Response received for retrieval of job progress: " + responseContents);
                }

                return ZencoderConversionStatus.fromProgressJSON(Integer.parseInt(id), new JSONObject(responseContents));
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Zencoder", e);
            }
        });

        // If we're at a terminal state, get the job details
        if (status.getStatus() == ConversionStatus.Status.processing) {
            return status;
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[" + id + "] Job is in terminal state " + status.getStatus() + ", returning full job status");
            }

            try {
                return ZencoderConversionStatus.fromCompletedJobJSON(getJob(id));
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON returned from Zencoder", e);
            }
        }
    }

    @Override
    public void delete(ConversionMedia media) throws IOException {
        // Delete the input media from our S3 bucket
        s3.deleteObject(bucketName, Uri.parse(media.getOriginalFilename()).getPath().substring(1));
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

    // Specific to Zencoder - get the number of encoding minutes remaining for this month
    public int getRemainingMinutes() throws IOException {
        HttpGet request = new HttpGet("https://" + API_HOST + "/api/" + API_VERSION + "/account?api_key=" + apiKey);

        return httpClient.execute(request, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IllegalStateException("Unexpected response code " + response.getStatusLine().getStatusCode() + " returned from Zencoder: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Response received for request to get account information: " + responseContents);
                }

                JSONObject json = new JSONObject(responseContents);

                int minutesUsed = json.getInt("minutes_used");
                int includedMinutes = json.getInt("minutes_included");

                return includedMinutes - minutesUsed;
            } catch (JSONException e) {
                throw new IllegalStateException("Invalid JSON returned from CloudConvert", e);
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        httpClient.close();
    }

}
