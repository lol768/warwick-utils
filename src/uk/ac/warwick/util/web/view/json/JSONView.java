package uk.ac.warwick.util.web.view.json;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

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
        return new CombinedJSONView(views);
    }
    
    public static JSONView of(final JSONObject object, final JSONPRequestValidator jsonpValidator) {
        return JSONView.of(object, jsonpValidator, new String[0]);        
    }
    
    public static JSONView of(final JSONObject object, final JSONPRequestValidator jsonpValidator, final String... error) {
        return JSONView.of(object, jsonpValidator, Arrays.asList(error));
    }
    
    public static JSONView of(final JSONObject object, final JSONPRequestValidator jsonpValidator, final Iterable<String> error) {
        return new PreconfiguredJSONView(object, error, jsonpValidator);
    }
    
    public static JSONView errors(final JSONPRequestValidator jsonpValidator, final String... error) {
        return JSONView.errors(jsonpValidator, Arrays.asList(error));
    }
    
    public static JSONView errors(final JSONPRequestValidator jsonpValidator, final Iterable<String> error) {
        return JSONView.of(new JSONObject(), jsonpValidator, error);
    }
    
    @Configurable
    public static class PreconfiguredJSONView extends JSONView {
        
        private final JSONObject object;
        private final Iterable<String> errs;
        
        public PreconfiguredJSONView(JSONObject obj, Iterable<String> err, JSONPRequestValidator jsonPValidator) {
            this.object = obj;
            this.errs = err;
            
            setJsonpRequestValidator(jsonPValidator);
        }
        
        @Override
        public JSONObject render(Map<String, Object> model, List<String> errors) throws Exception {
            for (String err : errs) {
                errors.add(err);
            }
            
            return object;
        }
        
    }
    
    @Configurable
    public static class CombinedJSONView extends JSONView {
        
        private final Iterable<JSONView> views;
        
        public CombinedJSONView(JSONView... theViews) {
            this(ImmutableList.copyOf(theViews));
        }
        
        public CombinedJSONView(Iterable<JSONView> theViews) {
            this.views = theViews;
            
            setJsonpRequestValidator(new CompositeJSONPRequestValidator(Iterables.transform(theViews, new Function<JSONView, JSONPRequestValidator>() {
                public JSONPRequestValidator apply(JSONView view) {
                    return view.getJsonpRequestValidator();
                }
            })));
        }
        
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
        
    }

}
