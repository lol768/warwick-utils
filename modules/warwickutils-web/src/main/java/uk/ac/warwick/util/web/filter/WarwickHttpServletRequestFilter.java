package uk.ac.warwick.util.web.filter;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import uk.ac.warwick.util.web.WarwickHttpServletRequest;

/**
 * Filter that wraps the {@link HttpServletRequest} in a
 * {@link WarwickHttpServletRequest}, so that methods such as
 * {@link HttpServletRequest#getRequestURL()} etc. return the correct requested
 * URL, even if it is from behind a reverse Apache proxy.
 * 
 * @author Mat
 */
public final class WarwickHttpServletRequestFilter implements Filter {
    
    private final Set<String> localIpAddresses;
    
    public WarwickHttpServletRequestFilter(Set<String> theLocalIpAddresses) {
        this.localIpAddresses = theLocalIpAddresses;
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new WarwickHttpServletRequest((HttpServletRequest)req, localIpAddresses), resp);
    }

    public void init(FilterConfig arg0) throws ServletException {}
    public void destroy() {}

}
