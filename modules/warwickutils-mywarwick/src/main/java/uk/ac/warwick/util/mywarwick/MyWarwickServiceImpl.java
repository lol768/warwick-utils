package uk.ac.warwick.util.mywarwick;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.util.mywarwick.model.Configuration;
import uk.ac.warwick.util.mywarwick.model.Instance;
import uk.ac.warwick.util.mywarwick.model.request.Activity;
import uk.ac.warwick.util.mywarwick.model.response.Error;
import uk.ac.warwick.util.mywarwick.model.response.Response;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Named
@Singleton
public class MyWarwickServiceImpl implements MyWarwickService {

    private final Logger LOGGER = LoggerFactory.getLogger(MyWarwickServiceImpl.class);
    private Set<Instance> instances;
    private HttpClient httpclient;
    private final ObjectMapper mapper = new ObjectMapper();
    private Configuration configuration;

    @Inject
    public MyWarwickServiceImpl(HttpClient httpclient, Configuration configuration) {
        this.httpclient = httpclient;
        this.configuration = configuration;
        this.setConfiguration(this.configuration);
        httpclient.start();
    }

    private Future<List<Response>> send(Activity activity, boolean isNotification) {
        List<CompletableFuture<Response>> listOfCompletableFutures = instances.stream().map(config -> {
            CompletableFuture<Response> completableFuture = new CompletableFuture<Response>();
            final String path = isNotification ? config.getNotificationPath() : config.getActivityPath();
            httpclient.execute(
                    makeRequest(
                            path,
                            makeJsonBody(activity),
                            config.getApiUser(),
                            config.getApiPassword(),
                            config.getProviderId()),
                    new FutureCallback<HttpResponse>() {
                        Response response = new Response();

                        @Override
                        public void completed(HttpResponse httpResponse) {
                            LOGGER.info("request completed");
                            try {
                                String responseString =  EntityUtils.toString(httpResponse.getEntity());
                                response = mapper.readValue(responseString, Response.class);
                                completableFuture.complete(response);
                                if (httpResponse.getStatusLine().getStatusCode() != 201) {
                                    LOGGER.error("request completed" + "but status code is not right" + httpResponse.getStatusLine().getStatusCode());
                                }
                            } catch (IOException e) {
                                LOGGER.error(e.getMessage());
                                response.setError(new Error("", e.getMessage()));
                                completableFuture.complete(response);
                            }
                        }

                        @Override
                        public void failed(Exception e) {
                            LOGGER.error("error talking to mywarwick" + e.getMessage());
                            response.setError(new Error("", e.getMessage()));
                            completableFuture.complete(response);
                        }

                        @Override
                        public void cancelled() {
                            LOGGER.info("request canceled");
                            response.setError(new Error("", "http request cancelled"));
                            completableFuture.complete(response);
                        }
                    });
            return completableFuture;
        }).collect(Collectors.toList());

        return CompletableFuture.allOf(listOfCompletableFutures.toArray(new CompletableFuture[listOfCompletableFutures.size()]))
                .thenApply(v -> listOfCompletableFutures
                        .stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

    @Override
    public Future<List<Response>> sendAsActivity(Activity activity) {
        return send(activity, false);
    }

    @Override
    public Future<List<Response>> sendAsNotification(Activity activity) {
        return send(activity, true);
    }

    public String makeJsonBody(Activity activity) {
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(activity);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            jsonString = "{}";
        }
        return jsonString;
    }

    public HttpPost makeRequest(String path, String json, String apiUser, String apiPassword, String providerId) {
        final HttpPost request = new HttpPost(path);
        request.addHeader(
                "Authorization",
                "Basic " + Base64.getEncoder().encodeToString((apiUser + ":" + apiPassword).getBytes(Charset.defaultCharset())));
        request.addHeader(
                "Content-type",
                "application/json");
        request.addHeader(
                "User-Agent",
                providerId + ":" + this.getClass().getCanonicalName());
        request.setEntity(new StringEntity(json, Charset.defaultCharset()));
        return request;
    }


    public Collection<Instance> getInstances() {
        return instances;
    }

    public HttpClient getHttpclient() {
        return httpclient;
    }

    public void setHttpclient(HttpClient httpclient) {
        this.httpclient = httpclient;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        this.instances = this.configuration.getInstances().stream().distinct().collect(Collectors.toSet());
    }
}