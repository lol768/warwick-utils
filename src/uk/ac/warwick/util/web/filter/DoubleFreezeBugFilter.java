package uk.ac.warwick.util.web.filter;

import static com.google.common.collect.Iterables.*;
import static java.util.Collections.*;
import static uk.ac.warwick.util.core.NumberUtils.*;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Predicate;

public class DoubleFreezeBugFilter implements Filter {

    private static final int TOO_LONG_TO_CHECK = 1100;
    private RejectionHandler rejectionHandler;
    //private static final Logger LOGGER = Logger.getLogger(DoubleFreezeBugFilter.class);
    
    public void init(FilterConfig arg0) throws ServletException {}
    public void destroy() {}

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        doHttpFilter((HttpServletRequest)req, (HttpServletResponse)res, chain);
    }
    
    public void doHttpFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (areHeadersGood(request) && areParametersGood(request)) {
            chain.doFilter(request, response);
        } else {
            reject(request, response);
        }
    }
    
    @SuppressWarnings("unchecked")
    private boolean areParametersGood(final HttpServletRequest request) {       
        return all(list(request.getParameterNames()), new Predicate<String>() {
            public boolean apply(String paramName) {
                return isParameterGood(request.getParameter(paramName));
            }
        });
    }

    private boolean isParameterGood(String value) {
        return value == null || value.length() > TOO_LONG_TO_CHECK || !isPossibleBugDouble(value);
    }
    
    private boolean areHeadersGood(HttpServletRequest request) {
        return isHeaderGood(request.getHeader("Accept-Language"));
    }
    
    private boolean isHeaderGood(String headerValue) {
        // a missing header is a good header.
        return headerValue == null || !containsPossibleBugDouble(headerValue);
    }
    
    private void reject(HttpServletRequest req, HttpServletResponse res) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        if (rejectionHandler != null) {
            rejectionHandler.handleRequest(req,res); 
        }
    }
    
    public void setRejectionHandler(RejectionHandler controller) {
        rejectionHandler = controller;
    }
    
    static interface RejectionHandler {
        void handleRequest(HttpServletRequest request, HttpServletResponse response);
    }
    
//  private <T> T logReturn(T o, String name) {
//      System.err.println(name + ": " + o);
//      return o;
//  }
}
