package uk.ac.warwick.util.convert.file2txt;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.util.convert.ConversionException;
import uk.ac.warwick.util.convert.TextConversionService;
import uk.ac.warwick.util.core.jodatime.DateTimeUtils;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.UriBuilder;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.IDN;
import java.net.ProxySelector;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

public class HttpTextConversionService implements TextConversionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpTextConversionService.class);

    private static final String DEFAULT_CONVERSION_SERVICE_HOSTNAME = "xn--2u8h.warwick.ac.uk";

    private static final Duration TTL_AVAILABLE_CONVERSIONS = Duration.ofDays(1);

    private final Multimap<String, String> availableConversions = HashMultimap.create();

    private final Uri endpoint;

    private final String apiKey;

    private final CloseableHttpClient httpClient;

    private Instant lastRefreshedAvailable;

    public HttpTextConversionService(String apiKey) {
        this(DEFAULT_CONVERSION_SERVICE_HOSTNAME, apiKey);
    }

    public HttpTextConversionService(String hostname, String apiKey) {
        this.endpoint =
            new UriBuilder()
                .setScheme("https")
                .setAuthority(IDN.toASCII(hostname))
                .setPath("/")
                .toUri();
        this.apiKey = apiKey;

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
                    .setSocketTimeout(300000) // 5 minutes
                    .setExpectContinueEnabled(true)
                    .build()
            )
            .setDefaultSocketConfig(
                SocketConfig.custom()
                    .setTcpNoDelay(true)
                    .build()
            )
            .setMaxConnPerRoute(5)
            .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
            .build();
    }

    @Override
    public synchronized boolean canConvert(ContentType from, ContentType to) {
        if (lastRefreshedAvailable == null || Duration.between(lastRefreshedAvailable, Instant.now(DateTimeUtils.CLOCK_IMPLEMENTATION)).compareTo(TTL_AVAILABLE_CONVERSIONS) > 0) {
            try {
                HttpGet request = new HttpGet(endpoint.toJavaUri());
                httpClient.execute(request, response -> {
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from \uD83D\uDD00 when trying to list available conversions: " + EntityUtils.toString(response.getEntity()));
                    }

                    try {
                        String responseContents = EntityUtils.toString(response.getEntity());
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Response received for request to list available conversions: " + responseContents);
                        }

                        JSONArray json = new JSONArray(responseContents);
                        availableConversions.clear();
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject obj = json.getJSONObject(i);
                            availableConversions.put(obj.getString("from"), obj.getString("to"));
                        }

                        return null;
                    } catch (JSONException e) {
                        throw new ConversionException("Invalid JSON returned from \uD83D\uDD00", e);
                    }
                });
            } catch (IOException e) {
                LOGGER.error("Couldn't refresh list of available conversions", e);
            }
        }

        return availableConversions.containsEntry(from.getMimeType(), to.getMimeType());
    }

    @Override
    public ByteSource convert(ByteSource in, ContentType from, ContentType to) throws IOException {
        if (!canConvert(from, to)) throw new ConversionException("Can't convert from " + from + " to " + to);

        // Save the contents to a temporary file so it's repeatable
        HttpPost request = new HttpPost(endpoint.toJavaUri());
        request.setHeader("Authorization", "API-Key " + apiKey);
        request.setHeader("User-Agent", "WarwickUtils HttpTextConversionService");
        request.setHeader("Accept", to.getMimeType());
        request.setEntity(new InputStreamEntity(in.openStream(), in.size(), from));

        File tempFile = httpClient.execute(request, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ConversionException("Invalid status code " + response.getStatusLine().getStatusCode() + " returned from \uD83D\uDD00 when trying to convert: " + EntityUtils.toString(response.getEntity()));
            }

            File f = File.createTempFile("converted", ".tmp");
            f.deleteOnExit();

            Files.asByteSink(f).writeFrom(response.getEntity().getContent());

            return f;
        });
        return Files.asByteSource(tempFile);
    }

    @PreDestroy
    public void destroy() throws Exception {
        httpClient.close();
    }

}
