package uk.ac.warwick.util.httpclient;

import junit.framework.TestCase;
import uk.ac.warwick.userlookup.User;
import uk.ac.warwick.util.httpclient.HttpMethodExecutor.Method;
import uk.ac.warwick.util.web.Uri;

public class WebServiceHttpMethodExecutorTest extends TestCase {

    public void testItWorks() throws Exception {
        HttpMethodExecutor ex = new WebServiceHttpMethodExecutor(Method.get, new User(), null);
        ex.setUrl(Uri.parse("http://www.warwick.ac.uk"));

        // this depends on us having a website. That is probably the case.

        int result = ex.execute();

        // ex.getURI() will be /insite from on campus, not really good to test

        assertEquals(result, 200);

        assertNotNull(ex.retrieveContents());

        // we use an etag based system
        // assertNotNull(ex.getLastModifiedDate());
    }

    public void testItSendsCookie() throws Exception {
        User user = new User();
        user.setOldWarwickSSOToken("ssotoken");
        
        //only if logged in
        user.setIsLoggedIn(true);

        WebServiceHttpMethodExecutor ex = new WebServiceHttpMethodExecutor(Method.get, user, null);
        ex.setUrl(Uri.parse("http://www.warwick.ac.uk"));
        ex.setSSOCookie(true);

        // this depends on us having a website. That is probably the case.

        int result = ex.execute();
        assertEquals(200, result);

        // assert that we have sent a cookie
        assertEquals(ex.getClient().getState().getCookies().length, 1);
    }

    public void testUnsupportedOperation() throws Exception {
        HttpMethodExecutor ex = new WebServiceHttpMethodExecutor(Method.head, new User(), null);
        ex.setUrl(Uri.parse("http://www.warwick.ac.uk"));

        try {
            ex.retrieveContentsAsString();
            fail();
        } catch (IllegalStateException e) {
            // should throw this, as has not executed yet
        }

        try {
            ex.setHttpClientFactoryStrategyAsString("always");
            fail();
        } catch (UnsupportedOperationException e) {
            // should throw this, as not supported
        }

        try {
            ex.setSSOCookie(true);
        } catch (UnsupportedOperationException e) {
            // should not throw this, as is supported
            fail();
        }
    }

}
