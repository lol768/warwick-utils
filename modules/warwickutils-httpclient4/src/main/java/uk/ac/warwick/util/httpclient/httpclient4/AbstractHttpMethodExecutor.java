package uk.ac.warwick.util.httpclient.httpclient4;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.apache.http.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.web.Uri;

import com.google.common.collect.Lists;

public abstract class AbstractHttpMethodExecutor implements HttpMethodExecutor {

    private static final long serialVersionUID = -6884588480427697793L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpMethodExecutor.class);
    
    private HttpRequestBase request;
    
    private Uri requestUrl;
    
    private final Method methodType;
    
    private final List<Header> headers = Lists.newArrayList();
    
    private List<? extends NameValuePair> postBody;
    
    private List<Pair<String, ? extends ContentBody>> multipartBody;
    
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    private int retrievalTimeout = DEFAULT_RETRIEVAL_TIMEOUT;
    
    private HttpClientFactory factory = MultiThreadedHttpClientFactory.getInstance();
    
    private final HttpContext context = new BasicHttpContext();
    
    private HttpResponse response;
    
    private HttpRequestDecorator httpRequestDecorator = new DefaultHttpRequestDecorator();
    
    private boolean followRedirects;
    private boolean followRedirectsSet;

    private boolean http10Only;

    private boolean useExpect = true;
    private boolean useExpectSet;
    
    public AbstractHttpMethodExecutor(Method method) {
        this(method, null);
    }
    
    public AbstractHttpMethodExecutor(Method method, Uri requestUrl) {
        this.methodType = method;
        this.requestUrl = requestUrl;
    }
    
    public final <T> Pair<Integer, T> execute(final ResponseHandler<T> responseHandler) throws IOException {
        assertNotExecuted();
        createRequest();

        RequestConfig.Builder configBuilder = RequestConfig.copy(request.getConfig());

        if (connectionTimeout > 0) {
            configBuilder.setConnectionRequestTimeout(connectionTimeout);
            configBuilder.setConnectTimeout(connectionTimeout);
        }
        if (retrievalTimeout > 0) {
            configBuilder.setSocketTimeout(retrievalTimeout);
        }

        request.setConfig(configBuilder.build());
        
        try {
            httpRequestDecorator.decorate(request, context);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        
        Pair<HttpResponse, Pair<Integer, T>> response = factory.getClient().execute(request, new ResponseHandler<Pair<HttpResponse, Pair<Integer, T>>>() {
            public Pair<HttpResponse, Pair<Integer, T>> handleResponse(HttpResponse response) throws IOException {
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
            public Void handleResponse(HttpResponse response) throws IOException {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    callback.doWithStream(entity.getContent());
                }
                
                return null;
            }
        }).getLeft();
    }

    private void createRequest() {
        HttpRequestBase r;
        switch (methodType) {
            case get:
                r = new HttpGet(parseRequestUrl(requestUrl).toJavaUri());
                break;
            case post:
                HttpPost postRequest = new HttpPost(parseRequestUrl(requestUrl).toJavaUri());
                if (multipartBody != null && !multipartBody.isEmpty()) {
                    MultipartEntityBuilder builder =
                        MultipartEntityBuilder.create()
                            .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    
                    for (Pair<String, ? extends ContentBody> multipart : multipartBody) {
                        builder.addPart(multipart.getLeft(), multipart.getRight());
                    }
                    
                    postRequest.setEntity(builder.build());
                } else if (postBody != null) {
                    try {
                        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postBody, "UTF-8");
                        postRequest.setEntity(entity);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException(e);
                    }
                }
                
                r = postRequest;
                break;
            case head:
                r = new HttpHead(parseRequestUrl(requestUrl).toJavaUri());
                break;
            default:
                throw new IllegalArgumentException("Unsupported request type: " + methodType);
        } 
        
        this.request = r;

        RequestConfig.Builder configBuilder = RequestConfig.copy(MultiThreadedHttpClientFactory.DEFAULT_REQUEST_CONFIG);

        if (followRedirectsSet) {
            configBuilder.setRedirectsEnabled(followRedirects);
        }
        
        if (http10Only) {
            r.setProtocolVersion(HttpVersion.HTTP_1_0);
        }
        
        if (useExpectSet) {
            configBuilder.setExpectContinueEnabled(useExpect);
        }

        r.setConfig(configBuilder.build());
        
        // Add headers
        for (Header header : headers) {
            r.addHeader(header);
        }
    }
    
    public abstract Uri parseRequestUrl(Uri requestUrl);
    
    public final HttpRequestDecorator getHttpRequestDecorator() {
        return httpRequestDecorator;
    }

    public final void setHttpRequestDecorator(HttpRequestDecorator httpRequestDecorator) {
        this.httpRequestDecorator = httpRequestDecorator;
    }

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
    
    public final void setMultipartBody(List<Pair<String, ? extends ContentBody>> body) {
        assertNotExecuted();
        
        this.multipartBody = body;
    }

    public final void setPostBody(List<? extends NameValuePair> postBody) {
        assertNotExecuted();
        
        this.postBody = postBody;
    }
    
    public final List<? extends NameValuePair> getPostBody() {
        return postBody;
    }

    public final List<Pair<String, ? extends ContentBody>> getMultipartBody() {
        return multipartBody;
    }

    public void setUrl(Uri url) {
        assertNotExecuted();
        
        this.requestUrl = url;
    }

    public final void setUrl(String url) {
        assertNotExecuted();
        
        this.requestUrl = parse(url);
    }
    
    protected static Uri parse(String uri) {
        if (uri == null) { return null; }
        
        return Uri.parse(uri);
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

            // we'll need to parse the date and whatnot
            result = DateUtils.parseDate(value);
        }
        return result;
    }

    public final String getRedirectUrl() {
        assertExecuted();
        
        return getUri().toString();
    }

    public final Uri getUri() {
        assertExecuted();
        
        HttpUriRequest finalRequest = (HttpUriRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
        HttpHost currentHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
        if (finalRequest != null && finalRequest.getURI().isAbsolute()) {
            return Uri.fromJavaUri(finalRequest.getURI());
        } else if (finalRequest != null && currentHost != null) {
            return Uri.parse(currentHost.toURI()).resolve(Uri.fromJavaUri(finalRequest.getURI()));
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
    
    public void setFollowRedirects(boolean follow) {
        followRedirectsSet = true;
        followRedirects = follow;
    }
    
    public void setHttp10Only(boolean http1) {
        this.http10Only = http1;
    }
    public void setUseExpectContinueHeader(boolean expect) {
        this.useExpectSet = true;
        this.useExpect   = expect;
    }

}
