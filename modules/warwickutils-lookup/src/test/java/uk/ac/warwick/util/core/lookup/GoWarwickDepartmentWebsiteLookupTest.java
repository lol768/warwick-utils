package uk.ac.warwick.util.core.lookup;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.warwick.util.AbstractJUnit4JettyTest;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.web.Uri;

@SuppressWarnings("serial")
public final class GoWarwickDepartmentWebsiteLookupTest extends AbstractJUnit4JettyTest {
    
    @BeforeClass
    public static void startServers() throws Exception {
        startServer(new HashMap<String, String>() {{
            put("/404", NotFoundServlet.class.getName());
            put("/found", RedirectFoundServlet.class.getName());
            put("/not-found", RedirectNotFoundServlet.class.getName());
            put("/invalid-json", InvalidJSONServlet.class.getName());
        }});
    }
    
    @Test
    public void found() {
        GoWarwickDepartmentWebsiteLookup lookup = new GoWarwickDepartmentWebsiteLookup(Uri.parse(super.serverAddress + "found"));
        
        assertEquals("http://www2.warwick.ac.uk/services/its", lookup.getWebsiteForDepartmentCode("IN"));
        
        // do it again to test caching behaviour
        assertEquals("http://www2.warwick.ac.uk/services/its", lookup.getWebsiteForDepartmentCode("IN"));
    }
    
    @Test
    public void notFound() {
        GoWarwickDepartmentWebsiteLookup lookup = new GoWarwickDepartmentWebsiteLookup(Uri.parse(super.serverAddress + "not-found"));
        
        assertNull(lookup.getWebsiteForDepartmentCode("NF"));
        
        // do it again to test caching behaviour
        assertNull(lookup.getWebsiteForDepartmentCode("NF"));
    }
    
    @Test
    public void invalid() {
        GoWarwickDepartmentWebsiteLookup lookup = new GoWarwickDepartmentWebsiteLookup(Uri.parse(super.serverAddress + "invalid-json"));
        
        assertNull(lookup.getWebsiteForDepartmentCode("IV"));
        
        // do it again to test caching behaviour
        assertNull(lookup.getWebsiteForDepartmentCode("IV"));
    }
    
    @Test 
    public void fourOhFour() {
        GoWarwickDepartmentWebsiteLookup lookup = new GoWarwickDepartmentWebsiteLookup(Uri.parse(super.serverAddress + "404"));
        
        assertNull(lookup.getWebsiteForDepartmentCode("FO"));
        
        // do it again to test caching behaviour
        assertNull(lookup.getWebsiteForDepartmentCode("FO"));
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
        assertTrue(Caches.newCache(GoWarwickDepartmentWebsiteLookup.CACHE_NAME, null, 0, CacheStrategy.EhCacheRequired).clear());
    }
    
    public static class RedirectFoundServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertEquals("dep-code-in", req.getParameter("path"));
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{ found: true , redirect: { path: \"dep-code-in\" , internal: true , target: \"http://www2.warwick.ac.uk/services/its\" , enabled: true , approved: true } }");
        }
    }
    
    public static class RedirectNotFoundServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{ found: false }");
        }
    }
    
    public static class InvalidJSONServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Error 500 Something has gone horribly wrong. OH BUT WE RETURNED A 200 OH WELL");
        }
    }

}
