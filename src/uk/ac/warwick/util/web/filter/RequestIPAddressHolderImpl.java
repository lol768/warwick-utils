package uk.ac.warwick.util.web.filter;

import java.util.Collection;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public final class RequestIPAddressHolderImpl implements RequestIPAddressHolder {
    
    public static final Logger LOGGER = Logger.getLogger(RequestIPAddressHolderImpl.class); 

    public static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("[0-9]{1,3}(:?\\.[0-9]{1,3}){3}");

    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final String ip;

    private boolean gotNonLocalAddress = true;// assume address is

    // non-local, will check
    // before returning from
    // constructor...

    public RequestIPAddressHolderImpl(HttpServletRequest request, Collection<String> localAddresses) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER);

        if (xForwardedFor != null) {
            String[] addresses = xForwardedFor.split(",");
            for (int i = 0; i < addresses.length; i++) {
                String address = addresses[i].trim();
                if (IP_ADDRESS_PATTERN.matcher(address).matches() && isNonLocalAddress(localAddresses, address)) {
                    ip = address;
                    return;
                }
            }
        }
        // we got all the way through the XFF headers (or there weren't
        // any),
        // and didn't find any valid addresses, so try the request's
        // remoteAddr
        ip = request.getRemoteAddr();
        if (!isNonLocalAddress(localAddresses, ip)) {
            gotNonLocalAddress = false;
            LOGGER.warn("Couldn't resolve a non-local address from XFF " + xForwardedFor + " and remoteAddr " + ip);
        }
    }

    private boolean isNonLocalAddress(Collection<String> localAddresses, String address) {
        // checkstyle pacifiers: these are the lower and upper bounds for the second octet
        // of the 20-bit private address space.
        final int private20bitLower = 16;
        final int private20bitUpper = 31;
        
        // the address space in the 20-bit private address space that wireless hotspots provide
        final int hotspot20bitLower = 31;
        final int hotspot20bitUpper = 31;

        // the address space in the 20-bit private address space that resnet-secure provides
        final int resnet20bitLower = 29;
        final int resnet20bitUpper = 29;

        boolean local = false;
        if (localAddresses.contains(address)) {
            local = true;
        } else {
            /*
             * look for private network addresses: 127.0.0.1 10.*.*.* 192.168.*.*
             * 172.[16-31].*.*
             * 
             * We allow 172.31.*.* as we serve that from Warwick's hotspots.
             * Also allow 172.29.*.* which are served from resnet-secure.
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
            } else if (address.startsWith("172")) {
                String[] bits = address.split("\\.");
                int secondOctet = Integer.parseInt(bits[1]);
                if (secondOctet >= private20bitLower && secondOctet <= private20bitUpper) {
                    if ((secondOctet < hotspot20bitLower || secondOctet > hotspot20bitUpper) && (secondOctet < resnet20bitLower || secondOctet > resnet20bitUpper)) {
                        local = true;
                    }
                }
            }            
        }
        
        return !local;
    }

    public boolean hasNonLocalAddress() {
        return gotNonLocalAddress;
    }

    public String getNonLocalAddress() {
        return ip;
    }
}
