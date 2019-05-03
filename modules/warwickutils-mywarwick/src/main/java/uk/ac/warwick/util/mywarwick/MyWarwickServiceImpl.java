package uk.ac.warwick.util.mywarwick;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.util.mywarwick.model.Configuration;
import uk.ac.warwick.util.mywarwick.model.Instance;
import uk.ac.warwick.util.mywarwick.model.request.Activity;
import uk.ac.warwick.util.mywarwick.model.request.PushNotification;
import uk.ac.warwick.util.mywarwick.model.response.Response;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Named
@Singleton
public class MyWarwickServiceImpl implements MyWarwickService {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final Logger LOGGER = LoggerFactory.getLogger(MyWarwickServiceImpl.class);
    private final Set<Instance> instances;
    private final HttpClient httpclient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final MyWarwickHttpResponseCallbackHelper callbackHelper = new DefaultMyWarwickHttpResponseCallbackHelper();

    @Inject
    public MyWarwickServiceImpl(HttpClient httpclient, Configuration configuration) {
        configuration.validate();

        this.httpclient = httpclient;
        this.instances = configuration.getInstances();

        httpclient.start();
    }

    private CompletableFuture<List<Response>> send(Activity activity, boolean isNotification, boolean isTransient, int maxAttempts) {
        final String reqJson = makeJsonBody(activity);
        List<CompletableFuture<Response>> listOfCompletableFutures = instances.stream().map(instance -> {
            CompletableFuture<Response> completableFuture = new CompletableFuture<>();
            final String reqPath;
            if (isTransient && isNotification) {
                reqPath = instance.getTransientPushPath();
            } else if (isNotification) {
                reqPath = instance.getNotificationPath();
            } else {
                reqPath = instance.getActivityPath();
            }

            HttpPost request = makeRequest(
                    reqPath,
                    reqJson,
                    instance.getApiUser(),
                    instance.getApiPassword(),
                    instance.getProviderId()
            );

            MyWarwickHttpResponseCallback myWarwickHttpResponseCallback = new MyWarwickHttpResponseCallback(
                    reqPath,
                    reqJson,
                    instance,
                    LOGGER,
                    completableFuture,
                    callbackHelper
            );

            RetryingHttpResponseCallback retryingHttpResponseCallback = new RetryingHttpResponseCallback(
                    httpclient,
                    request,
                    myWarwickHttpResponseCallback,
                    LOGGER,
                    maxAttempts
            );

            httpclient.execute(request, retryingHttpResponseCallback);
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
        return send(activity, false, false, DEFAULT_MAX_ATTEMPTS);
    }

    @Override
    public CompletableFuture<List<Response>> sendAsActivity(Activity activity, int maxAttempts) {
        return send(activity, false, false, maxAttempts);
    }

    @Override
    public CompletableFuture<List<Response>> sendAsNotification(Activity activity) {
        return send(activity, true, false, DEFAULT_MAX_ATTEMPTS);
    }

    @Override
    public CompletableFuture<List<Response>> sendAsNotification(Activity activity, int maxAttempts) {
        return send(activity, true, false, maxAttempts);
    }

    @Override
    public CompletableFuture<List<Response>> sendAsTransientPush(PushNotification pushNotification) {
        return send(pushNotification, true, true, DEFAULT_MAX_ATTEMPTS);
    }

    @Override
    public CompletableFuture<List<Response>> sendAsTransientPush(PushNotification pushNotification, int maxAttempts) {
        return send(pushNotification, true, true, maxAttempts);
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

    Set<Instance> getInstances() {
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