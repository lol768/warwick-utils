package uk.ac.warwick.util.web.view.json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@Configurable
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
        return new CombinedJSONArrayView(views);
    }
    
    public static JSONArrayView of(final JSONArray array, final String charEncoding, final JSONPRequestValidator jsonPValidator) {
        return JSONArrayView.of(array, charEncoding, jsonPValidator, new String[0]);        
    }
    
    public static JSONArrayView of(final JSONArray array, final String charEncoding, final JSONPRequestValidator jsonPValidator, final String... error) {
        return JSONArrayView.of(array, charEncoding, jsonPValidator, Arrays.asList(error));
    }
    
    public static JSONArrayView of(final JSONArray array, final String charEncoding, final JSONPRequestValidator jsonPValidator, final Iterable<String> error) {
        return new PreconfiguredJSONArrayView(array, charEncoding, error, jsonPValidator);
    }
    
    public static JSONArrayView errors(final String charEncoding, final JSONPRequestValidator jsonPValidator, final String... error) {
        return JSONArrayView.errors(charEncoding, jsonPValidator, Arrays.asList(error));
    }
    
    public static JSONArrayView errors(final String charEncoding, final JSONPRequestValidator jsonPValidator, final Iterable<String> error) {
        return JSONArrayView.of(new JSONArray(), charEncoding, jsonPValidator, error);
    }
    
    @Configurable
    public static class PreconfiguredJSONArrayView extends JSONArrayView {
        
        private final JSONArray array;
        private final Iterable<String> errs;
        private final String characterEncoding;
        
        public PreconfiguredJSONArrayView(JSONArray arr, String charEncoding, Iterable<String> err, JSONPRequestValidator jsonPValidator) {
            this.array = arr;
            this.errs = err;
            this.characterEncoding = charEncoding;
            
            setJsonpRequestValidator(jsonPValidator);
        }
        
        @Override
        public JSONArray render(Map<String, Object> model, List<String> errors) throws Exception {
            for (String err : errs) {
                errors.add(err);
            }
            
            return array;
        }

        @Override
        public String getCharacterEncoding() {
            return characterEncoding;
        }
        
    }
    
    @Configurable
    public static class CombinedJSONArrayView extends JSONArrayView {
        
        private final Iterable<JSONArrayView> views;
        
        public CombinedJSONArrayView(JSONArrayView... theViews) {
            this(ImmutableList.copyOf(theViews));
        }
        
        public CombinedJSONArrayView(Iterable<JSONArrayView> theViews) {
            this.views = theViews;
            
            setJsonpRequestValidator(new CompositeJSONPRequestValidator(Iterables.transform(theViews, new Function<JSONArrayView, JSONPRequestValidator>() {
                public JSONPRequestValidator apply(JSONArrayView view) {
                    return view.getJsonpRequestValidator();
                }
            })));
        }
        
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

        @Override
        public String getCharacterEncoding() {
            return views.iterator().next().getCharacterEncoding();
        }
        
    }

}
