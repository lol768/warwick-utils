package uk.ac.warwick.util.httpclient;

import junit.framework.TestCase;
import uk.ac.warwick.util.httpclient.HttpMethodExecutor.Method;

public class SimpleHttpMethodExecutorTest extends TestCase {

    public void testItWorks() throws Exception {
        HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get);
        ex.setUrl("http://www.warwick.ac.uk");

        // this depends on us having a website. That is probably the case.

        int result = ex.execute();

        // ex.getURI() will be /insite from on campus, not really good to test

        assertEquals(result, 200);

        assertNotNull(ex.retrieveContents());

        // we use an etag based system
        // assertNotNull(ex.getLastModifiedDate());
    }
    
    public void testUnsupportedOperation() throws Exception {
        HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.head);
        ex.setUrl("http://www.warwick.ac.uk");
        
        try {
            ex.retrieveContentsAsString();
            fail();
        } catch (IllegalStateException e) {
            //should throw this, as has not executed yet
        }
        
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
