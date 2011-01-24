package uk.ac.warwick.util.httpclient.httpclient4;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;

import uk.ac.warwick.sso.client.SSOProxyCookieHelper;
import uk.ac.warwick.userlookup.User;
import uk.ac.warwick.util.httpclient.WarwickTagUrlMangler;
import uk.ac.warwick.util.web.Uri;

public abstract class AbstractWarwickAwareHttpMethodExecutor extends AbstractHttpMethodExecutor {
    
	private static final long serialVersionUID = -2713163190560555670L;

	private boolean setCookie;

    private boolean substituteTags;
    
    private final String cookieDomain;
    
    private final User user;
    
    public AbstractWarwickAwareHttpMethodExecutor(Method method, Uri requestUrl, String theCookieDomain, User user) {
        super(method, requestUrl);
        this.cookieDomain = theCookieDomain;
        this.user = user;
    }
    
    public AbstractWarwickAwareHttpMethodExecutor(Method method, String requestUrl, String theCookieDomain, User user) {
        this(method, parse(requestUrl), theCookieDomain, user);
    }
    
    final boolean isSetCookie() {
        return setCookie;
    }
    
    final boolean isSubstituteTags() {
        return substituteTags;
    }
    
    public final void setSSOCookie(final boolean cookie) {
        this.setCookie = cookie;
    }

    public final void setSubstituteWarwickTags(final boolean substituteWarwickTags) {
        this.substituteTags = substituteWarwickTags;
    }

    @Override
    void beforeExecution(HttpUriRequest request, HttpContext context) throws Exception {
        if ((isWarwickServer(getUrl())) && user.isLoggedIn() && isSetCookie()) {
            request.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

            Uri targetUri = Uri.fromJavaUri(request.getURI());
            CookieStore store = new BasicCookieStore();
            
            // This is a httpclient3 cookie at the moment, so we'll have to translate it
            org.apache.commons.httpclient.Cookie cookie = new SSOProxyCookieHelper().getProxyHttpClientCookie(targetUri.toJavaUrl(), user);
            if (cookie != null) {
                BasicClientCookie proxyCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
                proxyCookie.setDomain(cookie.getDomain());
                proxyCookie.setPath(cookie.getPath());
                proxyCookie.setExpiryDate(cookie.getExpiryDate());
                proxyCookie.setSecure(cookie.getSecure());
                proxyCookie.setVersion(cookie.getVersion());
                store.addCookie(proxyCookie);
            }

            BasicClientCookie ssoCookie = new BasicClientCookie("WarwickSSO", user.getOldWarwickSSOToken());
            ssoCookie.setDomain(cookieDomain);
            ssoCookie.setPath("/");
            store.addCookie(ssoCookie);
            
            context.setAttribute(ClientContext.COOKIE_STORE, store);
        }
    }

    @Override
    Uri parseRequestUrl(Uri requestUrl) {
        return substituteWarwickTags(requestUrl);
    }

    private boolean isWarwickServer(final String theUrl) {
        return theUrl.indexOf("warwick.ac.uk") > -1 || theUrl.indexOf("137.205") > -1;
    }
    
    private Uri substituteWarwickTags(final Uri input) {
        if (isSubstituteTags()) {
            return new WarwickTagUrlMangler().substituteWarwickTags(input, user);
        } else {
            return input;
        }
    }

}
