package uk.ac.warwick.util.content.texttransformers.embed;

import java.io.InputStream;
import java.io.OutputStream;

public interface OEmbedParser {

    /**
     * Unmarshals an OembedResponse from the given inputstream
     * 
     * @param httpResponse
     * @return
     * @throws OEmbedException
     */
    public OEmbedResponse unmarshal(final InputStream httpResponse) throws OEmbedException;

    /**
     * Marhsals the given OembedResponse to a string
     * 
     * @param oembedResponse
     * @return
     * @throws OEmbedException
     */
    public String marshal(final OEmbedResponse oembedResponse) throws OEmbedException;

    public void marshal(final OEmbedResponse oembedResponse, final OutputStream outputStream) throws OEmbedException;
    
}