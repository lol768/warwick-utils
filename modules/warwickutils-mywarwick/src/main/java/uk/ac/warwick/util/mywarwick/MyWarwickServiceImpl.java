package uk.ac.warwick.util.mywarwick;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.util.mywarwick.model.Activity;
import uk.ac.warwick.util.mywarwick.model.Config;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Named
public class MyWarwickServiceImpl implements MyWarwickService {

    private final Logger LOGGER = LoggerFactory.getLogger(MyWarwickServiceImpl.class);
    private List<Config> configs;
    private final Gson gson = new Gson();

    @Inject
    AsyncHttpClient httpclient;

    public MyWarwickServiceImpl(Config config ) {
        this.configs = new ArrayList<>();
        configs.add(config);
    }

    @Inject
    public MyWarwickServiceImpl(List<Config> configs) {
        this.configs = configs;
    }

    private List<Future<HttpResponse>> send(Activity activity, boolean isNotification) {
        httpclient.start();
        return configs.parallelStream().limit(2).map(config -> {
            final String path = isNotification ? config.getNotificationPath() : config.getActivityPath();
            Future<HttpResponse> futureResponse = null;
            futureResponse = httpclient.execute(
                    makeRequest(
                            path,
                            makeJsonBody(activity),
                            config.getApiUser(),
                            config.getApiPassword()),
                    new FutureCallback<HttpResponse>() {
                        @Override
                        public void completed(HttpResponse response) {
                            LOGGER.info("request completed");
                            if (response.getStatusLine().getStatusCode() != 201) {
                                LOGGER.error("request completed" + "but status code is " + response.getStatusLine().getStatusCode());
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

            return futureResponse;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Future<HttpResponse>> sendAsActivity(Activity activity) {
        return send(activity, false);
    }

    @Override
    public List<Future<HttpResponse>> sendAsNotification(Activity activity) {
        return send(activity, true);
    }

    public String makeJsonBody(Activity activity) {
        return gson.toJson(activity);
    }

    public HttpPost makeRequest(String path, String json, String apiUser, String apiPassword) {
        final HttpPost request = new HttpPost(path);
        request.addHeader(
                "Authorization",
                "Basic " + Base64.getEncoder().encodeToString((apiUser + ":" + apiPassword).getBytes(Charset.defaultCharset())));
        request.addHeader(
                "Content-type",
                "application/json");
        request.setEntity(new StringEntity(json, Charset.defaultCharset()));
        return request;
    }

    public List<Config> getConfigs() {
        return configs;
    }
}