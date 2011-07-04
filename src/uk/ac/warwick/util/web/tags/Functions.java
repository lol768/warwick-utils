package uk.ac.warwick.util.web.tags;

import java.util.Collection;

import org.apache.commons.lang.StringEscapeUtils;

import uk.ac.warwick.util.core.StringUtils;


/**
 * Collection of generic JSTL functions.
 */
public class Functions {
	private Functions() {}
	
    public static String js(final String text) {
        return StringEscapeUtils.escapeJavaScript(text);
    }
    
    public static String html(final String text) {
        return StringEscapeUtils.escapeHtml(text);
    }
    
    public static String specialHtml(final String text) {
        return StringUtils.htmlEscapeSpecialCharacters(text);
    }
    
    public static String join(Collection<String> collection, String delimiter) {
        return StringUtils.join(collection, delimiter);
    }
}
