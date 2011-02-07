package uk.ac.warwick.util.content;

import org.w3c.dom.Document;

public interface HtmlParser {
    Document parseDOM(String source) throws HtmlParsingException;
}
