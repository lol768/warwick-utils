package uk.ac.warwick.util.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

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

    private HttpMethod method;

    private HttpClient client;
    
    private String url;
    
    private int connectionTimeout;

    private int retrievalTimeout;
    
    private Part[] multipartRequestPart;
    
    private NameValuePair[] postBody;

    // true when has been executed
    private boolean executed;
    
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

    public final void setUrl(final String url) {
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
}
