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

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Named
@Singleton
public class MyWarwickServiceImpl implements MyWarwickService {

    private final Logger LOGGER = LoggerFactory.getLogger(MyWarwickServiceImpl.class);
    private final Set<Instance> instances;
    private final HttpClient httpclient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    public MyWarwickServiceImpl(HttpClient httpclient, Configuration configuration) {
        configuration.validate();

        this.httpclient = httpclient;
        this.instances = configuration.getInstances();

        httpclient.start();
    }

    private CompletableFuture<List<Response>> send(Activity activity, boolean isNotification) {
        List<CompletableFuture<Response>> listOfCompletableFutures = instances.stream().map(instance -> {
            CompletableFuture<Response> completableFuture = new CompletableFuture<>();
            final String path = isNotification ? instance.getNotificationPath() : instance.getActivityPath();
            final String reqJsonBody = makeJsonBody(activity);
            httpclient.execute(
                makeRequest(
                    path,
                    reqJsonBody,
                    instance.getApiUser(),
                    instance.getApiPassword(),
                    instance.getProviderId()),
                new FutureCallback<HttpResponse>() {
                    Response response = new Response();

                    @Override
                    public void completed(HttpResponse httpResponse) {
                        if (LOGGER.isDebugEnabled()) LOGGER.debug("Request completed with mywarwick api( "+ path +" )");
                        try {
                            String responseString = EntityUtils.toString(httpResponse.getEntity());
                            response = mapper.readValue(responseString, Response.class);
                            completableFuture.complete(response);
                            if (response.getErrors().size() != 0) {
                                LOGGER.error("Request completed but it contains an error:" +
                                    "\npath: " + path +
                                    "\ninstance: " + instance +
                                    "\nrequest json body: " + reqJsonBody +
                                    "\nHTTP Status Code: " + httpResponse.getStatusLine().getStatusCode() +
                                    "\nResponse:\n" + response.toString()
                                );
                            }
                        } catch (IOException e) {
                            LOGGER.error("An IOException was thrown during communicating with mywarwick:\n" +
                                e.getMessage() +
                                "\npath: " + path +
                                "\ninstance: " + instance+
                                "\nrequest json body: " + reqJsonBody);
                            response.setError(new Error("", e.getMessage()));
                            completableFuture.complete(response);
                        }
                    }

                    @Override
                    public void failed(Exception e) {
                        LOGGER.error("Request to mywarwick API has failed with errors: " + e.getMessage() +
                                "\npath: " + path +
                                "\ninstance: " + instance +
                                "\nrequest json body: " + reqJsonBody, e);
                        response.setError(new Error("", e.getMessage()));
                        completableFuture.complete(response);
                    }

                    @Override
                    public void cancelled() {
                        String message = "Request to mywarwick has been cancelled";
                        if (LOGGER.isDebugEnabled()) LOGGER.debug(message);
                        response.setError(new Error("", message));
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
    public CompletableFuture<List<Response>> sendAsActivity(Activity activity) {
        return send(activity, false);
    }

    @Override
    public CompletableFuture<List<Response>> sendAsNotification(Activity activity) {
        return send(activity, true);
    }

    String makeJsonBody(Activity activity) {
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(activity);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            jsonString = "{}";
        }
        return jsonString;
    }

    HttpPost makeRequest(String path, String json, String apiUser, String apiPassword, String providerId) {
        final HttpPost request = new HttpPost(path);
        request.addHeader(
            "Authorization",
            "Basic " + Base64.getEncoder().encodeToString((apiUser + ":" + apiPassword).getBytes(StandardCharsets.UTF_8)));
        request.addHeader(
            "Content-type",
            "application/json");
        request.addHeader(
            "User-Agent",
            providerId + ":" + this.getClass().getCanonicalName());
        request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        return request;
    }

    public Set<Instance> getInstances() {
        return instances;
    }

    HttpClient getHttpClient() {
        return httpclient;
    }

    @PreDestroy
    public void destroy() throws Exception {
        httpclient.destroy();
    }
}