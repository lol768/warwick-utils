package uk.ac.warwick.util.httpclient;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;

import uk.ac.warwick.sso.client.SSOProxyCookieHelper;
import uk.ac.warwick.userlookup.User;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.UriBuilder;

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

            Cookie cookie = new SSOProxyCookieHelper().getProxyHttpClientCookie(getUrl().toJavaUrl(), user);
            state.addCookie(cookie);

            Cookie ssoCookie = new Cookie();
            ssoCookie.setName("WarwickSSO"); 
            ssoCookie.setDomain(cookieDomain);
            ssoCookie.setValue(user.getOldWarwickSSOToken());
            ssoCookie.setPath("/");
            state.addCookie(ssoCookie);
        }
    }

    private boolean isWarwickServer(final Uri uri) {
        return uri.getAuthority().indexOf("warwick.ac.uk") > -1 || uri.getAuthority().indexOf("137.205") > -1;
    }
    
    public final UriBuilder substituteWarwickTags(final UriBuilder builder, final User user) {
        if (isSubstituteTags()) {
            new WarwickTagUrlMangler().substituteWarwickTags(builder, user);
        }
        
        return builder;
    }
    
    public final Uri substituteWarwickTags(final Uri input, final User user) {
        if (isSubstituteTags()) {
            return new WarwickTagUrlMangler().substituteWarwickTags(input, user);
        } else {
            return input;
        }
    }

    public final String substituteWarwickTags(final String input, final User user) {
        if (isSubstituteTags()) {
            return new WarwickTagUrlMangler().substituteWarwickTags(input, user);
        } else {
            return input;
        }
    }

}
