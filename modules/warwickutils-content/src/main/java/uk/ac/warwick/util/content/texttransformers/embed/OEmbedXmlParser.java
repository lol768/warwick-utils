package uk.ac.warwick.util.content.texttransformers.embed;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class OEmbedXmlParser implements OEmbedParser {
	private final JAXBContext jaxbContext;
	
	public OEmbedXmlParser() {
		try {
			this.jaxbContext = JAXBContext.newInstance(OEmbedResponse.class);
		} catch(JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	

	@Override
	public OEmbedResponse unmarshal(InputStream httpResponse) throws OEmbedException {
		try {
			return (OEmbedResponse) jaxbContext.createUnmarshaller().unmarshal(httpResponse);
		} catch (JAXBException e) {
			throw new OEmbedException(e);
		}
	}


	@Override
	public String marshal(OEmbedResponse oembedResponse) throws OEmbedException {
		try {
			final StringWriter out = new StringWriter();
			jaxbContext.createMarshaller().marshal(oembedResponse, out);
			out.flush();
			out.close();
			return out.toString();
		} catch (Exception e) {
			throw new OEmbedException(e);
		}
	}

	@Override
	public void marshal(OEmbedResponse oembedResponse, OutputStream outputStream) throws OEmbedException {
		try {
			jaxbContext.createMarshaller().marshal(oembedResponse, outputStream);
		} catch (Exception e) {
			throw new OEmbedException(e);
		}
	}
}