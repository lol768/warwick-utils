package uk.ac.warwick.util.core;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;

public class HttpUtilsTest extends TestCase {
    public void testIsAbsolutePath() {
        assertTrue(HttpUtils.isAbsoluteAddress("http://www.warwick.ac.uk/file.gif"));
        assertTrue(HttpUtils.isAbsoluteAddress("www.warwick.ac.uk/file.gif"));
        assertTrue(HttpUtils.isAbsoluteAddress("crabs.com/file.gif"));
        
        assertFalse(HttpUtils.isAbsoluteAddress("video.avi"));
        assertFalse(HttpUtils.isAbsoluteAddress("super-flash/file.gif"));
        assertFalse(HttpUtils.isAbsoluteAddress("blah/relative.htm/feet/file.gif"));
        assertFalse(HttpUtils.isAbsoluteAddress("../super-flash/file.gif"));
        
        /**
         * This would be taken as absolute, because it starts relative.fish,
         * there's no simple way to know if this is a domain or a subdirectory.
         * Fortunately we don't allow dots in page names any more, so this
         * shouldn't matter. 
         */
        assertTrue(HttpUtils.isAbsoluteAddress("relative.fish/feet/file.gif"));
    }
    
    public void testGetBooleanRequestAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        assertFalse(HttpUtils.getBooleanRequestAttribute(request, "test", false));
        assertTrue(HttpUtils.getBooleanRequestAttribute(request, "test", true));
        
        request.setAttribute("test", false);
        assertFalse(HttpUtils.getBooleanRequestAttribute(request, "test", false));
        assertFalse(HttpUtils.getBooleanRequestAttribute(request, "test", true));
        
        //test with Boolean objects too.
        request.setAttribute("test", Boolean.valueOf(false));
        assertFalse(HttpUtils.getBooleanRequestAttribute(request, "test", false));
        assertFalse(HttpUtils.getBooleanRequestAttribute(request, "test", true));
        
        request.setAttribute("test", Boolean.TRUE);
        assertTrue(HttpUtils.getBooleanRequestAttribute(request, "test", false));
        assertTrue(HttpUtils.getBooleanRequestAttribute(request, "test", true));
    }
}
