package uk.ac.warwick.util.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.util.StringUtils;

import uk.ac.warwick.util.web.URLBuilder;

public final class WarwickHttpServletRequest extends HttpServletRequestWrapper {
    
    public static final String REQUESTED_URI_HEADER_NAME = "X-Requested-URI";

    public static final String FORWARDED_FOR_HEADER_NAME = "X-Forwarded-For";

    public static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("[0-9]{1,3}(:?\\.[0-9]{1,3}){3}");
    
    private final Set<String> localAddresses;

    public WarwickHttpServletRequest(HttpServletRequest request, Set<String> theLocalAddresses) {
        super(request);
        this.localAddresses = theLocalAddresses;
    }

    @Override
    public String getHeader(String name) {
        if ("Host".equals(name)) {
            String requestedUrl = getRequestedURL();
            
            if (StringUtils.hasText(requestedUrl)) {
                try {
                    return new URLBuilder(requestedUrl).getHost();
                } catch (MalformedURLException e) {
                    return super.getHeader(name);
                }
            }
        }
        
        return super.getHeader(name);
    }

    /**
     * Return the IP address that requested this.
     */
    @Override
    public String getRemoteAddr() {
        String xForwardedFor = getHeader(FORWARDED_FOR_HEADER_NAME);

        if (xForwardedFor != null) {
            String[] addresses = xForwardedFor.split(",");
            for (int i = 0; i < addresses.length; i++) {
                String address = addresses[i].trim();
                if (IP_ADDRESS_PATTERN.matcher(address).matches() && isNonLocalAddress(address)) {
                    return address;
                }
            }
        }
        
        // we got all the way through the XFF headers (or there weren't any),
        // and didn't find any valid addresses, so try the request's remoteAddr
        return super.getRemoteAddr();
    }
    
    private boolean isNonLocalAddress(String address) {
        // checkstyle pacifiers: these are the lower and upper bounds for the second octet
        // of the 20-bit private address space.
        final int private20bitLower = 15;
        final int private20bitUpper = 32;
        //
        boolean local = false;
        if (localAddresses.contains(address)) {
            local = true;
        } else {
            /*
             * look for private network addresses: 127.0.0.1 10.*.*.* 192.168.*.*
             * 172.[16-31].*.*
             * 
             * I am assuming for now that no-one will ever be dumb enough to
             * forge an X-forwarded-for address that appears to come from 
             * a multicast-reserved address (224.0.0.0 to 239.255.255.255).
             * If lots of people did this, then they might all start to throttle each
             * other, which, frankly, would serve them right
             * 
             * I have included the 169.254.*.* windows autoconf address range in, even though a computer that's connected 
             * to the 'net ought never to have an autoconf IP address, because otherwise it's bound to happen.
             * 
             */
            if ("127.0.0.1".equals(address) || address.startsWith("10.") || address.startsWith("192.168") || address.startsWith("169.254")) {
                local = true;
            }else if (address.startsWith("172")) {
                String[] bits = address.split("\\.");
                int secondOctet = Integer.parseInt(bits[1]);
                if (secondOctet > private20bitLower && secondOctet <private20bitUpper) {
                    local = true;
                }
            }
            
        }
        return !local;
    }

    /**
     * Return the requested URI, excluding the query string.
     */
    @Override
    public String getRequestURI() {
        String requestedUrl = getRequestedURL();
        
        if (StringUtils.hasText(requestedUrl)) {
            try {
                return new URLBuilder(requestedUrl).toURL().getPath();
            } catch (MalformedURLException e) {
                return super.getRequestURI();
            }
        }
        
        return super.getRequestURI();
    }

    /**
     * Return the entire requested URL, including query string.
     */
    @Override
    public StringBuffer getRequestURL() {
        String requestedUrl = getRequestedURL();
        
        if (StringUtils.hasText(requestedUrl)) {
            return new StringBuffer(requestedUrl);
        }
        
        return super.getRequestURL();
    }
    
    private String getRequestedURL() {
        return getHeader(REQUESTED_URI_HEADER_NAME);
    }
    
    private URL getURL() throws MalformedURLException {
        String requestedUrl = getRequestedURL();
        
        if (StringUtils.hasText(requestedUrl)) {
            return new URLBuilder(requestedUrl).toURL();
        }
        
        return null;
    }

    @Override
    public String getQueryString() {
        try {
            URL url = getURL();
            return url == null ? super.getQueryString() : url.getQuery();
        } catch (MalformedURLException e) {
            return super.getQueryString();
        }
    }

    @Override
    public String getScheme() {
        try {
            URL url = getURL();
            return url == null ? super.getScheme() : url.getProtocol();
        } catch (MalformedURLException e) {
            return super.getScheme();
        }
    }

    @Override
    public String getServerName() {
        try {
            URL url = getURL();
            return url == null ? super.getServerName() : url.getHost();
        } catch (MalformedURLException e) {
            return super.getServerName();
        }
    }

    @Override
    public int getServerPort() {
        try {
            URL url = getURL();
            if (url != null) {
                return url.getPort() > 0 ? url.getPort() : url.getDefaultPort(); 
            }
            
            return super.getServerPort();
        } catch (MalformedURLException e) {
            return super.getServerPort();
        }
    }

    @Override
    public boolean isSecure() {
        return getServerPort() == 443 || getServerPort() == 8443;
    }

}
