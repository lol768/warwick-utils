package uk.ac.warwick.util.httpclient;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import uk.ac.warwick.userlookup.User;
import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.UriBuilder;

import java.util.List;
import java.util.Map.Entry;

public class WarwickTagUrlMangler {

    /**
     * Only looks for the unencoded form
     * @deprecated Use substituteWarwickTags and specify allowToken
     */
    public final String substituteWarwickTags(String stringToSubstitute, final User user) {
        return substituteWarwickTags(stringToSubstitute, user, true);
    }

    public final String substituteWarwickTags(String stringToSubstitute, final User user, boolean allowToken) {
        stringToSubstitute = doStringReplace(stringToSubstitute, "<warwick_username/>", user.getFullName());
        stringToSubstitute = doStringReplace(stringToSubstitute, "<warwick_username/>", user.getFullName());
        stringToSubstitute = doStringReplace(stringToSubstitute, "<warwick_userid/>", user.getUserId());
        stringToSubstitute = doStringReplace(stringToSubstitute, "<warwick_useremail/>", user.getEmail());

        if (allowToken) {
            stringToSubstitute = doStringReplace(stringToSubstitute, "<warwick_token/>", user.getOldWarwickSSOToken());
        }

        stringToSubstitute = doStringReplace(stringToSubstitute, "<warwick_idnumber/>", user.getWarwickId());
        stringToSubstitute = doStringReplace(stringToSubstitute, "<warwick_deptcode/>", user.getDepartmentCode());
        return stringToSubstitute;
    }
    
    private String doStringReplace(String string, String token, String value) {
        string = replace(string, token, value);
        string = replace(string, token.replace("<", "%3C").replace(">", "%3E").replace("/", "%2F"), value);
        string = replace(string, token.replace("<", "%3C").replace(">", "%3E"), value);
        
        return string;
    }

    /**
     * This will url-encode unencoded non-query string aware characters, so
     * don't just Uri.parse() any old damn string
     */
    public final Uri substituteWarwickTags(final Uri urlToSubstitute, final User user, boolean allowToken) {
        return substituteWarwickTags(new UriBuilder(urlToSubstitute), user, allowToken).toUri();
    }

    /**
     * @deprecated Use overload with `allowToken` param
     */
    public final Uri substituteWarwickTags(final Uri urlToSubstitute, final User user) {
        return substituteWarwickTags(urlToSubstitute, user, true);
    }

    /**
     * This will url-encode unencoded non-query string aware characters, so
     * don't just Uri.parse() any old damn string
     */
    public final UriBuilder substituteWarwickTags(final UriBuilder builder, final User user, boolean allowToken) {
        replaceAndEncode(builder, "<warwick_username/>", user.getFullName());
        replaceAndEncode(builder, "<warwick_userid/>", user.getUserId());
        replaceAndEncode(builder, "<warwick_useremail/>", user.getEmail());

        if (allowToken) {
            replaceAndEncode(builder, "<warwick_token/>", user.getOldWarwickSSOToken());
        }

        replaceAndEncode(builder, "<warwick_idnumber/>", user.getWarwickId());
        replaceAndEncode(builder, "<warwick_deptcode/>", user.getDepartmentCode());

        return builder;
    }

    private void replaceAndEncode(final UriBuilder builder, final String token, final String value) {
        // Because of encoding issues, this may be un-encoded, url encoded (for
        // the query), or partially url encoded (for a part of the path)
        replaceAndEncodeToken(builder, token, value);
        replaceAndEncodeToken(builder, token.replace("<", "%3C").replace(">", "%3E").replace("/", "%2F"), value);
        replaceAndEncodeToken(builder, token.replace("<", "%3C").replace(">", "%3E"), value);
    }
    
    private void replaceAndEncodeToken(final UriBuilder builder, final String token, final String value) {
        String path = builder.getPath();
        if (path.indexOf(token) != -1) {
            builder.setPath(replace(path, token, value));
        }
        
        for (Entry<String, List<String>> param : Sets.newHashSet(builder.getQueryParameters().entrySet())) {
            boolean hasChanged = false;
            List<String> newValues = Lists.newArrayList();
            for (String paramValue : param.getValue()) {
                if (paramValue.indexOf(token) != -1) {
                    hasChanged = true;
                    newValues.add(replace(paramValue, token, value));
                } else {
                    newValues.add(paramValue);
                }
            }
            
            if (hasChanged) {
                builder.putQueryParameter(param.getKey(), newValues);
            }
            
            if (param.getKey().indexOf(token) != -1) {
                String newKey = replace(param.getKey(), token, value);

                builder.getQueryParameters().remove(param.getKey());
                
                for (String v : newValues) {
                    builder.addQueryParameter(newKey, v);
                }
            }
        }
    }

    private String replace(final String str, final String token, final String value) {
        return str.replace(token, StringUtils.hasLength(value) ? value : "");
    }
}
