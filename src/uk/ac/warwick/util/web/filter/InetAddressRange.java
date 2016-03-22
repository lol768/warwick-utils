package uk.ac.warwick.util.web.filter;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;

/**
 * Represents a range of IP addresses, based on a from and to address (inclusive).
 * So the range "10.*.*.*" or "10.0.0.0/8" would be represented as 10.0.0.0-10.255.255.255
 */
public final class InetAddressRange {
    private static final InetAddressComparator COMPARATOR = new InetAddressComparator();

    private final InetAddress from;
    private final InetAddress to;

    public static InetAddressRange of(String fromIp, String toIp) throws UnknownHostException {
        return new InetAddressRange(InetAddress.getByName(fromIp), InetAddress.getByName(toIp));
    }

    public InetAddressRange(InetAddress from, InetAddress to) {
        this.from = from;
        this.to = to;
        if (COMPARATOR.compare(from, to) > 0)  {
            throw new IllegalArgumentException("From address should be below the to address");
        }
    }

    public boolean contains(InetAddress addr) {
        return COMPARATOR.compare(from, addr) <= 0 &&
               COMPARATOR.compare(addr, to) <= 0;
    }

    static class InetAddressComparator implements Comparator<InetAddress> {
        @Override
        public int compare(InetAddress adr1, InetAddress adr2) {
            byte[] ba1 = adr1.getAddress();
            byte[] ba2 = adr2.getAddress();

            // general ordering: ipv4 before ipv6
            if(ba1.length < ba2.length) return -1;
            if(ba1.length > ba2.length) return 1;

            // we have 2 ips of the same type, so we have to compare each byte
            for(int i = 0; i < ba1.length; i++) {
                int b1 = unsignedByteToInt(ba1[i]);
                int b2 = unsignedByteToInt(ba2[i]);
                if(b1 == b2)
                    continue;
                if(b1 < b2)
                    return -1;
                else
                    return 1;
            }
            return 0;
        }

        private int unsignedByteToInt(byte b) {
            return (int) b & 0xFF;
        }
    }

    public String toString() {
        return from.getHostAddress() + "-" + to.getHostAddress();
    }
}
