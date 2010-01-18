package uk.ac.warwick.util.web;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.common.collect.Sets;

public class WarwickHttpServletRequestTest {

    @Test
    public void getHeaderString() {
        MockHttpServletRequest original = new MockHttpServletRequest();
        WarwickHttpServletRequest wrapped = new WarwickHttpServletRequest(original, Sets.<String>newHashSet());
        
        original.addHeader("Exists", "Test");
        original.addHeader("Host", "localhost:8080");
        original.addHeader(WarwickHttpServletRequest.REQUESTED_URI_HEADER_NAME, "http://start.warwick.ac.uk/something?yes=no");
        
        assertEquals("Test", wrapped.getHeader("Exists"));
        assertNull(wrapped.getHeader("NonExistant"));
        assertEquals("start.warwick.ac.uk", wrapped.getHeader("Host"));
    }

    @Test
    public void getRemoteAddr() {
        MockHttpServletRequest original = new MockHttpServletRequest();
        WarwickHttpServletRequest wrapped = new WarwickHttpServletRequest(original, Sets.newHashSet("137.205.194.132"));
        
        original.setRemoteAddr("1.2.3.4");
        
        assertEquals("1.2.3.4", wrapped.getRemoteAddr());
        
        original.addHeader(WarwickHttpServletRequest.FORWARDED_FOR_HEADER_NAME, "127.0.0.1,137.205.194.132,1.2.3.4");
        
        assertEquals("1.2.3.4", wrapped.getRemoteAddr());
    }

    @Test
    public void getRequestURI() {
        MockHttpServletRequest original = new MockHttpServletRequest();
        WarwickHttpServletRequest wrapped = new WarwickHttpServletRequest(original, Sets.<String>newHashSet());
        
        original.addHeader(WarwickHttpServletRequest.REQUESTED_URI_HEADER_NAME, "http://start.warwick.ac.uk/something?yes=no");
        
        assertEquals("http://start.warwick.ac.uk/something", wrapped.getRequestURI());
    }

    @Test
    public void getRequestURL() {
        MockHttpServletRequest original = new MockHttpServletRequest();
        WarwickHttpServletRequest wrapped = new WarwickHttpServletRequest(original, Sets.<String>newHashSet());
        
        original.addHeader(WarwickHttpServletRequest.REQUESTED_URI_HEADER_NAME, "http://start.warwick.ac.uk/something?yes=no");
        
        assertEquals("http://start.warwick.ac.uk/something?yes=no", wrapped.getRequestURL().toString());
    }

}
