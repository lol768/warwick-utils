package uk.ac.warwick.util.httpclient.httpclient4;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import uk.ac.warwick.util.web.Uri;

public final class SimpleHttpMethodExecutor extends AbstractHttpMethodExecutor {

    private static final long serialVersionUID = 4727981250584584960L;

    public SimpleHttpMethodExecutor(Method method) {
        super(method);
    }

    public SimpleHttpMethodExecutor(Method method, String requestUrl) {
        super(method, parse(requestUrl));
    }

    @Override
    void beforeExecution(HttpUriRequest request, HttpContext context) {
        // Nothing to do
    }

    @Override
    Uri parseRequestUrl(Uri requestUrl) {
        return requestUrl;
    }

    public void setHttpClientFactoryStrategyAsString(String httpClientFactory) {
        throw new UnsupportedOperationException("Cannot set different http client factories on a simple executor - use SitebuilderInfoBackedHttpMethodExecutor");
    }

    public void setSSOCookie(boolean setCookie) {
        throw new UnsupportedOperationException("Cannot set SSO cookies on a simple executor - use SitebuilderInfoBackedHttpMethodExecutor");
    }

    public void setSubstituteWarwickTags(boolean substituteTags) {
        throw new UnsupportedOperationException("Cannot substitute Warwick tags on a simple executor - use SitebuilderInfoBackedHttpMethodExecutor");
    }

}
