package uk.ac.warwick.util.web.filter;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.net.InetAddress;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;

public class InetAddressRangeTest {

    private void check(InetAddressRange range, List<String> contained, List<String> uncontained) throws Exception {
        for (String ip : contained) {
            assertTrue(range + " should contain " + ip, range.contains(InetAddress.getByName(ip)));
        }
        for (String ip : uncontained) {
            assertFalse(range + " should not contain " + ip, range.contains(InetAddress.getByName(ip)));
        }
    }

    @Test
    public void test10Subnet() throws Exception {
        check(InetAddressRange.of("10.0.0.0", "10.0.0.10"),
            newArrayList("10.0.0.0", "10.0.0.10"),
            newArrayList("10.1.0.5")
        );
    }

    @Test
    public void test172Subnet() throws Exception {
        check(InetAddressRange.of("172.16.0.0", "172.31.255.255"),
                newArrayList("172.16.0.0", "172.16.1.2", "172.16.255.255", "172.29.1.1", "172.31.50.50"),
                newArrayList("172.15.0.0", "171.16.0.0")
        );
    }
}