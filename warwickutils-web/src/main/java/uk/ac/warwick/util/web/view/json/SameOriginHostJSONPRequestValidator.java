package uk.ac.warwick.util.web.view.json;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import uk.ac.warwick.util.web.Uri;

public final class SameOriginHostJSONPRequestValidator implements JSONPRequestValidator {
    
    private final String host;
    
    private boolean validByDefault = true;
    
    public SameOriginHostJSONPRequestValidator(String host) {
        if (host.indexOf("://") != -1) {
            this.host = Uri.parse(host).getAuthority();
        } else {
            this.host = host;
        }
    }

    public boolean isAllow(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (StringUtils.hasText(referer)) {
            return Uri.parse(referer).getAuthority().equals(host);
        }
        
        // No referer - by default, request is valid.
        return validByDefault;
    }

    public void setValidByDefault(boolean validByDefault) {
        this.validByDefault = validByDefault;
    }

}
