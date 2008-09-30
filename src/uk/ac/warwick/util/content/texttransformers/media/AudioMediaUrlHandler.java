package uk.ac.warwick.util.content.texttransformers.media;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public final class AudioMediaUrlHandler extends AbstractExtensionMatchingMediaUrlHandler {
	
	private final String playerLocation;
	
	private final String alternatePlayerLocation;
	
	public AudioMediaUrlHandler(String wimpyPlayerLocation, String onePixelOutPlayerLocation) {
		this.playerLocation = wimpyPlayerLocation;
		this.alternatePlayerLocation = onePixelOutPlayerLocation;
		
		Assert.isTrue(playerLocation != null || alternatePlayerLocation != null, "Either Wimpy player location or one pixel out player location must be set");
	}

    protected String[] getSupportedExtensions() {
        return new String[] { "mp3" };
    }

    public String getHtml(final String url, final Map<String,Object> parameters) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("url", url);
        model.put("playerLocation", playerLocation);
        model.put("alternatePlayerLocation", alternatePlayerLocation);
        model.putAll(parameters);
        
        if (!StringUtils.hasText(playerLocation)) {
        	model.put("altplayer", "true");
        } else if (!StringUtils.hasText(alternatePlayerLocation)) {
        	model.put("altplayer", "false");
        }
        
        return renderTemplate("media/flashaudio.ftl", model);
    }
}
