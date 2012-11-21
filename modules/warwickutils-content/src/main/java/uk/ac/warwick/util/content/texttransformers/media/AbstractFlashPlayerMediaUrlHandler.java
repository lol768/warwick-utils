package uk.ac.warwick.util.content.texttransformers.media;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.web.Uri;

public abstract class AbstractFlashPlayerMediaUrlHandler extends MediaUrlHandler {
    
    /**
     * This method should only be called if recognises() returns true for the URL,
     * otherwise it is allowed to throw runtime exceptions.
     * @return the address of the Flash animation that will play the video.
     */
    public abstract String getFlashUrl(String url);
    
    protected final Uri toUri(final String url) {
        return Uri.parse(url);
    }
    
    public String getHtml(final String url, final Map<String,Object> parameters, MutableContent mc) {
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