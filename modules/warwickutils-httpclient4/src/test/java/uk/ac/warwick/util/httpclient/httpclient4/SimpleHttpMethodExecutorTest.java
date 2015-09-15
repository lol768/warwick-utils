package uk.ac.warwick.util.httpclient.httpclient4;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.warwick.util.AbstractJUnit4JettyTest;
import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;

import java.net.SocketTimeoutException;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class SimpleHttpMethodExecutorTest extends AbstractJUnit4JettyTest {

    @BeforeClass
    public static void letsGetItStarted() throws Exception {
        startServer(ImmutableMap.<String, String>builder()
                .put("/website", OKServlet.class.getName())
                .put("/longop", SlowServlet.class.getName())
                .build());
    }

    @Test
    public void testItWorks() throws Exception {

        HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get);
        ex.setUrl(serverAddress+"/website");

        Pair<Integer, byte[]> result = ex.execute(HttpMethodExecutor.RESPONSE_AS_BYTES);

        assertEquals("/website", ex.getUri().getPath());

        assertEquals(200, result.getLeft().intValue());
        assertNotNull(result.getRight());
    }

    @Test(expected = SocketTimeoutException.class)
    public void timeout() throws Exception {
        startServer(ImmutableMap.of("/longop", SlowServlet.class.getName()));

        HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get);
        ex.setUrl(serverAddress+"/longop");
        ex.setConnectionTimeout(10);
        ex.setRetrievalTimeout(10);

        ex.execute(HttpMethodExecutor.RESPONSE_AS_STRING);
    }

    @Test
    public void testUnsupportedOperation() throws Exception {
        HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.head);
        ex.setUrl(serverAddress+"/website");
        
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
