package uk.ac.warwick.util.web.filter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RequestIPAddressHolderImpl implements RequestIPAddressHolder {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(RequestIPAddressHolderImpl.class);

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


        boolean local = false;
        if (localAddresses.contains(address)) {
            local = true;
        } else {

            try {
                final InetAddress inetAddress = InetAddress.getByName(address);

                final InetAddressRange private172 = InetAddressRange.of("172.16.0.0", "172.31.255.255");
                final InetAddressRange hotspots = InetAddressRange.of("172.31.0.0", "172.31.255.255");
                final InetAddressRange resnet = InetAddressRange.of("172.26.0.0", "172.29.255.255");

                /*
                 * look for private network addresses: 127.0.0.1 192.168.*.*
                 * 172.[16-31].*.*
                 *
                 * We allow certain ranges within these private ranges that are used by things
                 * like resnet and wireless hotspots.
                 *
                 * 10.*.*.* is a private network but it seems that addresses get allocated across
                 * this range for things like guest wifi, conferences, and digital displays (SBTWO-6812)
                 * so may as well treat them as external IPs.
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
                if ("127.0.0.1".equals(address) || address.startsWith("192.168") || address.startsWith("169.254")) {
                    local = true;
                } else if (private172.contains(inetAddress) && !hotspots.contains(inetAddress) && !resnet.contains(inetAddress)) {
                    local = true;
                }

            } catch (UnknownHostException e) {
                // We should never get this exception as we only pass IP addresses, never hostnames.
                LOGGER.error("Failed to parse IP addresses! (this is a bug)", e);
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
