package uk.ac.warwick.util.httpclient.httpclient4;

import junit.framework.TestCase;
import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;

public class SimpleHttpMethodExecutorTest extends TestCase {

    public void testItWorks() throws Exception {
        HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get);
        ex.setUrl("http://www.warwick.ac.uk");

        // this depends on us having a website. That is probably the case.

        Pair<Integer, byte[]> result = ex.execute(HttpMethodExecutor.RESPONSE_AS_BYTES);

        // ex.getURI() will be /insite from on campus, not really good to test

        assertEquals(200, result.getLeft().intValue());

        assertNotNull(result.getRight());

        // we use an etag based system
        // assertNotNull(ex.getLastModifiedDate());
    }
    
    public void testUnsupportedOperation() throws Exception {
        HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.head);
        ex.setUrl("http://www.warwick.ac.uk");
        
        try {
            ex.setHttpClientFactoryStrategyAsString("always");
            fail();
        } catch (UnsupportedOperationException e) {
            //should throw this, as not supported
        }
        
        try {
            ex.setSSOCookie(true);
            fail();
        } catch (UnsupportedOperationException e) {
            //should throw this, as not supported
        }
    }

}
