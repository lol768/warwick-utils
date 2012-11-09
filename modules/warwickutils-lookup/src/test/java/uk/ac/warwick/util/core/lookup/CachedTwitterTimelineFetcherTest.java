package uk.ac.warwick.util.core.lookup;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.warwick.util.AbstractJUnit4JettyTest;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.web.Uri;

@SuppressWarnings("serial")
public final class CachedTwitterTimelineFetcherTest extends AbstractJUnit4JettyTest {
    
    @BeforeClass
    public static void startServers() throws Exception {
        startServer(new HashMap<String, String>() {{
            put("/notfound.json", NotFoundServlet.class.getName());
            put("/matmannion.json", TwitterJSONServlet.class.getName());
            put("/twitter-down.json", ServiceUnavailableServlet.class.getName());
        }});
    }
    
    @Test
    public void found() throws Exception {
        CachedTwitterTimelineFetcher fetcher = new CachedTwitterTimelineFetcher(Uri.parse(super.serverAddress));
        
        int buffer = TwitterJSONServlet.executionCount;
        
        assertEquals("{ \"success\": true }", fetcher.get("matmannion", 20, true).getResponseBody());
        assertEquals(1, TwitterJSONServlet.executionCount - buffer);
        
        // Test caching; shouldn't have hit the servlet again
        assertEquals("{ \"success\": true }", fetcher.get("matmannion", 20, true).getResponseBody());
        assertEquals(1, TwitterJSONServlet.executionCount - buffer);
        
        // Test caching with different capitalisation; shouldn't have hit the servlet again
        assertEquals("{ \"success\": true }", fetcher.get("MatMannion", 20, true).getResponseBody());
        assertEquals(1, TwitterJSONServlet.executionCount - buffer);
    }
    
    @Test
    public void notFound() throws Exception {
        CachedTwitterTimelineFetcher fetcher = new CachedTwitterTimelineFetcher(Uri.parse(super.serverAddress));
        
        int buffer = NotFoundServlet.executionCount;
        
        assertEquals(HttpStatus.SC_NOT_FOUND, fetcher.get("notfound", 20, true).getStatusCode());
        assertEquals(1, NotFoundServlet.executionCount - buffer);
        
        // Test caching; shouldn't have hit the servlet again
        assertEquals(HttpStatus.SC_NOT_FOUND, fetcher.get("notfound", 20, true).getStatusCode());
        assertEquals(1, NotFoundServlet.executionCount - buffer);
    }
    
    @Test
    public void twitterDown() throws Exception {
        CachedTwitterTimelineFetcher fetcher = new CachedTwitterTimelineFetcher(Uri.parse(super.serverAddress));
        
        int buffer = ServiceUnavailableServlet.executionCount;
        
        assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, fetcher.get("twitter-down", 20, true).getStatusCode());
        assertEquals(1, ServiceUnavailableServlet.executionCount - buffer);
        
        // Test caching - this shouldn't be cached, so we should see it get hit again
        assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, fetcher.get("twitter-down", 20, true).getStatusCode());
        assertEquals(2, ServiceUnavailableServlet.executionCount - buffer);
    }
    
    @BeforeClass
    public static void setupEhCache() throws Exception {
        Caches.resetEhCacheCheck();
        System.setProperty("warwick.ehcache.disk.store.dir", root.getAbsolutePath());
    }
    
    @AfterClass
    public static void unsetEhCache() throws Exception {
        System.clearProperty("warwick.ehcache.disk.store.dir");
        Caches.resetEhCacheCheck();
    }
    
    @After
    public void emptyCache() {
        assertTrue(Caches.newCache(CachedTwitterTimelineFetcher.CACHE_NAME, null, 0, CacheStrategy.EhCacheRequired).clear());
    }
    
    public static class TwitterJSONServlet extends HttpServlet {
        
        private static int executionCount = 0;
        
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertEquals("40", req.getParameter("count"));
            assertEquals("true", req.getParameter("include_rts"));
            assertEquals("true", req.getParameter("include_entities"));
            assertNull(req.getParameter("callback"));
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{ \"success\": true }");
            
            executionCount++;
        }
    }

}
