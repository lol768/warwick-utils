package uk.ac.warwick.util.queue.conversion;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

public class AnnotationJsonObjectConverter implements JsonObjectConverter {

    private Map<String, Class<?>> mappings = new HashMap<String, Class<?>>();
    
    public Object fromJson(String type, JSONObject json) {
        try {
        return new ObjectMapper().readValue(json.toString(), mappings.get(type));
        } catch (JsonGenerationException e) {
            throw new IllegalArgumentException(e);
        } catch (JsonMappingException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean supports(Object o) {
        return o.getClass().getAnnotation(ItemType.class) != null;
    }

    public JSONObject toJson(Object o) {
        try {
            return new JSONObject(new ObjectMapper().writeValueAsString(o));
        } catch (JsonGenerationException e) {
            throw new IllegalArgumentException(e);
        } catch (JsonMappingException e) {
            throw new IllegalArgumentException(e);
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void registerType(String value, Class<?> c) {
        mappings.put(value, c);
    }

}
