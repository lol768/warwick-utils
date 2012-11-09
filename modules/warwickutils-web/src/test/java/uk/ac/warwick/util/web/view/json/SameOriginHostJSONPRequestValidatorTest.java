package uk.ac.warwick.util.web.view.json;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public final class SameOriginHostJSONPRequestValidatorTest {
    
    @Test
    public void noRefererUsesDefault() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        SameOriginHostJSONPRequestValidator validator = new SameOriginHostJSONPRequestValidator("www2.warwick.ac.uk");
        
        validator.setValidByDefault(true);
        assertTrue(validator.isAllow(request));
        
        validator.setValidByDefault(false);
        assertFalse(validator.isAllow(request));
    }
    
    @Test
    public void differentReferer() throws Exception {
        SameOriginHostJSONPRequestValidator validator = new SameOriginHostJSONPRequestValidator("www2.warwick.ac.uk");
        validator.setValidByDefault(false);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("referer", "http://start.warwick.ac.uk/malicious.jsp");
        
        assertFalse(validator.isAllow(request));
    }
    
    @Test
    public void differentRefererWithFullURLConstructor() throws Exception {
        SameOriginHostJSONPRequestValidator validator = new SameOriginHostJSONPRequestValidator("http://www2.warwick.ac.uk/");
        validator.setValidByDefault(false);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("referer", "http://start.warwick.ac.uk/malicious.jsp");
        
        assertFalse(validator.isAllow(request));
    }
    
    @Test
    public void sameHostReferer() throws Exception {
        SameOriginHostJSONPRequestValidator validator = new SameOriginHostJSONPRequestValidator("www2.warwick.ac.uk");
        validator.setValidByDefault(false);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("referer", "http://www2.warwick.ac.uk/nice.jsp");
        
        assertTrue(validator.isAllow(request));
    }
    
    @Test
    public void sameHostRefererWithFullURLConstructor() throws Exception {
        SameOriginHostJSONPRequestValidator validator = new SameOriginHostJSONPRequestValidator("http://www2.warwick.ac.uk/");
        validator.setValidByDefault(false);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("referer", "http://www2.warwick.ac.uk/nice.jsp");
        
        assertTrue(validator.isAllow(request));
    }

}
