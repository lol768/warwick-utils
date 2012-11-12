package uk.ac.warwick.util.httpclient;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;

import uk.ac.warwick.userlookup.User;
import uk.ac.warwick.util.web.Uri;

/**
 * An implementation of the HttpMethodExecutor that is suitable for web services -
 * i.e., still sets cookies and so forth, but does not rely on there being a
 * SiteBuilderRequestInfo available.
 * 
 * @author Mat Mannion
 */
public final class WebServiceHttpMethodExecutor extends AbstractWarwickAwareHttpMethodExecutor implements HttpMethodExecutor {

    private static final long serialVersionUID = -4562920264538934912L;

    private static final Logger LOGGER = Logger.getLogger(WebServiceHttpMethodExecutor.class);

    private final Method methodType;
    
    private final User user;

    public WebServiceHttpMethodExecutor(final Method theMethodType, final User theUser, final String cookieDomain) {
        super(cookieDomain);
        this.methodType = theMethodType;
        this.user = theUser;
    }

    public int execute() throws IOException {
        if (isHasExecuted()) {
            throw new IllegalStateException("Cannot execute HTTP method more than once");
        }

        setMethod(createMethod());

        setClient(getHttpClientFromFactory());
        setSSOCookieIfAppropriate(user);

        HttpConnectionManagerParams params = getClient().getHttpConnectionManager().getParams();
        params.setConnectionTimeout(getConnectionTimeout());
        params.setSoTimeout(getRetrievalTimeout());
        getClient().getParams().setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
        getClient().getParams().setIntParameter(HttpClientParams.MAX_REDIRECTS, 10);

        setHasExecuted(true);

        return getClient().executeMethod(getMethod());
    }

    public void setHttpClientFactoryStrategyAsString(final String theHttpClientFactory) {
        throw new UnsupportedOperationException("Cannot set HttpClientFactory where no SitebuilderRequestInfo is present");
    }

    private HttpMethod createMethod() {
        HttpMethod method;
        Uri substitutedUrl = substituteWarwickTags(getUrl(), user);
        if (methodType.equals(Method.post)) {
            LOGGER.debug("method is post, appending parameters");
            method = new PostMethod(substitutedUrl.toString());
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
            method = new GetMethod(substitutedUrl.toString());
        } else if (methodType.equals(Method.head)) {
            method = new HeadMethod(substitutedUrl.toString());
            method.setFollowRedirects(true);
        } else {
            throw new IllegalArgumentException("No method type? No dice.");
        }
        
        addHeaders(method);
        
        return method;
    }

}