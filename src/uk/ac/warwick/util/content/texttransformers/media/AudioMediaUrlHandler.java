package uk.ac.warwick.util.content.texttransformers.media;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import freemarker.template.utility.StringUtil;

import uk.ac.warwick.util.content.textile2.Textile2;
import uk.ac.warwick.util.content.textile2.TransformationOptions;
import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.core.spring.FileUtils;

public final class AudioMediaUrlHandler extends AbstractExtensionMatchingMediaUrlHandler {
	
    private static final Pattern NUMBERS = Pattern.compile("[0-9]+");
    
	private final String playerLocation;
	
	private final String alternatePlayerLocation;
	
	private final EnumSet<TransformationOptions> options;
	
	private Map<String,String> extensionMimeMap;
	
	public AudioMediaUrlHandler(String wimpyPlayerLocation, String onePixelOutPlayerLocation) {
		this(wimpyPlayerLocation, onePixelOutPlayerLocation, Textile2.DEFAULT_OPTIONS);
	}
	
	// Spring can't create enumsets easily
	public AudioMediaUrlHandler(String wimpyPlayerLocation, String onePixelOutPlayerLocation, Set<TransformationOptions> options) {
	    this(wimpyPlayerLocation, onePixelOutPlayerLocation, EnumSet.copyOf(options));
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
		
		// Must have a mapping for everything in #getSupportedExtensions();
		extensionMimeMap = new HashMap<String, String>();
		extensionMimeMap.put("mp3", "audio/mpeg");
		extensionMimeMap.put("ogg", "audio/ogg");
	}

    protected String[] getSupportedExtensions() {
        return new String[] { "mp3" };
    }

    public String getHtml(final String url, final Map<String,Object> parameters) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("url", url);
        
        String urlExtension = FileUtils.getLowerCaseExtension(url);
        model.put("mimeType", extensionMimeMap.get(urlExtension));
        
        if (!options.contains(TransformationOptions.alwaysUseAlternativeMp3Player)) {
        	model.put("playerLocation", playerLocation);
        }
        
        model.put("useNativeElements", this.options.contains(TransformationOptions.useNativeAudioVideoElements));
        
        model.put("alternatePlayerLocation", alternatePlayerLocation);
        
        
        model.putAll(parameters);
        if (invalidNumber(model.get("width"))) {
            model.remove("width");
        }
        if (invalidNumber(model.get("height"))) {
            model.remove("height");
        }
        
        if (options.contains(TransformationOptions.alwaysUseAlternativeMp3Player)) {
        	model.put("altplayer", "true");
        } else if (!StringUtils.hasText(playerLocation)) {
        	model.put("altplayer", "true");
        } else if (!StringUtils.hasText(alternatePlayerLocation)) {
        	model.put("altplayer", "false");
        } //else...?
        
        return renderTemplate("media/audio.ftl", model);
    }

    private boolean invalidNumber(Object object) {
        if (object == null) {
            return false;
        }
        return !NUMBERS.matcher((String)object).matches();
    }
}
