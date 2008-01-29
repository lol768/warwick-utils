package uk.ac.warwick.util.httpclient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.util.StringUtils;

import uk.ac.warwick.userlookup.User;

public class WarwickTagUrlMangler {

    public final String substituteWarwickTags(final String urlToSubstitute, final User user) {
           String newUrl = urlToSubstitute;

            newUrl = replaceAndEncode(newUrl, "<warwick_username/>", user.getFullName(), "UTF-8");
            newUrl = replaceAndEncode(newUrl, "<warwick_userid/>", user.getUserId(), "UTF-8");
            newUrl = replaceAndEncode(newUrl, "<warwick_useremail/>", user.getEmail(), "UTF-8");
            newUrl = replaceAndEncode(newUrl, "<warwick_token/>", user.getOldWarwickSSOToken(), "UTF-8");
            newUrl = replaceAndEncode(newUrl, "<warwick_idnumber/>", user.getWarwickId(), "UTF-8");
            newUrl = replaceAndEncode(newUrl, "<warwick_deptcode/>", user.getDepartmentCode(),"UTF-8");

        return newUrl;
    }
    
    private String replaceAndEncode(final String str, final String token, final String value, final String encoding) {
        try {
            if (StringUtils.hasLength(value)) {
                return str.replaceAll(token, URLEncoder.encode(value, encoding));
            }
            return str.replaceAll(token, "");

        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Cannot encode " + value + " for token " + token);
        }
    }
}
