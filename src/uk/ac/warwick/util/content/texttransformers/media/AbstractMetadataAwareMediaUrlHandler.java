package uk.ac.warwick.util.content.texttransformers.media;

import java.util.Map;

public abstract class AbstractMetadataAwareMediaUrlHandler extends MediaUrlHandler {
	
	private MetadataHandler metadataHandler;

	@Override
	public final String getHtml(String url, Map<String, Object> parameters) {
		if (metadataHandler != null) {
			metadataHandler.handle(url, parameters);
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
