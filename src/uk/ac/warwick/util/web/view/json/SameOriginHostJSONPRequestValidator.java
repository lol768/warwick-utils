package uk.ac.warwick.util.web.view.json;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

public final class SameOriginHostJSONPRequestValidator implements JSONPRequestValidator {
    
    private final String host;
    
    private boolean validByDefault = true;
    
    public SameOriginHostJSONPRequestValidator(String host) throws MalformedURLException {
        if (host.indexOf("://") != -1) {
            this.host = new URL(host).getHost();
        } else {
            this.host = host;
        }
    }

    public boolean isAllow(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (StringUtils.hasText(referer)) {
            try {
                return new URL(referer).getHost().equals(host);
            } catch (MalformedURLException e) {
                return false;
            }
        }
        
        // No referer - by default, request is valid.
        return validByDefault;
    }

    public void setValidByDefault(boolean validByDefault) {
        this.validByDefault = validByDefault;
    }

}
