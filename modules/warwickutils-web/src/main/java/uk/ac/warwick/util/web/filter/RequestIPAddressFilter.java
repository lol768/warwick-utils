package uk.ac.warwick.util.web.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public final class RequestIPAddressFilter extends AbstractFilter {
    
    public static final String CURRENT_IP_ATTRIBUTE = "REMOTE_IP_ADDRESS";
    public static final String CURRENT_IP_STRING_ATTRIBUTE = "REMOTE_IP_ADDRESS_STRING";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestIPAddressFilter.class);

    private final Collection<String> disallowedIpAddresses;
    
    public RequestIPAddressFilter(final Collection<String> localIps) {
        this.disallowedIpAddresses = ImmutableSet.copyOf(localIps);
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp = (HttpServletResponse)response;
        
        RequestIPAddressHolderImpl requestIPAddressHolderImpl = new RequestIPAddressHolderImpl(req, disallowedIpAddresses);
        req.setAttribute(CURRENT_IP_ATTRIBUTE, requestIPAddressHolderImpl);
        req.setAttribute(CURRENT_IP_STRING_ATTRIBUTE, requestIPAddressHolderImpl.getNonLocalAddress());
        
        chain.doFilter(req, resp);
    }
    
    public static RequestIPAddressHolder get(HttpServletRequest request) {
        RequestIPAddressHolder holder = (RequestIPAddressHolder) request.getAttribute(CURRENT_IP_ATTRIBUTE); 
        if (holder == null) {
            LOGGER.error("No RequestIPAddressHolder in request; not gone through RequestIPAddressFilter???");
            return new RequestIPAddressHolderImpl(request, Sets.<String>newHashSet());
        }
        
        return holder;
    }

}
