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
	
	// Use the examples from https://dev.twitter.com/docs/auth/application-only-auth so we know the encoding is right
	private static final String TEST_CONSUMER_KEY = "xvz1evFS4wEEPTGEFPHBog";
	private static final String TEST_CONSUMER_SECRET = "L8qq9PZyRg6ieKGEKhZolGC0vJWLw8iEJ88DRdyOg";
    
    @BeforeClass
    public static void startServers() throws Exception {
        startServer(new HashMap<String, String>() {{
            put("/notfound.json", NotFoundServlet.class.getName());
            put("/twitter-down.json", ServiceUnavailableServlet.class.getName());
            
            put("/user_timeline.json", TwitterJSONServlet.class.getName());
            put("/oauth2token", OAuthTokenServlet.class.getName());
        }});
    }
    
    @Test
    public void found() throws Exception {
        CachedTwitterTimelineFetcher fetcher = new CachedTwitterTimelineFetcher(Uri.parse(super.serverAddress + "user_timeline.json"), Uri.parse(super.serverAddress + "oauth2token"));
        fetcher.setConsumerKey(TEST_CONSUMER_KEY);
        fetcher.setConsumerSecret(TEST_CONSUMER_SECRET);
        fetcher.afterPropertiesSet();
        
        int buffer = TwitterJSONServlet.executionCount;
        int oauthBuffer = OAuthTokenServlet.executionCount;
        
        assertEquals("{ \"success\": true }", fetcher.get("matmannion", 20, true, false).getResponseBody());
        assertEquals(1, TwitterJSONServlet.executionCount - buffer);
        
        // Test caching; shouldn't have hit the servlet again
        assertEquals("{ \"success\": true }", fetcher.get("matmannion", 20, true, false).getResponseBody());
        assertEquals(1, TwitterJSONServlet.executionCount - buffer);
        
        // Test caching with different capitalisation; shouldn't have hit the servlet again
        assertEquals("{ \"success\": true }", fetcher.get("MatMannion", 20, true, false).getResponseBody());
        assertEquals(1, TwitterJSONServlet.executionCount - buffer);
        
        // Assert that we only went for an OAuth token once
        assertEquals(1, OAuthTokenServlet.executionCount - oauthBuffer);
    }
    
    @Test
    public void notFound() throws Exception {
        CachedTwitterTimelineFetcher fetcher = new CachedTwitterTimelineFetcher(Uri.parse(super.serverAddress + "notfound.json"), Uri.parse(super.serverAddress + "oauth2token"));
        fetcher.setConsumerKey(TEST_CONSUMER_KEY);
        fetcher.setConsumerSecret(TEST_CONSUMER_SECRET);
        fetcher.afterPropertiesSet();
        
        int buffer = NotFoundServlet.executionCount;
        int oauthBuffer = OAuthTokenServlet.executionCount;
        
        assertEquals(HttpStatus.SC_NOT_FOUND, fetcher.get("notfound", 20, true, false).getStatusCode());
        assertEquals(1, NotFoundServlet.executionCount - buffer);
        
        // Test caching; shouldn't have hit the servlet again
        assertEquals(HttpStatus.SC_NOT_FOUND, fetcher.get("notfound", 20, true, false).getStatusCode());
        assertEquals(1, NotFoundServlet.executionCount - buffer);
        
        // Assert that we only went for an OAuth token once
        assertEquals(1, OAuthTokenServlet.executionCount - oauthBuffer);
    }
    
    @Test
    public void twitterDown() throws Exception {
        CachedTwitterTimelineFetcher fetcher = new CachedTwitterTimelineFetcher(Uri.parse(super.serverAddress + "twitter-down.json"), Uri.parse(super.serverAddress + "oauth2token"));
        fetcher.setConsumerKey(TEST_CONSUMER_KEY);
        fetcher.setConsumerSecret(TEST_CONSUMER_SECRET);
        fetcher.afterPropertiesSet();
        
        int buffer = ServiceUnavailableServlet.executionCount;
        int oauthBuffer = OAuthTokenServlet.executionCount;
        
        assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, fetcher.get("twitter-down", 20, true, false).getStatusCode());
        assertEquals(1, ServiceUnavailableServlet.executionCount - buffer);
        
        // Test caching - this shouldn't be cached, so we should see it get hit again
        assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, fetcher.get("twitter-down", 20, true, false).getStatusCode());
        assertEquals(2, ServiceUnavailableServlet.executionCount - buffer);
        
        // Assert that we only went for an OAuth token once
        assertEquals(1, OAuthTokenServlet.executionCount - oauthBuffer);
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
            assertEquals("Bearer AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2FAAAAAAAAAAAAAAAAAAAA%3DAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", req.getHeader("Authorization"));
        	
            assertEquals("matmannion", req.getParameter("screen_name"));
            assertEquals("40", req.getParameter("count"));
            assertEquals("true", req.getParameter("include_rts"));
            assertEquals("false", req.getParameter("exclude_replies"));
            assertNull(req.getParameter("callback"));
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{ \"success\": true }");
            
            executionCount++;
        }
        
    }
    
    public static class OAuthTokenServlet extends HttpServlet {
    	
    	private static int executionCount = 0;

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertEquals("Basic eHZ6MWV2RlM0d0VFUFRHRUZQSEJvZzpMOHFxOVBaeVJnNmllS0dFS2hab2xHQzB2SldMdzhpRUo4OERSZHlPZw==", req.getHeader("Authorization"));
            assertEquals("application/x-www-form-urlencoded;charset=UTF-8", req.getHeader("Content-Type"));
            
            assertEquals("client_credentials", req.getParameter("grant_type"));
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"token_type\":\"bearer\",\"access_token\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2FAAAAAAAAAAAAAAAAAAAA%3DAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}");
            
            executionCount++;
        }
    }

}
