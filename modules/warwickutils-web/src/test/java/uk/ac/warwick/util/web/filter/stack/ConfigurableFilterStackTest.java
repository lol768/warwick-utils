package uk.ac.warwick.util.web.filter.stack;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;
import static uk.ac.warwick.util.MockUtils.*;

public class ConfigurableFilterStackTest {

    ConfigurableFilterStack filter;
    Mockery ctx;
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    private Filter f1;
    private Filter f2;
    private Filter f3;
    
    @Before public void setup() {
        ctx = new Mockery();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRequestURI("/render/renderPage.htm");
        f1 = ctx.mock(Filter.class, "f1");
        f2 = ctx.mock(Filter.class, "f2");
        f3 = ctx.mock(Filter.class, "f3");
    }

    @After public void tearDown() {
        if (filter != null) {
            filter.getCache().invalidateAll();
        }
    }
    
    /**
     * Creates a single FilterStackSet with three filters.
     * 
     * Asserts that they are all executed in order - f1, f2, f3.
     * 
     * Asserts that they are executed in a stack - code after
     * f2 calls the chain should not have been executed while
     * f3 is in its doFilter(). We check this by setting request
     * attributes and checking for their existence.
     */
    @Test public void singleStackSet() throws Exception {              
        checkingFiltersRunInOrder();
        
        List<Filter> filters = asList(f1,f2,f3);
        FilterStackSet set = new FilterStackSet(filters, asList("/render/*"));
        filter = new ConfigurableFilterStack(asList(set));
        filter.afterPropertiesSet();
        
        filter.doFilter(request, response, new MockFilterChain());
        ctx.assertIsSatisfied();
    }
    
    /**
     * Filters are separated into two FilterStackSets, with different
     * URL patterns. As long as the request matches both patterns, the behaviour
     * should be the same as a single FilterStackSet (see {@link #singleStackSet()}).
     */
    @Test public void twoStackSets() throws Exception {
        checkingFiltersRunInOrder();
        
        FilterStackSet set1 = new FilterStackSet(asList(f1,f2), asList("/render/*"));
        FilterStackSet set2 = new FilterStackSet(asList(f3), asList("/*"));
        filter = new ConfigurableFilterStack(asList(set1, set2));
        filter.afterPropertiesSet();
        
        filter.doFilter(request, response, new MockFilterChain());
        ctx.assertIsSatisfied();
    }
    
    /**
     * Check that if you define two mappings for the same filter on the same URL,
     * it will run the filter twice and doesn't decide to merge them together.
     */
    @Test public void twoStackSetsSameFilter() throws Exception {
        ctx.checking(new Expectations(){{
            final Sequence filterOrder = ctx.sequence("filterOrder");
            exactly(2).of(f1).doFilter(with(same(request)), with(same(response)), with(any(FilterChain.class)));
            will(continueFilterChain()); 
            inSequence(filterOrder);
        }});
        
        FilterStackSet set1 = new FilterStackSet(asList(f1), asList("/render/*"));
        FilterStackSet set2 = new FilterStackSet(asList(f1), asList("/render/*"));
        
        filter = new ConfigurableFilterStack(asList(set1, set2));
        filter.afterPropertiesSet();
        
        filter.doFilter(request, response, new MockFilterChain());
        ctx.assertIsSatisfied();
    }
    
    /**
     * This test is actually to demonstrate an exclusion mapping that _doesn't_ work.
     * Currently * only works directly after a / - you can't put it just anywhere. This is
     * a limitation of the Servlet Filter spec, but there's no reason we couldn't make
     * our filter stack more advanced and accept this format.
     *
     * Edit: as of UTL-106, more flexible filter stack formats are permitted!
     *       This test now works as expected, ~8 years later \o/
     */
    @Test public void exclusion() throws Exception {
        ctx.checking(new Expectations(){{
            final Sequence filterOrder = ctx.sequence("filterOrder");
            never(f1);
            will(continueFilterChain());
            inSequence(filterOrder);
        }});
        //                                      filters            inclusions                  exclusions
        FilterStackSet set = new FilterStackSet(asList(f1), asList("/api/dataentry/*"), asList("/api/*/entries.*"));
        
        filter = new ConfigurableFilterStack(asList(set));
        filter.afterPropertiesSet();
        request.setRequestURI("/api/dataentry/entries.json");
        filter.doFilter(request, response, new MockFilterChain());
        ctx.assertIsSatisfied();
    }
    
    /**
     * Two FilterStackSets exist but the request matches only one of them.
     * Only the matching set should be executed.
     */
    @Test public void twoStackSetsOneMatch() throws Exception {
        ctx.checking(new Expectations(){{
            final Sequence filterOrder = ctx.sequence("filterOrder");
            one(f1).doFilter(with(same(request)), with(same(response)), with(any(FilterChain.class)));
                will(continueFilterChain()); 
                inSequence(filterOrder);
            one(f2).doFilter(with(same(request)), with(same(response)), with(any(FilterChain.class)));
                will(continueFilterChain());
                inSequence(filterOrder);
            never(f3);    
        }});
        
        FilterStackSet set1 = new FilterStackSet(asList(f1,f2), asList("/render/*"));
        FilterStackSet set2 = new FilterStackSet(asList(f3), asList("*.css"));
        filter = new ConfigurableFilterStack(asList(set1, set2));
        filter.afterPropertiesSet();
        
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertNotNull("should continue chain even if match not made", chain.getRequest());
        ctx.assertIsSatisfied();
    }
    
    /**
     * Check that GenericFilterBean doesn't blow up when initialised through
     * {@link ConfigurableFilterStack}. We build our own FilterConfig object
     * so need to make sure the right stuff is returned.
     */
    @Test public void genericFilterBeanInit() throws Exception {
        Filter f = new GenericFilterBean(){
            public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {}
        };
        FilterStackSet set = new FilterStackSet(asList(f), asList("*.css"));
        filter = new ConfigurableFilterStack(asList(set));
        FilterConfig cfg = new MockFilterConfig("helloFilter");
        filter.init(cfg);
    }

    /**
     * Adds expectations to mock filters f1,f2,f3 that they will be called in
     * order, and that f3 will be called inside f2's doFilter method (checked by
     * setting and reading request attributes).
     */
    private void checkingFiltersRunInOrder() throws IOException, ServletException {
        ctx.checking(new Expectations(){{
            final Sequence filterOrder = ctx.sequence("filterOrder");
            one(f1).doFilter(with(same(request)), with(same(response)), with(any(FilterChain.class)));
                will(continueFilterChain()); 
                inSequence(filterOrder);
            one(f2).doFilter(with(same(request)), with(same(response)), with(any(FilterChain.class)));
                will(doAll(
                        setAttribute("filter2started", "value"),
                        continueFilterChain(),
                        setAttribute("filter2finished", "value")));
                inSequence(filterOrder);
            one(f3).doFilter(with(same(request)), with(same(response)), with(any(FilterChain.class))); 
                will(doAll(
                        checkAttribute("filter2started", true),
                        checkAttribute("filter2finished", false),
                        continueFilterChain()
                        ));
                inSequence(filterOrder);
                
        }});
    }
}
