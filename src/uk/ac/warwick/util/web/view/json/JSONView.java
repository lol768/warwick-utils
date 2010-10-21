package uk.ac.warwick.util.web.view.json;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public abstract class JSONView extends AbstractJSONView<JSONObject> {

    @Override
    public final JSON<JSONObject> renderToJSON(Map<String, Object> model, List<String> errors) throws Exception {
        return JSON.wrap(render(model, errors));
    }

    /**
     * @return a {@link JSONObject} of the results
     */
    public abstract JSONObject render(Map<String, Object> model, List<String> errors) throws Exception;
    
    public static JSONView combine(final JSONView... views) {
        return new JSONView() {
            @SuppressWarnings("unchecked")
            @Override
            public JSONObject render(Map<String, Object> model, List<String> errors) throws Exception {
                JSONObject combined = new JSONObject();
                for (JSONView view : views) {
                    JSONObject object = view.render(model, errors);
                    for (Iterator<String> itr = object.keys(); itr.hasNext();) {
                        String key = itr.next();
                        combined.put(key, object.get(key));
                    }
                }
                
                return combined;
            }            
        };
    }
    
    public static JSONView of(final JSONObject object) {
        return JSONView.of(object, new String[0]);        
    }
    
    public static JSONView of(final JSONObject object, final String... error) {
        return JSONView.of(object, Arrays.asList(error));
    }
    
    public static JSONView of(final JSONObject object, final Iterable<String> error) {
        return new JSONView() {
            @Override
            public JSONObject render(Map<String, Object> model, List<String> errors) throws Exception {
                for (String err : error) {
                    errors.add(err);
                }
                
                return object;
            }            
        };
    }
    
    public static JSONView errors(final String... error) {
        return JSONView.errors(Arrays.asList(error));
    }
    
    public static JSONView errors(final Iterable<String> error) {
        return JSONView.of(new JSONObject(), error);
    }

}
