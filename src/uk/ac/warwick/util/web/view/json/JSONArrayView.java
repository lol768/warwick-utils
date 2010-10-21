package uk.ac.warwick.util.web.view.json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

public abstract class JSONArrayView extends AbstractJSONView<JSONArray> {
    
    @Override
    public final JSON<JSONArray> renderToJSON(Map<String, Object> model, List<String> errors) throws Exception {
        return JSON.wrap(render(model, errors));
    }

    /**
     * @return a {@link JSONArray} of the results
     */
    public abstract JSONArray render(Map<String, Object> model, List<String> errors) throws Exception;
    
    public static JSONArrayView combine(final JSONArrayView... views) {
        return new JSONArrayView() {
            @Override
            public JSONArray render(Map<String, Object> model, List<String> errors) throws Exception {
                JSONArray combined = new JSONArray();
                for (JSONArrayView view : views) {
                    JSONArray array = view.render(model, errors);
                    for (int i = 0; i < array.length(); i++) {
                        combined.put(array.get(i));
                    }
                }
                
                return combined;
            }            
        };
    }
    
    public static JSONArrayView of(final JSONArray array) {
        return JSONArrayView.of(array, new String[0]);        
    }
    
    public static JSONArrayView of(final JSONArray array, final String... error) {
        return JSONArrayView.of(array, Arrays.asList(error));
    }
    
    public static JSONArrayView of(final JSONArray array, final Iterable<String> error) {
        return new JSONArrayView() {
            @Override
            public JSONArray render(Map<String, Object> model, List<String> errors) throws Exception {
                for (String err : error) {
                    errors.add(err);
                }
                
                return array;
            }            
        };
    }
    
    public static JSONArrayView errors(final String... error) {
        return JSONArrayView.errors(Arrays.asList(error));
    }
    
    public static JSONArrayView errors(final Iterable<String> error) {
        return JSONArrayView.of(new JSONArray(), error);
    }

}
