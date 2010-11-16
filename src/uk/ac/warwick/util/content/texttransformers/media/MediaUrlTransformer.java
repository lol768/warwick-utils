package uk.ac.warwick.util.content.texttransformers.media;

import java.util.Map;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import uk.ac.warwick.util.content.texttransformers.AbstractSquareTagTransformer;
import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer;
import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer.Callback;

public final class MediaUrlTransformer extends AbstractSquareTagTransformer {
    
    static final String[] ALLOWED_PARAMETERS = new String[] {"height", "width", "type", "previewimage", "align", "download", "altplayer", "fallback", "fallbackVersion", "stretching", "title", "description"};
    private static final Logger LOGGER = Logger.getLogger(MediaUrlTransformer.class);
    
    private final Map<String, MediaUrlHandler> handlers;
    private final String closeButtonImgUrl;
    
    public MediaUrlTransformer(final Map<String, MediaUrlHandler> theHandlers, final String theCloseButtonImgUrl) {
        super("media");
        this.handlers = theHandlers;
        this.closeButtonImgUrl = theCloseButtonImgUrl;
    }
    
    protected String[] getAllowedParameters() {
        return ALLOWED_PARAMETERS;
    }

    @Override
    protected Callback getCallback() {
        return new TextPatternTransformer.Callback(){

            public String transform(final String input) {
                Matcher matcher = getTagPattern().matcher(input);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Failed to match media tag, but shouldn't be here if it didn't");
                }
                Map<String, Object> parameters = getParameters(matcher);
                String address = getContents(matcher);
                String result = input;
                
                //set default alignment
                if (!parameters.containsKey("align")) {
                    parameters.put("align","left");
                }
                
                parameters.put("random", new java.util.Random());
                parameters.put("closeButtonImgUrl", closeButtonImgUrl);
                
                // "type" overrides the content type
                if (parameters.containsKey("type")) {
                    String contentType = parameters.get("type").toString();
                    try {
                        MediaUrlHandler handler = handlers.get(contentType);
                        result = handler.getHtml(address, parameters);
                    } catch (Exception e) {
                        LOGGER.debug("Media URL with overridden type threw an exception: " + e.getMessage(),e);
                        /*
                         * If anything goes wrong it's invariably because either
                         * the content type is a made-up one or the content type
                         * chosen is nonsense for the URL supplied, so just
                         * leave it alone.
                         */
                    }
                } else {
                    for (MediaUrlHandler handler : handlers.values()) {
                        if (handler.recognises(address)) {
                            result = handler.getHtml(address, parameters);
                            break;
                        }
                    }
                }

                return result;
            }
        };
    }        
}