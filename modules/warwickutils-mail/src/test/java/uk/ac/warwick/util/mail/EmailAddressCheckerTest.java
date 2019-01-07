package uk.ac.warwick.util.mail;

import org.junit.Test;

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

}