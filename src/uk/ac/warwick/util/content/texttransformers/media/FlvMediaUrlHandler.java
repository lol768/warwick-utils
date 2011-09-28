package uk.ac.warwick.util.content.texttransformers.media;

import java.util.HashMap;
import java.util.Map;

import uk.ac.warwick.util.content.MutableContent;

public final class FlvMediaUrlHandler extends AbstractMetadataAwareMediaUrlHandler {
	
	private final String playerLocation;
	
	private final String newPlayerLocation;
	
	public FlvMediaUrlHandler(String playerLocation, String newPlayerLocation) {
		this.playerLocation = playerLocation;
		this.newPlayerLocation = newPlayerLocation;
		
		if (playerLocation == null && newPlayerLocation == null) {
			throw new IllegalStateException("FLV Player location must be set");
		}
	}

    /* An extension matching one within, so that I can use its powers
     * but also do my own checks for rmtp:// protocols.
     */
    private MediaUrlHandler delegate = new AbstractExtensionMatchingMediaUrlHandler() {
        protected String[] getSupportedExtensions() {
            return new String[] { "flv", "f4v", "f4p", "mp4", "m4v" };
        }
        public String getHtml(final String url, final Map<String, Object> parameters, MutableContent mc) {
            throw new UnsupportedOperationException();
        }
    };
    
    public boolean recognises(final String url) {
        return (delegate.recognises(url) || url.startsWith("rtmp://"));
    }
    
    public String getHtmlInner(final String url, final Map<String,Object> parameters) {
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("url", url);
        
        if (newPlayerLocation != null && !url.startsWith("rtmp://")) {
        	model.put("newPlayer", true);
        	model.put("playerLocation", newPlayerLocation);
        } else {
        	model.put("newPlayer", false);
        	model.put("playerLocation", playerLocation);
        }
        
        model.putAll(parameters);
        if (!parameters.containsKey("previewimage")) {
            model.put("previewimage", "");
        }
        
        if (!parameters.containsKey("stretching")) {
            model.put("stretching", "fill");
        }
        return renderTemplate("media/flv.ftl", model);
    }
}
