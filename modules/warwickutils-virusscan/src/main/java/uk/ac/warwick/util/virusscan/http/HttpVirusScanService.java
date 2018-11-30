package uk.ac.warwick.util.virusscan.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteSource;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import uk.ac.warwick.util.virusscan.VirusScanResult;
import uk.ac.warwick.util.virusscan.VirusScanService;
import uk.ac.warwick.util.virusscan.VirusScanServiceStatus;
import uk.ac.warwick.util.virusscan.conf.Configuration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Named("virusScanService")
@Singleton
public class HttpVirusScanService implements VirusScanService {

    private final AsyncHttpClient asyncHttpClient;

    private final Configuration configuration;

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    public HttpVirusScanService(AsyncHttpClient asyncHttpClient, Configuration configuration) {
        this.asyncHttpClient = asyncHttpClient;
        this.configuration = configuration;
    }

    @PostConstruct
    public void init() {
        asyncHttpClient.start();
    }

    @Override
    public CompletableFuture<VirusScanResult> scan(ByteSource in) throws IOException {
        HttpPost request = prepare(new HttpPost(configuration.getApiHost() + "/scan"));
        request.setEntity(EntityBuilder.create().setStream(in.openStream()).build());

        CompletableFuture<VirusScanResult> result = new CompletableFuture<>();
        asyncHttpClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                HttpEntity entity = response.getEntity();

                try {
                    result.complete(mapper.readValue(entity.getContent(), HttpVirusScanResult.class));
                } catch (IOException e) {
                    result.completeExceptionally(e);
                } finally {
                    EntityUtils.consumeQuietly(entity);
                }
            }

            @Override
            public void failed(Exception ex) {
                result.completeExceptionally(ex);
            }

            @Override
            public void cancelled() {
                result.cancel(true);
            }
        });

        return result;
    }

    @Override
    public CompletableFuture<VirusScanServiceStatus> status() {
        HttpGet request = prepare(new HttpGet(configuration.getApiHost() + "/service/healthcheck"));

        CompletableFuture<VirusScanServiceStatus> result = new CompletableFuture<>();
        asyncHttpClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            @SuppressWarnings("unchecked")
            public void completed(HttpResponse response) {
                HttpEntity entity = response.getEntity();
                try {
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        Map<String, Object> healthchecks = mapper.readValue(entity.getContent(), new TypeReference<HashMap<String, Object>>() {});
                        Map<String, Object> healthcheck = ((List<Map<String, Object>>) healthchecks.get("data")).iterator().next();

                        boolean isAvailable = "okay".equals(healthcheck.get("status"));
                        String statusMessage = healthcheck.get("message").toString();

                        result.complete(new HttpVirusScanServiceStatus(isAvailable, statusMessage));
                    } else {
                        result.complete(new HttpVirusScanServiceStatus(false, "Error connecting to virus scan service"));
                    }
                } catch (Throwable e) {
                    result.completeExceptionally(e);
                } finally {
                    EntityUtils.consumeQuietly(entity);
                }
            }

            @Override
            public void failed(Exception ex) {
                result.completeExceptionally(ex);
            }

            @Override
            public void cancelled() {
                result.cancel(true);
            }
        });

        return result;
    }

    private <T extends HttpUriRequest> T prepare(T request) {
        request.setHeader("Authorization", "Api-Key " + configuration.getApiKey());
        request.setHeader("User-Agent", "WarwickUtils HttpVirusScanService");

        return request;
    }

    private static class HttpVirusScanResult implements VirusScanResult {

        private Status status;

        private Optional<String> virus = Optional.empty();

        private Optional<String> error = Optional.empty();

        @Override
        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        @Override
        public Optional<String> getVirus() {
            return virus;
        }

        public void setVirus(String virus) {
            this.virus = Optional.of(virus);
        }

        @Override
        public Optional<String> getError() {
            return error;
        }

        public void setError(String error) {
            this.error = Optional.of(error);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("status", status)
                .append("virus", virus)
                .append("error", error)
                .toString();
        }
    }

    private static class HttpVirusScanServiceStatus implements VirusScanServiceStatus {

        private final boolean available;

        private final String statusMessage;

        private HttpVirusScanServiceStatus(boolean available, String statusMessage) {
            this.available = available;
            this.statusMessage = statusMessage;
        }

        @Override
        public boolean isAvailable() {
            return available;
        }

        @Override
        public String getStatusMessage() {
            return statusMessage;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("available", available)
                .append("statusMessage", statusMessage)
                .toString();
        }
    }

}
