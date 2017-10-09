package uk.ac.warwick.util.convert.cloudconvert;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import uk.ac.warwick.util.convert.DocumentConversionResult;
import uk.ac.warwick.util.convert.DocumentConversionService;
import uk.ac.warwick.util.convert.S3ByteSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CloudConvertDocumentConversionService implements DocumentConversionService, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudConvertDocumentConversionService.class);

    private static final Map<String, String> PRESETS = new HashMap<String, String>() {{
        put("doc", "l13YvUeMsA");
        put("rtf", "zKJDpBNOuy");
        put("odt", "E6jN1SnYCW");
        put("docx", "J76dyfkTLh");
        put("docm", "jI1QgRVl5m");
        put("xls", "cQMFkKD14j");
        put("ods", "fnMu96S18y");
        put("xlsx", "1rfXoAEbP5");
        put("xlsm", "1rfXoAEbP5");
        put("ppt", "UlObp49CwQ");
        put("pps", "v30mHJN6ZO");
        put("pptx", "l21KivdyTm");
        put("ppsx", "pQ3zsr0xtT");
        put("pptm", "msJPQIRp6B");
    }};

    private static final String API_HOST = "api.cloudconvert.com";

    private final CloseableHttpClient httpClient;

    private final String apiKey;

    private final Detector mimeTypeDetector = new DefaultDetector(MimeTypes.getDefaultMimeTypes());

    private final AmazonS3 s3;

    private final String awsAccessKey;

    private final String awsSecretKey;

    private final String bucketName;

    public CloudConvertDocumentConversionService(String apiKey, String awsAccessKey, String awsSecretKey, String awsBucketName) {
        this.apiKey = apiKey;
        this.s3 = new AmazonS3Client(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
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
    public CloudConvertDocumentConversionResult convert(ByteSource in, String filename, String inputFormat, String outputFormat) throws IOException {
        final String conversionId = UUID.randomUUID().toString();

        ContentType contentType;
        try (InputStream is = in.openBufferedStream()) {
            contentType = ContentType.parse(mimeTypeDetector.detect(is, new Metadata()).toString());
        }

        // POST the file for conversion
        HttpPost request = new HttpPost("https://" + API_HOST + "/convert");
        request.setEntity(
            MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addBinaryBody("file", in.openStream(), contentType, filename)
                .addTextBody("apikey", apiKey)
                .addTextBody("inputformat", inputFormat)
                .addTextBody("outputformat", outputFormat)
                .addTextBody("preset", PRESETS.get(inputFormat))
                .addTextBody("input", "upload")
                .addTextBody("wait", "true") // block until complete
                .addTextBody("output[s3][accesskeyid]", awsAccessKey)
                .addTextBody("output[s3][secretaccesskey]", awsSecretKey)
                .addTextBody("output[s3][bucket]", bucketName)
                .addTextBody("output[s3][region]", "eu-west-1")
                .addTextBody("output[s3][path]", conversionId + "/")
                .addTextBody("download", "false")
                .build()
        );

        try {
            return httpClient.execute(request, response -> {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                    return CloudConvertDocumentConversionResult.error(conversionId, "The document was corrupted or otherwise incompatible with the document conversion service. Please check your document and try again.");
                }
                else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    return CloudConvertDocumentConversionResult.error(conversionId, "Unexpected response code from the document conversion service: " + response.getStatusLine() + "\n" + EntityUtils.toString(response.getEntity()));
                }

                try {
                    JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
                    JSONObject output = json.getJSONObject("output");

                    if (output.has("files")) {
                        JSONArray files = output.getJSONArray("files");

                        ImmutableList.Builder<String> keys = ImmutableList.builder();
                        for (int i = 0; i < files.length(); i++) {
                            keys.add(files.getString(i));
                        }

                        return CloudConvertDocumentConversionResult.success(conversionId, keys.build());
                    } else {
                        String key = output.getString("filename");

                        return CloudConvertDocumentConversionResult.success(conversionId, Collections.singletonList(key));
                    }
                } catch (JSONException e) {
                    return CloudConvertDocumentConversionResult.error(conversionId, "Invalid JSON received from the document conversion service: " + e.getMessage());
                }
            });
        } catch (SocketTimeoutException e) {
            return CloudConvertDocumentConversionResult.error(conversionId, "Timeout from the conversion service.");
        }
    }

    @Override
    public ByteSource getConvertedFile(DocumentConversionResult result, String key) {
        return new S3ByteSource(s3, bucketName, result.getConversionId() + "/" + key);
    }

    // Specific to CloudConvert - get the number of credits remaining for this month
    public int getRemainingCredits() throws IOException {
        HttpGet request = new HttpGet("https://" + API_HOST + "/user");
        request.setHeader("Authorization", "Bearer " + apiKey);

        return httpClient.execute(request, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IllegalStateException("Unexpected response code " + response.getStatusLine().getStatusCode() + " returned from CloudConvert: " + EntityUtils.toString(response.getEntity()));
            }

            try {
                String responseContents = EntityUtils.toString(response.getEntity());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Response received for request to get user information: " + responseContents);
                }

                JSONObject json = new JSONObject(responseContents);
                return json.getInt("minutes");
            } catch (JSONException e) {
                throw new IllegalStateException("Invalid JSON returned from CloudConvert.", e);
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        httpClient.close();
    }

}
