package uk.ac.warwick.util.mywarwick;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.util.mywarwick.model.request.Activity;
import uk.ac.warwick.util.mywarwick.model.Config;
import uk.ac.warwick.util.mywarwick.model.Configs;
import uk.ac.warwick.util.mywarwick.model.response.Error;
import uk.ac.warwick.util.mywarwick.model.response.Response;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Named
@Singleton
public class MyWarwickServiceImpl implements MyWarwickService {

    private final Logger LOGGER = LoggerFactory.getLogger(MyWarwickServiceImpl.class);
    private List<Config> configs;
    private AsyncHttpClient httpclient;
    private final ObjectMapper mapper = new ObjectMapper();

    private MyWarwickServiceImpl(AsyncHttpClient httpclient) {
        this.httpclient = httpclient;
        httpclient.start();
    }

    @Inject
    public MyWarwickServiceImpl(AsyncHttpClient httpclient, Config config) {
        this(httpclient);
        this.configs = new ArrayList<>();
        configs.add(config);
    }

    @Inject
    public MyWarwickServiceImpl(AsyncHttpClient httpclient, Configs configs) {
        this(httpclient);
        ArrayList<Config> configArrayList = new ArrayList<>();
        configArrayList.add(configs.getDevConfig());
        configArrayList.add(configs.getProdConfig());
        configArrayList.add(configs.getTestConfig());
        setConfigs(configArrayList);
    }

    public MyWarwickServiceImpl(AsyncHttpClient httpclient, List<Config> configs) {
        this(httpclient);
        setConfigs(configs);
    }

    private Future<List<Response>> send(Activity activity, boolean isNotification) {
        List<Future<HttpResponse>> futureList = configs.stream().map(config -> {
            final String path = isNotification ? config.getNotificationPath() : config.getActivityPath();
            return httpclient.execute(
                    makeRequest(
                            path,
                            makeJsonBody(activity),
                            config.getApiUser(),
                            config.getApiPassword(),
                            config.getProviderId()),
                    new FutureCallback<HttpResponse>() {
                        @Override
                        public void completed(HttpResponse response) {
                            LOGGER.info("request completed");
                            if (response.getStatusLine().getStatusCode() != 201) {
                                LOGGER.error("request completed" + "but status code is not right" + response.getStatusLine().getStatusCode());
                            }
                        }

                        @Override
                        public void failed(Exception e) {
                            LOGGER.error("error talking to mywarwick" + e.getMessage());
                        }

                        @Override
                        public void cancelled() {
                            LOGGER.info("request canceled");
                        }
                    });
        }).collect(Collectors.toList());

        return CompletableFuture
                .allOf(futureList.toArray(new CompletableFuture[futureList.size()]))
                .thenApply(value -> futureList.stream()
                        .map(element -> {
                            Response response = new Response();
                            if (element.isDone()){
                                try {
                                    HttpResponse httpResponse = element.get();
                                    response = mapper.readValue(httpResponse.getEntity().toString(), Response.class);
                                } catch (InterruptedException | ExecutionException | IOException e) {
                                    e.printStackTrace();
                                    response.setError(new Error("",e.getMessage()));
                                }
                            }
                            return response;
                        }).collect(Collectors.toList())
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
            e.printStackTrace();
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
                providerId
        );
        request.setEntity(new StringEntity(json, Charset.defaultCharset()));
        return request;
    }


    public List<Config> getConfigs() {
        return configs;
    }

    public AsyncHttpClient getHttpclient() {
        return httpclient;
    }

    private void setConfigs(List<Config> configs) {
        if (this.configs == null) this.configs = new ArrayList<>();
        HashSet<Config> configsSet = new HashSet<>(configs);
        this.configs = new ArrayList<>(configsSet);
    }
}