package uk.ac.warwick.util.content.texttransformers.media;

import java.util.Map;

public abstract class AbstractMetadataAwareMediaUrlHandler extends MediaUrlHandler {
	
	private MetadataHandler metadataHandler;

	@Override
	public final String getHtml(String url, Map<String, Object> parameters) {
		if (metadataHandler != null) {
			metadataHandler.handle(url, parameters);
		}
		
		// sanitise width and height parameters to contain only numbers. if this
		// ends up as empty string then the defaults will be used in the
		// freemarker
		if (parameters.containsKey("width")) {
			parameters.put("width", parameters.get("width").toString().replaceAll("[^0-9]+", ""));
		}
		
		if (parameters.containsKey("height")) {
			parameters.put("height", parameters.get("height").toString().replaceAll("[^0-9]+", ""));
		}
		
		return getHtmlInner(url, parameters);
	}
	
	/**
	 * To be implemented
	 */
	abstract String getHtmlInner(String url, Map<String, Object> parameters);

	public MetadataHandler getMetadataHandler() {
		return metadataHandler;
	}

	public void setMetadataHandler(MetadataHandler metadataHandler) {
		this.metadataHandler = metadataHandler;
	}

}
