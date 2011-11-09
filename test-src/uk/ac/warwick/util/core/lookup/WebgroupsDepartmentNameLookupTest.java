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
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.AbstractJUnit4JettyTest;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.web.Uri;

public final class WebgroupsDepartmentNameLookupTest extends AbstractJUnit4JettyTest {
    
    @Test
    public void getDepartment() {
        WebgroupsDepartmentNameLookup lookup = new WebgroupsDepartmentNameLookup(Uri.parse(super.serverAddress + "departments"));
        assertEquals("Information Technology Services", lookup.getNameForDepartmentCode("IN"));
        assertEquals("Information Technology Services", lookup.getNameForDepartmentCode("in"));
        assertNull(lookup.getNameForDepartmentCode("nodep"));
        assertNull(lookup.getNameForDepartmentCode("nodep"));
        assertNull(lookup.getNameForDepartmentCode("unknown"));
        assertNull(lookup.getNameForDepartmentCode("unknown"));
        assertEquals("Biological Sciences", lookup.getNameForDepartmentCode("bs"));
    }
    
    @Test
    public void getDepartmentCached() {
        WebgroupsDepartmentNameLookup lookup = new WebgroupsDepartmentNameLookup(Uri.parse(super.serverAddress + "departments-onceOnly"));
        assertEquals("Information Technology Services", lookup.getNameForDepartmentCode("IN"));
        assertEquals("Information Technology Services", lookup.getNameForDepartmentCode("in"));
        
        assertTrue(Caches.newCache(WebgroupsDepartmentNameLookup.CACHE_NAME, null, 0, CacheStrategy.EhCacheRequired).clear());
        
        assertNull(lookup.getNameForDepartmentCode("in"));
    }
    
    @Test
    public void getDepartmentWhenServiceDown() {
        WebgroupsDepartmentNameLookup lookup = new WebgroupsDepartmentNameLookup(Uri.parse(super.serverAddress + "webgroupsdown"));
        assertNull(lookup.getNameForDepartmentCode("IN"));
        assertNull(lookup.getNameForDepartmentCode("in"));
    }
    
    @BeforeClass
    public static void setupEhCache() throws Exception {
        System.setProperty("warwick.ehcache.disk.store.dir", root.getAbsolutePath());
    }
    
    @AfterClass
    public static void unsetEhCache() throws Exception {
        System.clearProperty("warwick.ehcache.disk.store.dir");
    }
    
    @After
    public void emptyCache() {
        assertTrue(Caches.newCache(WebgroupsDepartmentNameLookup.CACHE_NAME, null, 0, CacheStrategy.EhCacheRequired).clear());
    }
    
    @SuppressWarnings("serial")
    @BeforeClass
    public static void startServers() throws Exception {
        startServer(new HashMap<String, String>() {{
            put("/departments", DepartmentsAllServlet.class.getName());
            put("/departments-onceOnly", DepartmentsAllOnceOnlyServlet.class.getName());
            put("/webgroupsdown", ServiceUnavailableServlet.class.getName());
        }});
    }
    
    @SuppressWarnings("serial")
    public static class DepartmentsAllServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            FileCopyUtils.copy(WebgroupsDepartmentNameLookupTest.class.getResourceAsStream("/all-departments.xml"), resp.getOutputStream());
        }
    }
    
    @SuppressWarnings("serial")
    public static class DepartmentsAllOnceOnlyServlet extends HttpServlet {
        private static boolean called = false;
        
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if (called) {
                fail("Should only have been called once");
            }
            
            called = true;
            
            FileCopyUtils.copy(WebgroupsDepartmentNameLookupTest.class.getResourceAsStream("/all-departments.xml"), resp.getOutputStream());
        }
    }

}
