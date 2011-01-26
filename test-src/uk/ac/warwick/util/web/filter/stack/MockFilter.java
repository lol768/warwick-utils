/**
 * 
 */
package uk.ac.warwick.util.web.filter.stack;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class MockFilter implements Filter {
    private int invocationCount;
    private boolean finished;
    private boolean succeed = true;
    private ServletRequest swapRequest;
    
    public MockFilter() {
        this(true);
    }
    
    public MockFilter(final boolean succeeds) {
        this.succeed = succeeds;
    }
    
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (finished) {
            throw new IllegalStateException("Already ran this filter");
        }
        invocationCount++;
        if (succeed) {
            ServletRequest request = req;
            if (swapRequest != null) {
                request = swapRequest;
            }
            chain.doFilter(request, res);
        }
        finished = true;
    }

    public void destroy() {}
    public void init(FilterConfig arg0) throws ServletException {}

    public int getInvocationCount() {
        return invocationCount;
    }

    public void setSwapRequest(ServletRequest swapRequest) {
        this.swapRequest = swapRequest;
    }

    public boolean isFinished() {
        return finished;
    }
    
}