package uk.ac.warwick.util.web.filter;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * Convenience implementation.
 *
 * Consider using {@link AbstractHttpFilter} because you just know it's
 * an HTTP server anyway.
 * 
 * @see AbstractHttpFilter
 */
public abstract class AbstractFilter implements Filter {
    public void destroy() { }
    public void init(final FilterConfig arg0) throws ServletException { }
    
}
