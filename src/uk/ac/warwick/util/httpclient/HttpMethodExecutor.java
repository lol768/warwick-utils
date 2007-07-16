package uk.ac.warwick.util.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.multipart.Part;

/**
 * A HttpMethodExecutor is a wrapper for the Apache HttpClient that sets a lot
 * of Warwick-specific behaviour and abstracts it away from the instances where
 * the client is used (URL backed content fetchers).
 * <p>
 * Most importantly, GUARANTEES closing the connection as soon as the response
 * is gotten.
 * 
 * @author Mat Mannion
 */
public interface HttpMethodExecutor extends Serializable {
	
	int DEFAULT_CONNECTION_TIMEOUT = 5000;
	int DEFAULT_RETRIEVAL_TIMEOUT = 5000;

    public enum Method {
        get, post, head
    };

    /**
     * Retrieves the content from the method as a byte array
     * 
     * @return String the contents
     * @throws HttpException
     *             when something nasty happened (subclass of IOException)
     * @throws IOException
     *             when there's been a timeout, usually
     */
    byte[] retrieveContents() throws IOException;

    /**
     * Retrieves the content from the method as a String
     * 
     * @return String the contents
     * @throws HttpException
     *             when something nasty happened (subclass of IOException)
     * @throws IOException
     *             when there's been a timeout, usually
     */
    String retrieveContentsAsString() throws IOException;

    /**
     * Retrieves the content from the method as an InputStream.
     * <p>
     * <strong>NOTE:</strong> Must call executor.close() in a finally {} block
     * once the input stream has been dealt with, else the http resource will be
     * left open.
     * 
     * @return InputStream the contents
     * @throws HttpException
     *             when something nasty happened (subclass of IOException)
     * @throws IOException
     *             when there's been a timeout, usually
     */
    InputStream retrieveContentsAsStream() throws IOException;

    /**
     * Executes the HTTP call - must be called before retrieveContents is
     * called.
     * 
     * @return int the status code
     * @throws HttpException
     *             when something nasty happened (subclass of IOException)
     * @throws IOException
     *             when there's been a timeout, usually
     */
    int execute() throws IOException;

    /**
     * Returns the last modified date that was returned when the method was
     * executed. Must be called AFTER execution, damnit, AFTER!
     * 
     * @return Date the date the remote content was last modified, or
     *         Page.NULL_LAST_CONTENT_CHANGED_DATE if last-modified header was
     *         not sent
     */
    Date getLastModifiedDate();

    /**
     * Returns the URL that the Http call is pointing to. Note that this may
     * differ following actual execution - it will not include any query string
     * and will also follow any redirects to the target page.
     * 
     * @return String the URL
     */
    String getUrl();

    /**
     * On the assumption that the method has executed, returns the URI of the
     * final location that the request was made to.
     * 
     * @return
     * @throws URIException
     */
    URI getURI() throws URIException;
    
    /**
     * Returns the specified header from the response. 
     * 
     * @param headerTitle
     * @return
     */
    String getHeader(String headerTitle);
    
    /**
     * Returns the value of the "Location" header
     * 
     * @return
     */
    String getRedirectUrl();

    /**
     * The url to fetch from
     * 
     * @param url
     */
    void setUrl(final String url);

    /**
     * Whether to substitute Warwick tags (<warwick_username/> etc) for values
     * 
     * @param substituteTags
     */
    void setSubstituteWarwickTags(final boolean substituteTags);

    /**
     * Whether to set the SSO cookie
     * 
     * @param setCookie
     */
    void setSSOCookie(final boolean setCookie);

    /**
     * Sets connection timeout
     * 
     * @param timeout
     */
    void setConnectionTimeout(final int timeout);

    /**
     * Sets retrieval timeout
     * 
     * @param timeout
     */
    void setRetrievalTimeout(final int timeout);

    /**
     * Set the http client factory as a string ("always", "never")
     * 
     * @param httpClientFactory
     */
    void setHttpClientFactoryStrategyAsString(final String httpClientFactory);

    /**
     * Sets the multipart part of the request. POST METHODS ONLY
     */
    void setMultipartRequestPart(final Part[] thePart);

    /**
     * Sets the POST body
     */
    void setPostBody(final NameValuePair[] postBody);

    /**
     * Closes the stream.
     */
    void close();

}
