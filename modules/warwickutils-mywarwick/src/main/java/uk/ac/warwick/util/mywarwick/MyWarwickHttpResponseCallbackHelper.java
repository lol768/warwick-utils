package uk.ac.warwick.util.mywarwick;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import uk.ac.warwick.util.mywarwick.model.response.Response;

import java.io.IOException;

public interface MyWarwickHttpResponseCallbackHelper {
    default Response parseHttpResponseToResponseObject(HttpResponse httpResponse, ObjectMapper mapper) throws IOException {
        String responseString = EntityUtils.toString(httpResponse.getEntity());
        return parseJsonStringToResponseObject(responseString, mapper);
    }

    static Response parseJsonStringToResponseObject(String jsonString, ObjectMapper mapper) throws IOException {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(jsonString, Response.class);
    }
}
