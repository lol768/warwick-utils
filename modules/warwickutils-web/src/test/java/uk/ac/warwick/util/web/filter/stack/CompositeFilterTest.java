package uk.ac.warwick.util.web.filter.stack;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class CompositeFilterTest {

    private MockFilter f1;
    private MockFilter f2;
    private MockFilter f3;
    private MockFilter f4;
    private List<Filter> filters;
    private MockHttpServletRequest req;
    private MockHttpServletResponse res;
    private MockFilterChain filterChain;

    @Before
    public void setUp() throws Exception {
        f1 = new MockFilter(true);
        f2 = new MockFilter(true);
        f3 = new MockFilter(true);
        f4 = new MockFilter(false);
        req = new MockHttpServletRequest();
        res = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test public void allFiltersRun() throws Exception {
        filters = Arrays.asList((Filter)f1,f2,f3);
        CompositeFilter filter = new CompositeFilter(filters);
        filter.doFilter(req, res, filterChain);
        
        assertEquals(1, f1.getInvocationCount());
        assertEquals(1, f2.getInvocationCount());
        assertEquals(1, f3.getInvocationCount());
        assertSame(req, filterChain.getRequest());
        assertSame(res, filterChain.getResponse());
    }
    
    /**
     * Check that if one filter doesn't call the chain, then
     * the remainder of the filters are abandoned.
     */
    @Test public void failedFilter() throws Exception {
        filters = Arrays.asList((Filter)f1,f2,f4,f3);
        CompositeFilter filter = new CompositeFilter(filters);
        filter.doFilter(req, res, filterChain);
        
        assertEquals(1, f1.getInvocationCount());
        assertEquals(1, f2.getInvocationCount());
        assertEquals(1, f4.getInvocationCount());
        assertEquals(0, f3.getInvocationCount());
        assertNull(filterChain.getRequest());
        assertNull(filterChain.getResponse());
    }
    
    /**
     * Check that a filter can pass a different request through
     * the chain and the composite filter will handle it.
     */
    @Test public void requestObjectChanges() throws Exception {
        MockHttpServletRequest swappedRequest = new MockHttpServletRequest();
        f2.setSwapRequest(swappedRequest);
        
        filters = Arrays.asList((Filter)f1,f2,f3);
        CompositeFilter filter = new CompositeFilter(filters);
        filter.doFilter(req, res, filterChain);
        
        assertEquals(1, f1.getInvocationCount());
        assertEquals(1, f2.getInvocationCount());
        assertEquals(1, f3.getInvocationCount());
        assertSame(swappedRequest, filterChain.getRequest());
        assertSame(res, filterChain.getResponse());
    }
    
    /**
     * Default behaviour of CompositeFilter is to flatten out the contents
     * of nested CompositeFilters into itself.
     */
    @Test public void flattening() throws Exception {
        CompositeFilter filter1 = new CompositeFilter(Arrays.asList((Filter)f1,f2,f3));
        CompositeFilter filter2 = new CompositeFilter(Arrays.asList((Filter)f1,filter1,f1));
        assertEquals(5, filter2.getFilters().size());
        
        //no flattening
        CompositeFilter filter3 = new CompositeFilter(Arrays.asList((Filter)f1,filter1,f1), false);
        assertEquals(3, filter3.getFilters().size());
        assertSame(filter1, filter3.getFilters().get(1));
    }
    
    @Test(expected=UnsupportedOperationException.class) 
    public void cannotModifyFilters() throws Exception {
        filters = Arrays.asList((Filter)f1,f2,f3);
        CompositeFilter filter = new CompositeFilter(filters);
        filter.getFilters().clear();
    }
    
    /**
     * We need to actually keep the doFilters in the stack - it's not enough to
     * just execute them in turn as teardown code runs after doFilter.
     * 
     * Check that when we're in the second doFilter, the first one hasn't
     * returned yet.
     */
    @Test public void afterChain() throws Exception {
        final Filter f = new Filter() {
            public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
                assertFalse("first filter shouldn't have returned while still in second filter", f1.isFinished());
            }

            public void destroy() {}
            public void init(FilterConfig config) throws ServletException {}
        };
        filters = Arrays.asList((Filter)f1,f);
        CompositeFilter filter = new CompositeFilter(filters);
        filter.doFilter(req, res, filterChain);
    }

}
