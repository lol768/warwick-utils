package uk.ac.warwick.util.httpclient.httpclient4;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.web.URLBuilder;

import com.google.common.collect.Lists;

public abstract class AbstractHttpMethodExecutor implements HttpMethodExecutor {

    private static final long serialVersionUID = -6884588480427697793L;
    
    private static final Logger LOGGER = Logger.getLogger(AbstractHttpMethodExecutor.class);
    
    private HttpUriRequest request;
    
    private URI requestUrl;
    
    private final Method methodType;
    
    private final List<Header> headers = Lists.newArrayList();
    
    private List<NameValuePair> postBody;
    
    private List<Pair<String, ContentBody>> multipartBody;
    
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    private int retrievalTimeout = DEFAULT_RETRIEVAL_TIMEOUT;
    
    private HttpClientFactory factory = MultiThreadedHttpClientFactory.getInstance();
    
    private final HttpContext context = new BasicHttpContext();
    
    private HttpResponse response;
    
    public AbstractHttpMethodExecutor(Method method) {
        this(method, null);
    }
    
    public AbstractHttpMethodExecutor(Method method, URI requestUrl) {
        this.methodType = method;
        this.requestUrl = requestUrl;
    }
    
    public final <T> Pair<Integer, T> execute(final ResponseHandler<T> responseHandler) throws IOException {
        assertNotExecuted();
        createRequest();
        
        HttpParams params = request.getParams();
        if (connectionTimeout > 0) {
            ConnManagerParams.setTimeout(params, connectionTimeout);
            HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        }
        if (retrievalTimeout > 0) {
            HttpConnectionParams.setSoTimeout(params, retrievalTimeout);
        }
        
        beforeExecution(request, context);
        
        Pair<HttpResponse, Pair<Integer, T>> response = factory.getClient().execute(request, new ResponseHandler<Pair<HttpResponse, Pair<Integer, T>>>() {
            public Pair<HttpResponse, Pair<Integer, T>> handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                return 
                    Pair.of(
                        response, 
                        Pair.of(
                            response.getStatusLine().getStatusCode(), 
                            responseHandler.handleResponse(response)
                        )
                    );
            }
        }, context);
        
        this.response = response.getLeft();
        return response.getRight();
    }
    
    public final int execute(final StreamCallback callback) throws IOException {
        return execute(new ResponseHandler<Void>() {
            public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    callback.doWithStream(entity.getContent());
                }
                
                return null;
            }
        }).getLeft();
    }

    private void createRequest() {
        HttpUriRequest request;
        switch (methodType) {
            case get:
                request = new HttpGet(parseRequestUrl(requestUrl));
                break;
            case post:
                HttpPost postRequest = new HttpPost(parseRequestUrl(requestUrl));
                if (multipartBody != null && !multipartBody.isEmpty()) {
                    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                    
                    for (Pair<String, ContentBody> multipart : multipartBody) {
                        entity.addPart(multipart.getLeft(), multipart.getRight());
                    }
                    
                    postRequest.setEntity(entity);
                } else if (postBody != null) {
                    try {
                        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postBody, "UTF-8");
                        postRequest.setEntity(entity);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException(e);
                    }
                }
                
                request = postRequest;
                break;
            case head:
                request = new HttpHead(parseRequestUrl(requestUrl));
                break;
            default:
                throw new IllegalArgumentException("Unsupported request type: " + methodType);
        } 
        
        this.request = request;
        
        // Add headers
        for (Header header : headers) {
            request.addHeader(header);
        }
    }
    
    abstract URI parseRequestUrl(URI requestUrl);
    abstract void beforeExecution(HttpUriRequest request, HttpContext context);

    /* Methods that only make sense before execution */
    public final void addHeader(String name, String value) {
        assertNotExecuted();
        
        addHeader(new BasicHeader(name, value));
    }

    public final void addHeader(Header header) {
        assertNotExecuted();
        
        this.headers.add(header);
    }

    public final void setConnectionTimeout(int timeout) {
        assertNotExecuted();
        
        this.connectionTimeout = timeout;
    }

    public final void setRetrievalTimeout(int timeout) {
        assertNotExecuted();
        
        this.retrievalTimeout = timeout;
    }

    public final void setHttpClientFactory(HttpClientFactory factory) {
        assertNotExecuted();
        
        this.factory = factory;
    }
    
    public final void setMultipartBody(List<Pair<String, ContentBody>> body) {
        assertNotExecuted();
        
        this.multipartBody = body;
    }

    public final void setPostBody(List<NameValuePair> postBody) {
        assertNotExecuted();
        
        this.postBody = postBody;
    }

    public final void setUrl(String url) throws MalformedURLException {
        assertNotExecuted();
        
        this.requestUrl = parse(url);
    }
    
    protected static final URI parse(String uri) throws MalformedURLException {
        if (uri == null) { return null; }
        
        return new URLBuilder(uri).toURI();
    }

    /* Methods that can be called either before or after execution */
    public final String getUrl() {
        if (response == null) {
            return requestUrl.toString();
        } else {
            return getRedirectUrl();
        }
    }
    
    public final HttpContext getContext() {
        return context;
    }

    public final HttpResponse getResponse() {
        return response;
    }

    /* Methods that can only be called after execution */
    public final String getHeader(String name) {
        assertExecuted();
        
        Header header = response.getFirstHeader(name);
        return header == null ? null : header.getValue();
    }

    public final Date getLastModifiedDate() {
        assertExecuted();
        
        Header lastModifiedHeader = response.getFirstHeader("Last-Modified");
        Date result = null;
        
        if (lastModifiedHeader != null) {
            String value = lastModifiedHeader.getValue();
            //we'll need to parse the date and whatnot
            try {
                result = DateUtils.parseDate(value);
            } catch (DateParseException e) {
                LOGGER.error("Could not parse last-modified header for page",e);
            }
        }
        return result;
    }

    public final String getRedirectUrl() {
        assertExecuted();
        
        try {
            return getURI().toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URI", e);
        }
    }

    public final URI getURI() throws URISyntaxException {
        assertExecuted();
        
        HttpUriRequest finalRequest = (HttpUriRequest)context.getAttribute(ExecutionContext.HTTP_REQUEST);
        if (finalRequest != null) {
            try {
                return new URL(request.getURI().toURL(), finalRequest.getURI().toString()).toURI();
            } catch (MalformedURLException e) {
                throw new URISyntaxException(finalRequest.getURI().toString(), e.getMessage());
            }
        } else {
            throw new IllegalStateException("Couldn't find target in context");
        }
    }
    
    private void assertNotExecuted() {
        if (response != null) {
            throw new IllegalStateException("Request has already been executed");
        }
    }
    
    private void assertExecuted() {
        if (response == null) {
            throw new IllegalStateException("Request has not yet been executed");
        }
    }

}
