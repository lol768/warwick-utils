package uk.ac.warwick.util.content.texttransformers.media;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

public final class FlvMediaUrlHandler extends MediaUrlHandler {
	
	private final String playerLocation;
	
	public FlvMediaUrlHandler(String playerLocation) {
		this.playerLocation = playerLocation;
		
		Assert.notNull(playerLocation, "FLV Player location must be set");
	}

    /* An extension matching one within, so that I can use its powers
     * but also do my own checks for rmtp:// protocols.
     */
    private MediaUrlHandler delegate = new AbstractExtensionMatchingMediaUrlHandler() {
        protected String[] getSupportedExtensions() {
            return new String[] { "flv", "f4v", "f4p", "mp4", "m4v" };
        }
        public String getHtml(final String url, final Map<String, Object> parameters) {
            throw new UnsupportedOperationException();
        }
    };
    
    @Override
    public boolean recognises(final String url) {
        return (delegate.recognises(url) || url.startsWith("rmtp://"));
    }
    
    @Override
    public String getHtml(final String url, final Map<String,Object> parameters) {
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("url", url);
        model.put("playerLocation", playerLocation);
        model.putAll(parameters);
        if (!parameters.containsKey("previewimage")) {
            model.put("previewimage", "");
        }
        return renderTemplate("media/flv.ftl", model);
    }
}
