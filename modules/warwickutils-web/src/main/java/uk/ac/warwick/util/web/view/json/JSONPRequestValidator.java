package uk.ac.warwick.util.web.view.json;

import javax.servlet.http.HttpServletRequest;

/**
 * A service to verify whether a JSONP request is valid.
 * 
 * @author Mat
 */
public interface JSONPRequestValidator {

    JSONPRequestValidator REJECT_ALL = new JSONPRequestValidator() {
        public boolean isAllow(HttpServletRequest request) {
            return false;
        }
    };

    /**
     * WARNING: This is EXTREMELY dangerous - use
     * {@link SameOriginHostJSONPRequestValidator} with a default of true.
     */
    JSONPRequestValidator ALLOW_ALL = new JSONPRequestValidator() {
        public boolean isAllow(HttpServletRequest request) {
            return true;
        }
    };

    boolean isAllow(HttpServletRequest request);

}
