package uk.ac.warwick.util.mywarwick;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import uk.ac.warwick.util.mywarwick.model.Activity;
import uk.ac.warwick.util.mywarwick.model.Config;

import java.io.*;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MyWarwickServiceImpl implements MyWarwickService {

    private static final Logger LOGGER = Logger.getRootLogger();

    String baseUrl;
    String activityPath;
    String notificationPath;
    String apiUser;
    String apiPassword;
    String providerId;

    public MyWarwickServiceImpl() throws IOException {
        Properties config = new Properties();
        InputStream input = null;
        input = new FileInputStream("config.properties");
        config.load(input);
        baseUrl = config.get("baseUrl").toString();
        apiUser = config.get("apiUser").toString();
        apiPassword = config.get("apiPassword").toString();
        providerId = config.get("providerId").toString();
        initPaths(config.get("baseUrl").toString(), providerId);
    }

    public MyWarwickServiceImpl(Config config) {
        apiPassword = config.getApiPassword();
        apiUser = config.getApiUser();
        providerId = config.getProviderId();
        baseUrl = config.getBaseUrl();
        initPaths(baseUrl, providerId);
    }

    private void initPaths(String baseUrl, String providerId) {
        activityPath = baseUrl + "/api/streams/" + providerId + "/activities";
        notificationPath = baseUrl + "/api/streams/" + providerId + "/notifications";
    }

    @Override
    public Integer sendAsActivity(Activity activity) throws ExecutionException, InterruptedException {
        return postToMyWarwick(makeRequest(activityPath, makeJsonBody(activity)));
    }

    @Override
    public Integer sendAsNotification(Activity activity) throws ExecutionException, InterruptedException {
        return postToMyWarwick(makeRequest(notificationPath, makeJsonBody(activity)));
    }

    @Override
    public void sendAsActivities(Set<Activity> activities) {
        activities.parallelStream().limit(2).forEach(activity -> {
            try {
                sendAsActivity(activity);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void sendAsNotifications(Set<Activity> activities) {
        activities.parallelStream().limit(2).forEach(activity -> {
            try {
                sendAsNotification(activity);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public JsonNode makeJsonBody(Activity activity) {
        return new ObjectMapper().convertValue(activity, JsonNode.class);
    }

    public RequestBodyEntity makeRequest(String path, JsonNode json) {
        Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {
            private ObjectMapper jacksonObjectMapper
                    = new ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return Unirest.post(path)
                .header("content-type", "application/json")
                .basicAuth(apiUser, apiPassword)
                .body(json);
    }

    public Integer postToMyWarwick(RequestBodyEntity request) throws ExecutionException, InterruptedException {
        HttpResponse<InputStream> response = request.asBinaryAsync().get();
        if (response.getStatus() != 201) {
            LOGGER.error("error talking to mywarwick" + response.getHeaders() + response.getBody());
        }
        return response.getStatus();
    }
}