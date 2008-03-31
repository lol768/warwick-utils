package uk.ac.warwick.util.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.log4j.Logger;

/**
 * Utility class with common methods to HttpMethodExecutor.
 * 
 * @author Mat Mannion
 */
public abstract class AbstractHttpMethodExecutor implements HttpMethodExecutor {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractHttpMethodExecutor.class);
    
    private static final BitSet ALLOWED_QUERYSTRING_CHARACTERS;
    
    static {
    	ALLOWED_QUERYSTRING_CHARACTERS = new BitSet(256);
    	
    	//standard URL characters
    	ALLOWED_QUERYSTRING_CHARACTERS.set(';');
		ALLOWED_QUERYSTRING_CHARACTERS.set('/');
		ALLOWED_QUERYSTRING_CHARACTERS.set('?');
		ALLOWED_QUERYSTRING_CHARACTERS.set(':');
		ALLOWED_QUERYSTRING_CHARACTERS.set('@');
		ALLOWED_QUERYSTRING_CHARACTERS.set('&');
		ALLOWED_QUERYSTRING_CHARACTERS.set('=');
		ALLOWED_QUERYSTRING_CHARACTERS.set('+');
		ALLOWED_QUERYSTRING_CHARACTERS.set('$');
		ALLOWED_QUERYSTRING_CHARACTERS.set(',');
		ALLOWED_QUERYSTRING_CHARACTERS.set('-');
		ALLOWED_QUERYSTRING_CHARACTERS.set('_');
		ALLOWED_QUERYSTRING_CHARACTERS.set('.');
		ALLOWED_QUERYSTRING_CHARACTERS.set('!');
		ALLOWED_QUERYSTRING_CHARACTERS.set('~');
		ALLOWED_QUERYSTRING_CHARACTERS.set('*');
		ALLOWED_QUERYSTRING_CHARACTERS.set('\'');
		ALLOWED_QUERYSTRING_CHARACTERS.set('(');
		ALLOWED_QUERYSTRING_CHARACTERS.set(')');
		
		// ignore already escaped characters
		ALLOWED_QUERYSTRING_CHARACTERS.set('%');
        
		// alphanumeric
        for (int i = 'a'; i <= 'z'; i++) {
        	ALLOWED_QUERYSTRING_CHARACTERS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
        	ALLOWED_QUERYSTRING_CHARACTERS.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
        	ALLOWED_QUERYSTRING_CHARACTERS.set(i);
        }
    }

    private HttpMethod method;

    private HttpClient client;
    
    private String url;
    
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
        if (!executed) {
            throw new IllegalStateException("Could not retrieve contents of HTTP stream - has not been executed");
        }
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
        if (!executed) {
            throw new IllegalStateException("Could not retrieve contents of HTTP stream - has not been executed");
        }
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
        if (!executed) {
            throw new IllegalStateException("Could not retrieve contents of HTTP stream - has not been executed");
        }
        try {
            return method.getResponseBodyAsStream();
        } catch (IOException e) {
            throw e;
        }
    }
    
    public final Date getLastModifiedDate() {
        if (!executed) {
            throw new IllegalStateException("Could not retrieve last modified header - has not been executed");
        }
        Header lastModifiedHeader = method.getResponseHeader("Last-Modified");
        Date result = null;
        
        if (lastModifiedHeader != null) {
            String value = lastModifiedHeader.getValue();
            //we'll need to parse the date and whatnot
            try {
                result = DateUtil.parseDate(value);
            } catch (DateParseException e) {
                LOGGER.error("Could not parse last-modified header for page",e);
                result = null;
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
        if (!executed) {
            throw new IllegalStateException("Cannot get URI if method has not been executed");
        }
        return method.getURI();
    }

    public String getHeader(String headerTitle) {
        if (!executed) {
            throw new IllegalStateException("Cannot get header if method has not been executed");
        }
        
        Header header = getMethod().getResponseHeader(headerTitle);
        if (header == null) {
            return null;
        }
        return header.getValue();
    }

    public String getRedirectUrl() {
        return getHeader("Location");
    }

    public final String getUrl() {
        // if we have executed already, return the final path (following
        // redirects)

        if (!executed) {
            return url;
        } else {
            return method.getPath();
        }
    }
    
    public final String escapeQueryString(String url) {
    	String escapedUrl = url;
    	
    	// if the URL has a query string, escape that query string since HttpClient 3.1 is really anal about it
    	if (escapedUrl.indexOf('?') != -1) {
    		try {
    			escapedUrl = escapedUrl.substring(0, escapedUrl.indexOf('?')) + new String(URLCodec.encodeUrl(ALLOWED_QUERYSTRING_CHARACTERS, escapedUrl.substring(escapedUrl.indexOf('?')).getBytes("UTF-8")), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// this can never happen unless UTF-8 is suddenly invalid. if that happened, we'd have much, much bigger problems
				throw new IllegalStateException(e);
			}
    	}
    	
    	return escapedUrl;
    }

    public final void setUrl(String url) {
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
    
    public final void close() {
        method.abort();
        method.releaseConnection();
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
	
	protected void addHeaders(HttpMethod method) {
		for (Header header : headers) {
        	method.addRequestHeader(header);
        }
	}
}
