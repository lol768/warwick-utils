package uk.ac.warwick.util.content.texttransformers.media;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import uk.ac.warwick.util.content.textile2.Textile2;
import uk.ac.warwick.util.content.textile2.TransformationOptions;
import uk.ac.warwick.util.core.StringUtils;

public final class AudioMediaUrlHandler extends AbstractExtensionMatchingMediaUrlHandler {
	
	private final String playerLocation;
	
	private final String alternatePlayerLocation;
	
	private final EnumSet<TransformationOptions> options;
	
	public AudioMediaUrlHandler(String wimpyPlayerLocation, String onePixelOutPlayerLocation) {
		this(wimpyPlayerLocation, onePixelOutPlayerLocation, Textile2.DEFAULT_OPTIONS);
	}
	
	public AudioMediaUrlHandler(String wimpyPlayerLocation, String onePixelOutPlayerLocation, EnumSet<TransformationOptions> options) {
		this.playerLocation = wimpyPlayerLocation;
		this.alternatePlayerLocation = onePixelOutPlayerLocation;
		this.options = options;
		
		if (playerLocation == null && alternatePlayerLocation == null) {
			throw new IllegalStateException("Either Wimpy player location or one pixel out player location must be set");
		} else if (options.contains(TransformationOptions.alwaysUseAlternativeMp3Player) && alternatePlayerLocation == null) {
			throw new IllegalStateException("Must set one-pixel-out player location, because alwaysUseAlternativeMp3Player option has been set");
		}
	}

    protected String[] getSupportedExtensions() {
        return new String[] { "mp3" };
    }

    public String getHtml(final String url, final Map<String,Object> parameters) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("url", url);
        
        if (!options.contains(TransformationOptions.alwaysUseAlternativeMp3Player)) {
        	model.put("playerLocation", playerLocation);
        }
        
        model.put("alternatePlayerLocation", alternatePlayerLocation);
        model.putAll(parameters);
        
        if (options.contains(TransformationOptions.alwaysUseAlternativeMp3Player)) {
        	model.put("altplayer", "true");
        } else if (!StringUtils.hasText(playerLocation)) {
        	model.put("altplayer", "true");
        } else if (!StringUtils.hasText(alternatePlayerLocation)) {
        	model.put("altplayer", "false");
        }
        
        return renderTemplate("media/flashaudio.ftl", model);
    }
}
