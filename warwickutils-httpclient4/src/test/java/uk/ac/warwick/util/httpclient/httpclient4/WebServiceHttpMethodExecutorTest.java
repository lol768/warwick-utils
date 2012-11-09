package uk.ac.warwick.util.httpclient.httpclient4;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;

import junit.framework.TestCase;
import uk.ac.warwick.userlookup.User;
import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;

public class WebServiceHttpMethodExecutorTest extends TestCase {

    public void testItWorks() throws Exception {
        HttpMethodExecutor ex = new WebServiceHttpMethodExecutor(Method.get, "http://www.warwick.ac.uk", null, new User());

        // this depends on us having a website. That is probably the case.

        Pair<Integer, byte[]> result = ex.execute(HttpMethodExecutor.RESPONSE_AS_BYTES);

        // ex.getURI() will be /insite from on campus, not really good to test

        assertEquals(200, result.getLeft().intValue());

        assertNotNull(result.getRight());

        // we use an etag based system
        // assertNotNull(ex.getLastModifiedDate());
    }

    public void testItSendsCookie() throws Exception {
        User user = new User();
        user.setOldWarwickSSOToken("ssotoken");
        
        //only if logged in
        user.setIsLoggedIn(true);

        HttpMethodExecutor ex = new WebServiceHttpMethodExecutor(Method.get, "http://www.warwick.ac.uk", null, user);
        ex.setSSOCookie(true);

        // this depends on us having a website. That is probably the case.

        Pair<Integer, byte[]> result = ex.execute(HttpMethodExecutor.RESPONSE_AS_BYTES);
        assertEquals(200, result.getLeft().intValue());

        // assert that we have sent a cookie
        CookieStore store = (CookieStore) ex.getContext().getAttribute(ClientContext.COOKIE_STORE);
        assertNotNull(store);
        assertEquals(1, store.getCookies().size());
    }

    public void testUnsupportedOperation() throws Exception {
        HttpMethodExecutor ex = new WebServiceHttpMethodExecutor(Method.head, "http://www.warwick.ac.uk", null, new User());

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
