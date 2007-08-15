package uk.ac.warwick.util.httpclient;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;

/**
 * A simple implementation of the http method executor that doesn't worry about
 * SSO stuff.
 * 
 * @author Mat Mannion
 */
public final class SimpleHttpMethodExecutor extends AbstractHttpMethodExecutor implements HttpMethodExecutor {

    private static final long serialVersionUID = -2406117798201732629L;
    
    private static final Logger LOGGER = Logger.getLogger(SimpleHttpMethodExecutor.class);
    
    private final Method methodType;
    
    public SimpleHttpMethodExecutor(final Method theMethodType) {
    	super();
        this.methodType = theMethodType;
    }

    public int execute() throws IOException {
        if (isHasExecuted()) {
            throw new IllegalStateException("Cannot execute HTTP method more than once");
        }

        setMethod(createMethod());

        setClient(getHttpClientFromFactory());

        //if we have set timeouts, pass them through
        HttpConnectionManagerParams params = getClient().getHttpConnectionManager().getParams();
        
        if (getConnectionTimeout() != 0) {
            params.setConnectionTimeout(getConnectionTimeout());
        }
        
        if (getRetrievalTimeout() != 0) {
            params.setSoTimeout(getRetrievalTimeout());
        }
        
        setHasExecuted(true);

        return getClient().executeMethod(getMethod());
    }
    
    private HttpMethod createMethod() {
        HttpMethod method;
        if (methodType.equals(Method.post)) {
            LOGGER.debug("method is post, appending parameters");
            method = new PostMethod(getUrl());
            // can't follow redirects from a POST request. HTTPClient is TeH
            // SUX0R
            method.setFollowRedirects(false);
            
            if (getMultipartRequestPart() != null) {
                ((PostMethod)method).setRequestEntity(new MultipartRequestEntity(getMultipartRequestPart(),method.getParams()));
            }
            if (getPostBody() != null) {
                ((PostMethod)method).setRequestBody(getPostBody());
            }
        } else if (methodType.equals(Method.get)) {
            LOGGER.debug("Method is get");
            method = new GetMethod(getUrl());
        } else if (methodType.equals(Method.head)) {
            method = new HeadMethod(getUrl());
            method.setFollowRedirects(true);
        } else {
            throw new IllegalArgumentException("No method type? No dice.");
        }
        return method;
    }

    public void setHttpClientFactoryStrategyAsString(final String theHttpClientFactory) {
        throw new UnsupportedOperationException("Cannot set different http client factories on a simple executor - use SitebuilderInfoBackedHttpMethodExecutor");
    }

    public void setSSOCookie(final boolean cookie) {
        throw new UnsupportedOperationException("Cannot set SSO cookies on a simple executor - use SitebuilderInfoBackedHttpMethodExecutor");
    }

    public void setSubstituteWarwickTags(final boolean substituteWarwickTags) {
        throw new UnsupportedOperationException("Cannot substitute Warwick tags on a simple executor - use SitebuilderInfoBackedHttpMethodExecutor");
    }

}
