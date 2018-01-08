package uk.ac.warwick.util.mywarwick.model.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

public class ResponseTest {


    ObjectMapper objectMapper = new ObjectMapper();

    final String expectedFailedJsonResponse = "{\"success\":true,\"status\":\"string\",\"errors\":[{\"id\":\"no-permission\",\"message\":\"User'example-user'doesnothavepermissiontoposttothestreamforprovider'example-provider'\"}]}";
    final String expectedSuccessJsonResponse = "{\"success\":true,\"status\":\"ok\",\"data\":{\"id\":\"9e71b33b-debd-40e5-8e6c-989c9de8c547\"}}";
    final String expectedSuccessWithWarningResponse = "{\"success\":true,\"status\":\"ok\",\"data\":{\"id\":\"fb97dfd7-df8e-4fcd-ab23-241e59c8358c\"},\"warnings\":[{\"id\":\"InvalidUsercodeAudience\",\"message\":\"The request contains one or more invalid usercode: List()\"}]}";

    @Test
    public void shouldMapSuccessResponseToObjectCorrectly() throws IOException {
        Response response = new Response();
        response.setSuccess(true);
        response.setStatus("ok");
        response.setData(new Data("fb97dfd7-df8e-4fcd-ab23-241e59c8358c"));
        response.setWarnings(Collections.singletonList(new Warning("InvalidUsercodeAudience", "The request contains one or more invalid usercode: List()")));
        assertTrue(EqualsBuilder.reflectionEquals(objectMapper.readValue(expectedSuccessWithWarningResponse, Response.class), response));
    }

    @Test
    public void shouldMapSuccessWithWarningResponseToObjectCorrectly() throws IOException {
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