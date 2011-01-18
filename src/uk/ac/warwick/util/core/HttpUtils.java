package uk.ac.warwick.util.core;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.StringTokenizer;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;

/**
 * Singleton convenience methods.
 *
 * @author xusqac
 */
public final class HttpUtils {
    private static final HttpUtils INSTANCE = new HttpUtils();

    private HttpUtils() { }

    public static HttpUtils getInstance() {
        return INSTANCE;
    }

    public static String appendGetParameter(final String url,
            final String paramName,
            final String paramValue) {
        String result = url;

//        // trim trailing slash.
//        if (result.charAt(result.length() - 1) == '/') {
//            result = result.substring(0, result.length() - 1);
//        }

        String paramString;
        try {
            /**
             * UTF-8 is the recommended encoding as per
             * http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars 
             */
            paramString = paramName + "=" + URLEncoder.encode(paramValue, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("this should never happen!", e);
        }
        if (result.indexOf("?") == -1) {
            result += "?" + paramString;
        } else {
            result += "&" + paramString;
        }

        return result;
    }
    
    /**
     * Retrieves an attribute from the request as a boolean. If it doesn't exist or is
     * null or is not a Boolean, then defaultValue is returned instead.
     */
    public static boolean getBooleanRequestAttribute(ServletRequest request, String attributeName, boolean defaultValue) {
        Object value = request.getAttribute(attributeName);
        Boolean result = defaultValue;
        if (value != null && value instanceof Boolean) {
            result = (Boolean)value; 
        }
        return result.booleanValue();
    }

    /**
     * Removes the trailing slash from the URL passed in, unless the URL
     * consists solely of a slash - in which case it is returned
     * unmodified.
     * @param url the {@link String} to trim.
     * @return the trimmed {@link String}.
     */
    public static String trimTrailingSlash(final String url) {
        int urlLength = url.length();
        if ( (!(urlLength == 1 && url.equals("/")))
             && urlLength > 0 && (url.charAt(urlLength - 1) == '/')) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * Does some parsing on a given address to check if it is
     * an absolute or a relative link. It's quite lenient as it
     * doesn't require http:// on absolute links, which makes
     * it useful for finding out whether http:// needs to be
     * prepended.
     */
    public static boolean isAbsoluteAddress(final String path) {
        boolean result;
        //no dots, can't have a domain in it
        if (path.indexOf("/") == -1 || path.indexOf(".") == -1) {
            result = false;
        } else if (path.toLowerCase().startsWith("http://")) {
            result = true;
        } else {
            String beforeSlash = path.substring(0, path.indexOf('/'));
            result = isDomain(beforeSlash);
        }
        return result;
    }
    
    private static boolean isDomain(final String domain) {
        StringTokenizer tokenizer = new StringTokenizer(domain, ".");
        return (tokenizer.countTokens() > 1);
    }
    
    /**
     * Gets a desired cookie from a list of cookies
     */
    public static Cookie getCookie(final Cookie[] cookies, final String name) {
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals(name)) {
                    return cookie;
                } 
            }
        }
        return null;
    }
    
    public static String utf8Encode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String utf8Decode(String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
