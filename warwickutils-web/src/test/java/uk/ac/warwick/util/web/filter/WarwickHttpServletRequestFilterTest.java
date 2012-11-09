package uk.ac.warwick.util.web.filter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

import uk.ac.warwick.util.web.WarwickHttpServletRequest;

import com.google.common.collect.Sets;

public final class WarwickHttpServletRequestFilterTest {
    
    private final Mockery m = new JUnit4Mockery();
    
    @Test
    public void itWorks() throws Exception {
        WarwickHttpServletRequestFilter filter = new WarwickHttpServletRequestFilter(Sets.<String>newHashSet());
        
        final HttpServletRequest req = m.mock(HttpServletRequest.class);
        final HttpServletResponse resp = m.mock(HttpServletResponse.class);
        final FilterChain chain = m.mock(FilterChain.class);
        
        m.checking(new Expectations() {{
            one(chain).doFilter(with(aNonNull(WarwickHttpServletRequest.class)), with(equal(resp)));
        }});
        
        filter.doFilter(req, resp, chain);
        
        m.assertIsSatisfied();
    }

}
