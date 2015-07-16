package uk.ac.warwick.util.web.spring.view.json;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public abstract class ModelLessJSONView extends JSONView {

    @Override
    public final JSONObject render(Map<String, Object> model, HttpServletRequest request, List<String> errors) throws Exception {
        return render(errors);
    }
    
    public abstract JSONObject render(List<String> errors) throws Exception;

}
