package uk.ac.warwick.util.mywarwick.model.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ResponseTest {


    ObjectMapper objectMapper = new ObjectMapper();

    final String expectedFailedJsonResponse = "{\"success\":true,\"status\":\"string\",\"errors\":[{\"id\":\"no-permission\",\"message\":\"User'example-user'doesnothavepermissiontoposttothestreamforprovider'example-provider'\"}]}";
    final String expectedSuccessJsonResponse = "{\"success\":true,\"status\":\"ok\",\"data\":{\"id\":\"9e71b33b-debd-40e5-8e6c-989c9de8c547\"}}";

    @Test
    public void shouldMapSuccessResponseToObjectCorrectly() throws IOException {
        Response response = new Response();
        response.setSuccess(true);
        response.setStatus("ok");
        response.setData(new Data("9e71b33b-debd-40e5-8e6c-989c9de8c547"));
        assertTrue(EqualsBuilder.reflectionEquals(objectMapper.readValue(expectedSuccessJsonResponse, Response.class), response));
    }

    @Test
    public void shouldMapFailedResponseToObjectCorrectly() throws IOException {
        Response response = new Response();
        response.setSuccess(true);
        response.setStatus("string");
        response.setError(new Error("no-permission", "User'example-user'doesnothavepermissiontoposttothestreamforprovider'example-provider'"));
        assertTrue(EqualsBuilder.reflectionEquals(objectMapper.readValue(expectedFailedJsonResponse, Response.class), response));
    }


}