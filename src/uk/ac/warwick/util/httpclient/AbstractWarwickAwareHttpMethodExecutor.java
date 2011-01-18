package uk.ac.warwick.util.httpclient;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;

import uk.ac.warwick.sso.client.SSOProxyCookieHelper;
import uk.ac.warwick.userlookup.User;
import uk.ac.warwick.util.web.Uri;

public abstract class AbstractWarwickAwareHttpMethodExecutor extends AbstractHttpMethodExecutor {
    
	private static final long serialVersionUID = -2713163190560555670L;

	private boolean setCookie;

    private boolean substituteTags;
    
    private String cookieDomain;
    
    public AbstractWarwickAwareHttpMethodExecutor(String theCookieDomain) {
    	super();
        this.cookieDomain = theCookieDomain;
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

    public final void setSSOCookieIfAppropriate(final User user) {
        if ((isWarwickServer(getUrl())) && user.isLoggedIn() && isSetCookie()) {
            HttpState state = getClient().getState();
            getMethod().getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

            URL targetURL;
            try {
                targetURL = new URL(escapeQueryString(getUrl()));
            } catch (final MalformedURLException e) {
                throw new IllegalStateException("URL is invalid: " + getUrl(), e);
            }
            Cookie cookie = new SSOProxyCookieHelper().getProxyHttpClientCookie(targetURL, user);
            state.addCookie(cookie);

            Cookie ssoCookie = new Cookie();
            ssoCookie.setName("WarwickSSO"); 
            ssoCookie.setDomain(cookieDomain);
            ssoCookie.setValue(user.getOldWarwickSSOToken());
            ssoCookie.setPath("/");
            state.addCookie(ssoCookie);
        }
    }

    private boolean isWarwickServer(final String theUrl) {
        return theUrl.indexOf("warwick.ac.uk") > -1 || theUrl.indexOf("137.205") > -1;
    }

    public final String substituteWarwickTags(final String urlToSubstitute, final User user) {
        if (isSubstituteTags()) {
            return new WarwickTagUrlMangler().substituteWarwickTags(Uri.parse(urlToSubstitute), user).toString();
        } else {
            return urlToSubstitute;
        }
    }


}
