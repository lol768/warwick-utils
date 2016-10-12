package uk.ac.warwick.util.convert.cloudconvert;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import uk.ac.warwick.util.convert.ConversionException;
import uk.ac.warwick.util.convert.DocumentConversionService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CloudConvertDocumentConversionService implements DocumentConversionService, InitializingBean, DisposableBean {

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

    private CloseableHttpClient httpClient;

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
        this.awsSecretKey = awsAccessKey;
        this.bucketName = awsBucketName;
    }

    @Override
    public ByteSource convert(ByteSource in, String filename, String inputFormat, String outputFormat) throws IOException {
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
                .addTextBody("filename", UUID.randomUUID().toString() + "." + inputFormat)
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
                .addTextBody("output[s3][path]", UUID.randomUUID().toString() + "/")
                .addTextBody("download", "false")
                .build()
        );

        String s3Key = httpClient.execute(request, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Unexpected response code from convert endpoint: " + response.getStatusLine() + "\n" + EntityUtils.toString(response.getEntity()));
            }

            try {
                JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));

                String key = json.getJSONObject("output").getString("filename");
                if (json.getJSONObject("output").has("dir")) {
                    key = json.getJSONObject("output").getString("dir") + key;
                }

                return key;
            } catch (JSONException e) {
                throw new ConversionException("Invalid JSON received from convert endpoint", e);
            }
        });

        final S3Object object = s3.getObject(new GetObjectRequest(bucketName, s3Key));
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
                    .setConnectTimeout(300000) // 5 minutes
                    .setSocketTimeout(300000) // 5 minutes
                    .setExpectContinueEnabled(true)
                    .setRedirectsEnabled(true)
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

    public static void main(String[] args) throws Exception {
        CloudConvertDocumentConversionService service = new CloudConvertDocumentConversionService("GoIdfNdB_4k6dJOgZwpWx8BcrOBr3GkL8ano05TDwHOYHEkBWU5KRc1yu7cbRripLc5pEG6iygex8GPyrjTWxw", "AKIAI2IWTE7FDZGVCEEA", "7E7AEaZRWfntsukqN6nvc0ByZ1wvh4jj8l9tXuIe", "uk.ac.warwick.sitebuilder.convert");
        service.afterPropertiesSet();

        System.out.println(service.convert(Files.asByteSource(new File("/home/mat/Downloads/VAT invoices.pptx")), "VAT invoices.pptx", "pptx", "png"));

        service.destroy();
    }

}
