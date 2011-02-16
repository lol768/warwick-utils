package uk.ac.warwick.util.web.view;

import java.util.Map;

import com.sun.syndication.feed.rss.Channel;

public abstract class RSSView extends AbstractXMLAggregationView<Channel> {
    
    private static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";
    
    private static final String RSS_CONTENT_TYPE = "application/rss+xml";

    public abstract Channel getFeed(Map<String, Object> model) throws Exception;

    public String getContentType() {
        return RSS_CONTENT_TYPE;
    }
    
    public String getCharacterEncoding() {
        return DEFAULT_CHARACTER_ENCODING;
    }
    
    public static final AbstractXMLAggregationView<Channel> of(final Channel feed) {
        return AbstractXMLAggregationView.of(feed, RSS_CONTENT_TYPE, DEFAULT_CHARACTER_ENCODING);
    }

}
