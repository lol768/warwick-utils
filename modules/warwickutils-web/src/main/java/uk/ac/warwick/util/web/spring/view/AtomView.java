package uk.ac.warwick.util.web.spring.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.rometools.rome.feed.atom.Feed;

public abstract class AtomView extends AbstractXMLAggregationView<Feed> {
    
    private static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";
    
    private static final String ATOM_CONTENT_TYPE = "application/atom+xml";

    public abstract Feed getFeed(Map<String, Object> model, HttpServletRequest request) throws Exception;

    public String getContentType() {
        return ATOM_CONTENT_TYPE;
    }
    
    public String getCharacterEncoding() {
        return DEFAULT_CHARACTER_ENCODING;
    }
    
    public static final AbstractXMLAggregationView<Feed> of(final Feed feed) {
        return AbstractXMLAggregationView.of(feed, ATOM_CONTENT_TYPE, DEFAULT_CHARACTER_ENCODING);
    }

}
