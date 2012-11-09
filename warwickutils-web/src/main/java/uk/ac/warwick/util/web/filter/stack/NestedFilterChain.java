/**
 * 
 */
package uk.ac.warwick.util.web.filter.stack;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * FilterChain which will execute the given filters,
 * and then continue with the given chain. 
 */
class NestedFilterChain implements FilterChain {
    private final Queue<Filter> filterQueue;
    private final FilterChain originalChain;
    
    public NestedFilterChain(final List<Filter> filters, final FilterChain chain) {
        this.originalChain = chain;
        this.filterQueue = new LinkedBlockingQueue<Filter>(filters);
    }
    
    public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        if (filterQueue.isEmpty()) {
            originalChain.doFilter(req, res);
        } else {
            Filter filter = filterQueue.poll();
            filter.doFilter(req, res, this);
        }
    }
}