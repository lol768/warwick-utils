package uk.ac.warwick.util.httpclient;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import uk.ac.warwick.userlookup.User;
import uk.ac.warwick.util.core.StringUtils;

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
    
    public final URI substituteWarwickTags(final URI uri, final User user) {
        return URI.create(substituteWarwickTags(uri.toString(), user));
    }
    
    private String replaceAndEncode(final String str, final String token, final String value, final String encoding) {
        // Because of encoding issues, this may be un-encoded, url encoded (for the query), or partially url encoded (for a part of the path)
        String replaced = replaceAndEncodeToken(str, token, value, "UTF-8");
        replaced = replaceAndEncodeToken(replaced, token.replace("<", "%3C").replace(">", "%3E").replace("/", "%2F"), value, "UTF-8");
        replaced = replaceAndEncodeToken(replaced, token.replace("<", "%3C").replace(">", "%3E"), value, "UTF-8");
        return replaced;
    }
    
    private String replaceAndEncodeToken(final String str, final String token, final String value, final String encoding) {
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
