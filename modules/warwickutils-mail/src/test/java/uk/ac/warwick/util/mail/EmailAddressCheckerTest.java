package uk.ac.warwick.util.mail;

import org.junit.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.SimpleResolver;

import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class EmailAddressCheckerTest {

    @Test
    public void itWorks() {
        EmailAddressChecker checker = new EmailAddressChecker("mat.mannion@gmail.com");
        assertFalse(checker.isEmpty());
        assertTrue(checker.isMatches());
        assertTrue(checker.isValid());

        checker = new EmailAddressChecker("m.mannion@warwick.ac.uk");
        assertFalse(checker.isEmpty());
        assertTrue(checker.isMatches());
        assertTrue(checker.isValid());

        checker = new EmailAddressChecker("invalid@inv.alid");
        assertFalse(checker.isEmpty());
        assertFalse(checker.isMatches());
        assertFalse(checker.isValid());
    }

    @Test
    public void testDnsServerFailure() throws UnknownHostException {
        SimpleResolver resolver = new SimpleResolver("203.0.113.042");
        resolver.setTimeout(3);
        Lookup.setDefaultResolver(resolver);
        EmailAddressChecker checker = new EmailAddressChecker("m.mannion@warwick.ac.uk");
        assertFalse(checker.isEmpty());
        assertFalse(checker.isMatches());
        assertTrue(checker.isServerError());
        assertTrue(checker.isValid());
        Lookup.refreshDefault();
    }

}
