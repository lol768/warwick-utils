package uk.ac.warwick.util.web.view.json;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Lists;

public final class CompositeJSONPRequestValidator implements JSONPRequestValidator {
    
    private final Iterable<JSONPRequestValidator> validators;
    
    public CompositeJSONPRequestValidator(JSONPRequestValidator... validators) {
        this.validators = Lists.newArrayList(validators);
    }

    public boolean isAllow(HttpServletRequest request) {
        for (JSONPRequestValidator validator : validators) {
            if (validator.isAllow(request)) {
                return true;
            }
        }
        
        return false;
    }

}
