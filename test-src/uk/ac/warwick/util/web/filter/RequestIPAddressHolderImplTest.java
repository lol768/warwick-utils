package uk.ac.warwick.util.web.filter;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.ac.warwick.util.core.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

public class RequestIPAddressHolderImplTest {

    // Used as list of known Sitebuilder zone IPs, I think. Explicitly treated as local.
    private Collection<String> localAddresses = Arrays.asList("137.205.194.5", "137.205.194.6");

    @Test
    public void testRegularAddress() {
        assertEquals("137.205.194.1", createReqHolder("137.205.194.1").getNonLocalAddress());
        assertTrue(createReqHolder("2.14.93.254").hasNonLocalAddress());
    }

    @Test
    public void testExplicitLocalAddress() {
        RequestIPAddressHolder ip = createReqHolder("137.205.194.5");
        assertEquals("127.0.0.1", ip.getNonLocalAddress());
        assertFalse(ip.hasNonLocalAddress());
    }

    @Test
    public void test172PrivateSubnet() {
        assertTrue("172.1.* is nonlocal", createReqHolder("172.1.2.3").hasNonLocalAddress());
        assertFalse("172.16.* is local", createReqHolder("172.16.2.3").hasNonLocalAddress());
        assertTrue("resnet is nonlocal", createReqHolder("172.27.1.1").hasNonLocalAddress());
        assertTrue("hotspot is nonlocal", createReqHolder("172.31.255.255").hasNonLocalAddress());
    }

    @Test
    public void test10PrivateSubnet() {
        assertTrue("10.* is nonlocal", createReqHolder("10.1.2.3").hasNonLocalAddress());
    }

    @Test
    public void multipleXffValues() {
        final RequestIPAddressHolder ip = createReqHolder("137.205.194.5", "172.16.10.10", "137.205.194.200", "172.17.10.10");
        assertEquals("should use first nonlocal address found", "137.205.194.200", ip.getNonLocalAddress());
    }


    private RequestIPAddressHolder createReqHolder(String... xffAddresses) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("127.0.0.1");
        req.addHeader("X-Forwarded-For", StringUtils.join(xffAddresses, ","));
        RequestIPAddressHolder ip = new RequestIPAddressHolderImpl(req, localAddresses);
        return ip;
    }

}