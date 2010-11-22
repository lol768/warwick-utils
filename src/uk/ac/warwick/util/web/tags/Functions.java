package uk.ac.warwick.util.web.tags;

import org.apache.commons.lang.StringEscapeUtils;

import uk.ac.warwick.util.core.StringUtils;


/**
 * Collection of generic JSTL functions, 
 * which don't have any particular dependencies.
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
}
