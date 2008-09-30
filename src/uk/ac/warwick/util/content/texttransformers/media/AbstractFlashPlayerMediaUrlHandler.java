package uk.ac.warwick.util.content.texttransformers.media;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public abstract class AbstractFlashPlayerMediaUrlHandler extends MediaUrlHandler {
    
    /**
     * This method should only be called if recognises() returns true for the URL,
     * otherwise it is allowed to throw runtime exceptions.
     * @return the address of the Flash animation that will play the video.
     */
    public abstract String getFlashUrl(String url);
    
    protected final URL toURL(final String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL was malformed",e);
        }
    }
    
    public final String getHtml(final String url, final Map<String,Object> parameters) {
        String flashUrl = getFlashUrl(url);
        if (flashUrl != null) {
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("url", flashUrl);
            
            model.putAll(parameters);
            return renderTemplate("media/flashvideo.ftl", model);
        } else {
            throw new IllegalStateException("This Media URL handler doesn't recognise the given URL");
        }
    }

    protected final String extractVideoId(final String theUrlQuery, final String paramName) {
        String urlQuery = theUrlQuery.replace("&amp;", "&");
        StringTokenizer tokenizer = new StringTokenizer(urlQuery, "&=");
        while (tokenizer.hasMoreTokens()) {
            if (tokenizer.nextToken().toLowerCase().equals(paramName.toLowerCase())) {
                return tokenizer.nextToken();
            }
        }
        throw new IllegalArgumentException("urlQuery has no video ID in it");
    }
}
