package uk.ac.warwick.util.content.texttransformers.embed;

import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

public class OEmbedJsonParser implements OEmbedParser {
	private final ObjectMapper objectMapper;
	
	public OEmbedJsonParser() {
		this.objectMapper = new ObjectMapper();
		final AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
		   
		objectMapper.setDeserializationConfig(
				objectMapper.getDeserializationConfig()
					.withAnnotationIntrospector(introspector)
					.without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES)
		);
		objectMapper.setSerializationConfig(objectMapper.getSerializationConfig().withAnnotationIntrospector(introspector));
	}
	
	public OEmbedResponse unmarshal(InputStream httpResponse) throws OEmbedException {
		try {			
			return objectMapper.readValue(httpResponse, OEmbedResponse.class);
		} catch(Exception e) {
			throw new OEmbedException(e);
		}
	}

	public String marshal(OEmbedResponse oembedResponse) throws OEmbedException {
		try {
			return objectMapper.writeValueAsString(oembedResponse);
		} catch (Exception e) {
			throw new OEmbedException(e);
		}
	}

	public void marshal(OEmbedResponse oembedResponse, OutputStream outputStream) throws OEmbedException {
		try {
			this.objectMapper.writeValue(outputStream, oembedResponse);
		} catch (Exception e) {
			throw new OEmbedException(e);
		}
	}
}