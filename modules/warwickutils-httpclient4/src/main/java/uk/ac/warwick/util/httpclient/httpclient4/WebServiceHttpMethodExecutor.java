package uk.ac.warwick.util.httpclient.httpclient4;

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

    public WebServiceHttpMethodExecutor(final Method method, final String cookieDomain, final User user) {
        this(method, (String)null, cookieDomain, user);
    }
    
    public WebServiceHttpMethodExecutor(final Method method, final String requestUrl, final String cookieDomain, final User user) {
        super(method, requestUrl, cookieDomain, user);
    }

    @Override
    protected boolean shouldAllowWarwickToken(Uri requestUri) {
        return false;
    }

    public void setHttpClientFactoryStrategyAsString(final String theHttpClientFactory) {
        throw new UnsupportedOperationException("Cannot set HttpClientFactory where no SitebuilderRequestInfo is present");
    }

}
