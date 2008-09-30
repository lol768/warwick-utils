package uk.ac.warwick.util.content.cleaner;

import org.xml.sax.Attributes;

/**
 * Interface for writing HTML content into a string buffer. Ideal if you want to
 * rewrite stuff :)
 * 
 * @author Mat Mannion
 */
public interface HtmlContentWriter {
    
    String renderStartTag(String tagName, Attributes atts);
    
    String renderEndTag(String tagName);
    
    
    String htmlEscapeAll(String html);

    boolean isSelfCloser(String tagName);

	void setDelegate(HtmlContentWriter contentWriter);

}
