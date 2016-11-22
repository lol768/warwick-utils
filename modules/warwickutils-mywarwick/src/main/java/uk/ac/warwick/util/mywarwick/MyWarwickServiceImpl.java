package uk.ac.warwick.util.mywarwick;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import uk.ac.warwick.util.mywarwick.model.Activity;
import uk.ac.warwick.util.mywarwick.model.Config;
import uk.ac.warwick.util.mywarwick.model.Response;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MyWarwickServiceImpl implements MyWarwickService {

    private final Logger LOGGER = LoggerFactory.getLogger(MyWarwickServiceImpl.class);
    private List<Config> configs;

    public MyWarwickServiceImpl() {
        this.configs = new ArrayList<>();
    }

    public MyWarwickServiceImpl(Config config) {
        this();
        configs.add(config);
    }

    public MyWarwickServiceImpl(List<Config> configs){
        this.configs = configs;
    }

    @Override
    public List<Response> sendAsActivity(Activity activity) throws ExecutionException, InterruptedException {
        return configs.parallelStream().limit(2).map(config -> {
            Integer responseCode = null;
            try {
                responseCode = postToMyWarwick(makeRequest(config.getActivityPath(), makeJsonBody(activity), config.getApiUser(), config.getApiPassword()));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return new Response(activity, config.getActivityPath(), responseCode);
        }).collect(Collectors.toList());
    }

    @Override
    public List<Response> sendAsNotification(Activity activity) throws ExecutionException, InterruptedException {
        return configs.parallelStream().limit(2).map(config -> {
            Integer responseCode = null;
            try {
                responseCode = postToMyWarwick(makeRequest(config.getNotificationPath(), makeJsonBody(activity), config.getApiUser(), config.getApiPassword()));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return new Response(activity, config.getNotificationPath(), responseCode);
        }).collect(Collectors.toList());
    }

    public JsonNode makeJsonBody(Activity activity) {
        return new ObjectMapper().convertValue(activity, JsonNode.class);
    }

    public RequestBodyEntity makeRequest(String path, JsonNode json, String apiUser, String apiPassword) {
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

    public List<Config> getConfigs() {
        return configs;
    }
}