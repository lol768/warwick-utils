package uk.ac.warwick.util.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * For people too lazy to even cast from Servlet* to HttpServlet* themselves.
 */
public abstract class AbstractHttpFilter extends AbstractFilter {

    public final void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest)req, (HttpServletResponse)res, chain);
    }
    
    public abstract void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)  throws IOException, ServletException;

}
