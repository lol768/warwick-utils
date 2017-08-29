package uk.ac.warwick.util.content;

import org.jsoup.nodes.Document;

public interface HtmlParser {
    Document parseDOM(String source) throws HtmlParsingException;
}
