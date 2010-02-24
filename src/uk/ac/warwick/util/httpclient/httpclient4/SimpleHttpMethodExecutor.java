package uk.ac.warwick.util.httpclient.httpclient4;

import java.net.MalformedURLException;
import java.net.URI;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

public final class SimpleHttpMethodExecutor extends AbstractHttpMethodExecutor {

    private static final long serialVersionUID = 4727981250584584960L;

    public SimpleHttpMethodExecutor(Method method) {
        super(method);
    }

    public SimpleHttpMethodExecutor(Method method, String requestUrl) throws MalformedURLException {
        super(method, parse(requestUrl));
    }

    @Override
    void beforeExecution(HttpUriRequest request, HttpContext context) {
        // Nothing to do
    }

    @Override
    URI parseRequestUrl(URI requestUrl) {
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
