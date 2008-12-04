package uk.ac.warwick.util.content.texttransformers.media;

import java.util.Map;

public interface MetadataHandler {

	void handle(String url, Map<String, Object> parameters);

}
