package uk.ac.warwick.util.queue.conversion;

import org.json.JSONObject;

/**
 * Converter which is capable of turning a particular object
 * or range of objects into JSON, and back again.
 */
public interface JsonObjectConverter{
    JSONObject toJson(Object o);
    Object fromJson(String type, JSONObject json);
    
    boolean supports(Object o);
}
