package uk.ac.warwick.util.httpclient.httpclient4;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.web.Uri;

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

	ResponseHandler<byte[]> RESPONSE_AS_BYTES = new ResponseHandler<byte[]>() {
        public byte[] handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toByteArray(entity);
            } else {
                return null;
            }
        }
    };
    
    ResponseHandler<String> RESPONSE_AS_STRING = new ResponseHandler<String>() {
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            } else {
                return null;
            }
        }
    };
    
    // In case you just need the status code.
    ResponseHandler<Void> IGNORE_RESPONSE = new ResponseHandler<Void>() {
        public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                entity.consumeContent();
            }
            return null;
        }
    };
    
    public interface StreamCallback {
        void doWithStream(InputStream stream);
    }

    public enum Method {
        get, post, head
    };

    /**
     * Executes the HTTP call and returns the response from the response handler and the status code.
     * 
     * @return A {@link Pair} of the status code and the return value.
     * 
     * @throws HttpException
     *             when something nasty happened (subclass of IOException)
     * @throws IOException
     *             when there's been a timeout, usually
     */
    <T> Pair<Integer, T> execute(ResponseHandler<T> handler) throws IOException;
    
    /**
     * Executes the HTTP call and runs the StreamHandler with the response.
     * 
     * @return The HTTP status code
     */
    int execute(StreamCallback handler) throws IOException;

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
     * @throws URISyntaxException 
     */
    Uri getUri();
    
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
    
    HttpResponse getResponse();
    
    HttpContext getContext();

    /**
     * The url to fetch from
     * 
     * @param url
     */
    void setUrl(final String url);
    void setUrl(final Uri url);

    /**
     * Whether to substitute Warwick tags (<warwick_username/> etc) for values
     * 
     * @param substituteTags
     */
    void setSubstituteWarwickTags(final boolean substituteTags);

    void setFollowRedirects(final boolean redirects);
    
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
    void setMultipartBody(List<Pair<String, ? extends ContentBody>> body);

    /**
     * Sets the POST body
     */
    void setPostBody(final List<? extends NameValuePair> postBody);
    
    /**
     * Adds a header to send
     */
    void addHeader(final String name, final String value);
    void addHeader(final Header header);
    
    /**
     * Sets the Http Client Factory to get the HttpClient for the request.
     */
    void setHttpClientFactory(HttpClientFactory factory);
    
    void setHttp10Only(boolean http1);
    void setUseExpectContinueHeader(boolean expect);

}
