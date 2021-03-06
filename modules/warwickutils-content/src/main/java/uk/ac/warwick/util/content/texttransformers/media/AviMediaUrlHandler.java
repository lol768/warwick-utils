package uk.ac.warwick.util.content.texttransformers.media;

import java.util.HashMap;
import java.util.Map;

import uk.ac.warwick.util.content.MutableContent;

public final class AviMediaUrlHandler extends AbstractMetadataAwareMediaUrlHandler {
	
	private final String defaultPreviewImageLocation;
	
	public AviMediaUrlHandler(String theDefaultPreviewImage) {
		this.defaultPreviewImageLocation = theDefaultPreviewImage;
		
		if (defaultPreviewImageLocation == null) {
			throw new IllegalStateException("Default preview image location must be set");
		}
	}

    private MediaUrlHandler delegate = new AbstractExtensionMatchingMediaUrlHandler() {
        protected String[] getSupportedExtensions() {
            return new String[] { "wmv", "avi", "wma" };
        }
        public String getHtml(final String url, final Map<String, Object> parameters, MutableContent mc) {
            throw new UnsupportedOperationException();
        }
    };
    
    public boolean recognises(final String url) {
        return delegate.recognises(url);
    }

    public String getHtmlInner(final String url, final Map<String,Object> parameters) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("url", url);
        model.putAll(parameters);
        if (!parameters.containsKey("previewimage")) {
            model.put("previewimage",defaultPreviewImageLocation);
        }
        return renderTemplate("media/windowsmedia.ftl", model);
    }
}
