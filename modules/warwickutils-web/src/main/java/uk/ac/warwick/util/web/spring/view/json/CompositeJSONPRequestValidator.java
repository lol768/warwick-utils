package uk.ac.warwick.util.web.spring.view.json;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableList;

public final class CompositeJSONPRequestValidator implements JSONPRequestValidator {
    
    private final Iterable<JSONPRequestValidator> validators;
    
    public CompositeJSONPRequestValidator(JSONPRequestValidator... validators) {
        this(ImmutableList.copyOf(validators));
    }
    
    public CompositeJSONPRequestValidator(Iterable<JSONPRequestValidator> validators) {
        this.validators = validators;
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
