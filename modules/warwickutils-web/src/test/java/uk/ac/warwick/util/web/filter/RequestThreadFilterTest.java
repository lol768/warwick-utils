package uk.ac.warwick.util.web.filter;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


public class RequestThreadFilterTest {
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;
    
    @Before
    public void before() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }
    
    @Test public void runRequest() throws Exception {
        RequestThreadFilter filter = new RequestThreadFilter();
        
        filter.doFilter(request, response, chain);
    }
}
