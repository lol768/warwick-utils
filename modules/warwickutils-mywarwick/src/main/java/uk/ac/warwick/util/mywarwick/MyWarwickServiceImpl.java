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
            final String reqPath = isNotification ? instance.getNotificationPath() : instance.getActivityPath();
            final String reqJson = makeJsonBody(activity);
            httpclient.execute(
                    makeRequest(
                            reqPath,
                            reqJson,
                            instance.getApiUser(),
                            instance.getApiPassword(),
                            instance.getProviderId()),
                    new FutureCallback<HttpResponse>() {
                        @Override
                        public void completed(HttpResponse httpResponse) {
                            handleCompletedHttpResponse(httpResponse, completableFuture, instance);
                        }

                        @Override
                        public void failed(Exception e) {
                            handleFailedHttpResponse(e, instance, reqJson, reqPath, completableFuture);
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

    public void handleCompletedHttpResponse(
            HttpResponse httpResponseFromMyWarwick,
            CompletableFuture<Response> completableFuture,
            Instance instance) {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Request completed");
        try {
            Response response = parseHttpResponseToResponseObject(httpResponseFromMyWarwick);
            completableFuture.complete(response);
            if (response.getErrors().size() != 0) {
                logError(instance, "Request completed but it contains error(s):" +
                        "\nbaseUrl:" + instance.getBaseUrl() +
                        "\nHTTP Status Code: " + httpResponseFromMyWarwick.getStatusLine().getStatusCode() +
                        "\nResponse:\n" + response.toString()
                );
            }
            if (response.getWarnings().size() != 0) {
                LOGGER.warn("Request completed but it contains warning(s):" +
                        "\nbaseUrl:" + instance.getBaseUrl() +
                        "\nHTTP Status Code: " + httpResponseFromMyWarwick.getStatusLine().getStatusCode() +
                        "\nResponse:\n" + response.toString()
                );
            }
        } catch (IOException e) {
            Response errorResponse = new Response();
            logError(instance, "An IOException was thrown communicating with mywarwick:\n" +
                    e.getMessage() +
                    "\nbaseUrl: " + instance.getBaseUrl());
            errorResponse.setError(new Error("", e.getMessage()));
            completableFuture.complete(errorResponse);
        }
    }

    public void handleFailedHttpResponse(
            Exception e,
            Instance instance,
            String reqJson,
            String reqPath,
            CompletableFuture<Response> completableFuture) {
        logError(instance, "Request to mywarwick API has failed with errors:" +
                "\npath: " + reqPath +
                "\ninstance: " + instance +
                "\nrequest json " + reqJson +
                "\nerror message:" + e.getMessage(), e);
        Response failedResponse = new Response();
        failedResponse.setError(new Error("", e.getMessage()));
        completableFuture.complete(failedResponse);
    }

    public Response parseHttpResponseToResponseObject(HttpResponse httpResponse) throws IOException {
        String responseString = EntityUtils.toString(httpResponse.getEntity());
        return mapper.readValue(responseString, Response.class);
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
                "MyWarwickService/" + providerId);
        request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        return request;
    }

    void logError(Instance instance, String message) {
        if (instance.getLogErrors()) {
            LOGGER.error(message);
        } else {
            LOGGER.warn(message);
        }
    }

    void logError(Instance instance, String message, Exception e) {
        if (instance.getLogErrors()) {
            LOGGER.error(message, e);
        } else {
            LOGGER.warn(message, e);
        }
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