package uk.ac.warwick.util.web.filter;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.Assert.*;

public class DoubleFreezeBugFilterTest {
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;
    private DoubleFreezeBugFilter filter;
    
    @Before public void before() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
        filter = new DoubleFreezeBugFilter();
    }
    
    @Test public void badHeaders() throws Exception {
        request.addHeader("Accept-Language", "en-us;q=22.250738585072012e-309");
        filter.doFilter(request, response, chain);
        assertIsRejected();
    }
    
    @Test public void goodHeaders() throws Exception {
        request.addHeader("Accept-Language", "en-us;q=0.3");
        filter.doFilter(request, response, chain);
        assertIsAccepted();
    }
    
    @Test public void goodParameters() throws Exception {
        request.setParameter("quotient", "22.2507385");
        filter.doFilter(request, response, chain);
        assertIsAccepted();
    }

    /**
     * Quoted here is the full decimal value without 
     */
    @Test public void badParameters() throws Exception {
        request.setParameter("quotient", "0.000000000000000000000000000000000000000000000000000000000000000000000000000000"
            +"00000000000000000000000000000000000000000000000000000000000000000000000000000000"
            +"00000000000000000000000000000000000000000000000000000000000000000000000000000000"
            +"00000000000000000000000000000000000000000000000000000000000000000000022250738585"
            +"07201136057409796709131975934819546351645648023426109724822222021076945516529523"
            +"90813508791414915891303962110687008643869459464552765720740782062174337998814106"
            +"32673292535522868813721490129811224514518898490572223072852551331557550159143974"
            +"76397983411801999323962548289017107081850690630666655994938275772572015763062690"
            +"66333264756530000924588831643303777979186961204949739037782970490505108060994073"
            +"02629371289589500035837999672072543043602840788957717961509455167482434710307026"
            +"09144621572289880258182545180325707018860872113128079512233426288368622321503775"
            +"66662250398253433597456888442390026549819838548794829220689472168983109969836584"
            +"68140228542433306603398508864458040010349339704275671864433837704860378616227717"
            +"38545623065874679014086723327636718751"
            );
        filter.doFilter(request, response, chain);
        assertIsRejected();
    }
    
    @Test public void nullValues() throws Exception {
        request.setParameter("quotient", (String)null);
        request.addHeader("Accept-Language", "");
        filter.doFilter(request, response, chain);
        assertIsAccepted();
    }
    
    private void assertIsAccepted() {
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    private void assertIsRejected() {
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
}
