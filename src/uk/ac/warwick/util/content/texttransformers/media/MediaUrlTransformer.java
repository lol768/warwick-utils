package uk.ac.warwick.util.content.texttransformers.media;

import java.util.Map;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import uk.ac.warwick.util.content.texttransformers.AbstractSquareTagTransformer;
import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer;
import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer.Callback;
import uk.ac.warwick.util.core.HttpUtils;

public final class MediaUrlTransformer extends AbstractSquareTagTransformer  {
    
    static final String[] ALLOWED_PARAMETERS = new String[] {"height", "width", "type", "previewimage", "align", "download", "altplayer", "fallback", "fallbackVersion", "stretching"};
    private static final Logger LOGGER = Logger.getLogger(MediaUrlTransformer.class);
    
    private Map<String, MediaUrlHandler> handlers;
    
    public MediaUrlTransformer(final Map<String, MediaUrlHandler> theHandlers) {
        super("media");
        handlers = theHandlers;
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

            /**
             * Alter entered address, adding http:// if it looks like
             * it's a full web address.
             */
            private String normaliseAddress(final String enteredAddress) {
                String normalisedAddress = enteredAddress;
                if (HttpUtils.isAbsoluteAddress(normalisedAddress) &&
                        !normalisedAddress.toLowerCase().startsWith("http://")) {
                    normalisedAddress = "http://" + normalisedAddress;
                }
                return normalisedAddress;
            }
        };
    }        
}