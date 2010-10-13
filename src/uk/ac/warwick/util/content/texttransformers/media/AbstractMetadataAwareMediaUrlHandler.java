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
			parameters.put("width", sanitise(parameters.get("width").toString()));
		}
		
		if (parameters.containsKey("height")) {
			parameters.put("height", sanitise(parameters.get("height").toString()));
		}
		
		return getHtmlInner(url, parameters);
	}
	
	private String sanitise(String dimension) {
	    dimension = dimension.trim();
	    if (dimension.equalsIgnoreCase("auto")) { return "auto"; }
	    
	    boolean percentage = false; 
	    if (dimension.endsWith("%")) {
	        percentage = true;
	        dimension = dimension.substring(0, dimension.length()-1);
	    }
	    
	    dimension = dimension.replaceAll("[^0-9]+", "");
	    return percentage ? dimension + "%" : dimension;
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
