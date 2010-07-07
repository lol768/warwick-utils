package uk.ac.warwick.util.web.view.json;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public abstract class ModelLessJSONView extends JSONView {

    @Override
    public final JSONObject render(Map<String, Object> model, List<String> errors) throws Exception {
        return render(errors);
    }
    
    public abstract JSONObject render(List<String> errors) throws Exception;

}
