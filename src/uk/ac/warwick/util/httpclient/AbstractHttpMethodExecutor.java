package uk.ac.warwick.util.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.log4j.Logger;

import uk.ac.warwick.util.web.Uri;

/**
 * Utility class with common methods to HttpMethodExecutor.
 * 
 * @author Mat Mannion
 */
public abstract class AbstractHttpMethodExecutor implements HttpMethodExecutor {
    
	private static final long serialVersionUID = -1663245845376032024L;

	private static final Logger LOGGER = Logger.getLogger(AbstractHttpMethodExecutor.class);

    private HttpMethod method;

    private HttpClient client;
    
    private Uri url;
    
    private int connectionTimeout;

    private int retrievalTimeout;
    
    private Part[] multipartRequestPart;
    
    private NameValuePair[] postBody;

    // true when has been executed
    private boolean executed;
    
    private List<Header> headers = new ArrayList<Header>();
    
    private HttpClientFactory factory = MultiThreadedHttpClientFactory.getInstance();
    
    public AbstractHttpMethodExecutor() {
    	//set default timeout values
    	setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
    	setRetrievalTimeout(DEFAULT_RETRIEVAL_TIMEOUT);
    }
    
    public final HttpClient getClient() {
        return client;
    }

    public final HttpMethod getMethod() {
        return method;
    }
    
    public final void setClient(final HttpClient client) {
        this.client = client;
    }
    
    public final void setMethod(final HttpMethod method) {
        this.method = method;
    }
    
    public final int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public final int getRetrievalTimeout() {
        return retrievalTimeout;
    }
    
    public final boolean isHasExecuted() {
        return executed;
    }
    
    public final void setHasExecuted(final boolean hasExecuted) {
        this.executed = hasExecuted;
    }
    
    public final byte[] retrieveContents() throws IOException {
    	assertExecuted();
        try {
            return method.getResponseBody();
        } catch (IOException e) {
            throw e;
        } finally {
            LOGGER.debug("Released connection");
            close();
        }
    }

    public final String retrieveContentsAsString() throws IOException {
    	assertExecuted();
        try {
            return method.getResponseBodyAsString();
        } catch (IOException e) {
            throw e;
        } finally {
            LOGGER.debug("Released connection");
            close();
        }

    }

    /**
     * MUST BE CLOSED MANUALLY
     */
    public final InputStream retrieveContentsAsStream() throws IOException {
    	assertExecuted();
        try {
            return method.getResponseBodyAsStream();
        } catch (IOException e) {
            throw e;
        }
    }
    
    public final Date getLastModifiedDate() {
    	assertExecuted();
        Header lastModifiedHeader = method.getResponseHeader("Last-Modified");
        Date result = null;
        
        if (lastModifiedHeader != null) {
            String value = lastModifiedHeader.getValue();
            //we'll need to parse the date and whatnot
            try {
                result = DateUtil.parseDate(value);
            } catch (DateParseException e) {
                LOGGER.error("Could not parse last-modified header for page",e);
            }
        }
        return result;
    }
    
    public final void setConnectionTimeout(final int timeout) {
        this.connectionTimeout = timeout;
    }

    public final void setRetrievalTimeout(final int timeout) {
        this.retrievalTimeout = timeout;
    }

    public final URI getURI() throws URIException {
    	assertExecuted();
        return method.getURI();
    }

    public String getHeader(String headerTitle) {
        assertExecuted();
        Header header = getMethod().getResponseHeader(headerTitle);
        if (header == null) {
            return null;
        }
        return header.getValue();
    }

    /**
     * Find the declared charset in the headers, or null if it
     * wasn't specified.
     * From {@link HttpMethodBase#getContentCharSet}
     */
    public String getEncodingFromHeader() {
    	assertExecuted();
    	Header contentheader = getMethod().getResponseHeader("Content-Type");
    	String charset = null;
        if (contentheader != null) {
            HeaderElement values[] = contentheader.getElements();
            // I expect only one header element to be there
            // No more. no less
            if (values.length == 1) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    // If I get anything "funny" 
                    // UnsupportedEncodingException will result
                    charset = param.getValue();
                }
            }
        }
        return charset;
    }

    public String getRedirectUrl() {
        return getHeader("Location");
    }

    public final Uri getUrl() {
        // if we have executed already, return the final path (following
        // redirects)

        if (!executed) {
            return url;
        } else {
            return Uri.parse(method.getPath());
        }
    }

    public final void setUrl(Uri url) {
    	this.url = url;
    }
    
    public final Part[] getMultipartRequestPart() {
        return multipartRequestPart;
    }
    
    public final void setMultipartRequestPart(final Part[] thePart) {
        this.multipartRequestPart = thePart;
    }
    
    public final NameValuePair[] getPostBody() {
        return postBody;
    }
    
    public final void setPostBody(final NameValuePair[] postBody) {
        this.postBody = postBody;
    }
    
    /**
     * NOOP if not executed
     */
    public final void close() {
    	if (executed || method != null) {
	        method.abort();
	        method.releaseConnection();
    	}
    }

    public void setHttpClientFactory(HttpClientFactory factory) {
        this.factory = factory;
    }
    
    public HttpClient getHttpClientFromFactory() {
        return factory.getClient();
    }

	public void addHeader(Header header) {
		this.headers.add(header);
	}

	public void addHeader(String name, String value) {
		this.headers.add(new Header(name, value));
	}
	
	protected void addHeaders(HttpMethod theMethod) {
		for (Header header : headers) {
			theMethod.addRequestHeader(header);
        }
	}

	private void assertExecuted() {
		if (!executed) {
            throw new IllegalStateException("Method has not yet been executed");
        }
	}
}
