package uk.ac.warwick.util.content.texttransformers.embed;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import java.io.InputStream;
import java.io.OutputStream;

public class OEmbedJsonParser implements OEmbedParser {
	private final ObjectMapper objectMapper;
	
	public OEmbedJsonParser() {
		this.objectMapper =	new ObjectMapper();

		final AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
		   
		objectMapper.setConfig(
				objectMapper.getDeserializationConfig()
						.with(introspector)
						.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		);
		objectMapper.setConfig(objectMapper.getSerializationConfig().with(introspector));
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