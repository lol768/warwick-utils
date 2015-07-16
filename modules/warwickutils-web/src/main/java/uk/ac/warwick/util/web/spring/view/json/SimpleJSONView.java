package uk.ac.warwick.util.web.spring.view.json;

import java.util.List;

import org.json.JSONObject;

public final class SimpleJSONView extends ModelLessJSONView {

    private final JSONObject object;
    
    public SimpleJSONView(JSONObject renderMe) {
        object = renderMe;
    }
    
    @Override
    public JSONObject render(List<String> errors) throws Exception {
        return object;
    }

}
