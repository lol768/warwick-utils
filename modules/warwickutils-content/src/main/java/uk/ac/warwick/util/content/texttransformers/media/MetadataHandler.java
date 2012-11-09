package uk.ac.warwick.util.content.texttransformers.media;

import java.util.Map;

import uk.ac.warwick.util.content.MutableContent;

public interface MetadataHandler {

	void handle(String url, Map<String, Object> parameters, MutableContent mc);

}
