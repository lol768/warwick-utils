package uk.ac.warwick.util.web.tags;

import org.apache.commons.lang.StringEscapeUtils;


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
}
