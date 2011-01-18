package uk.ac.warwick.util.web;

import static org.junit.Assert.*;

import org.junit.Test;

public final class EscapingUriParserTest {
    
    @Test
    public void invalidEscapeSequence() throws Exception {
        assertEquals("http://www.google.com/?cheese=a%25", new EscapingUriParser().parse("http://www.google.com/?cheese=a%").toString());
        assertEquals("http://www.google.com/?cheese=a%25%7E%25&steve=so%7Emething", new EscapingUriParser().parse("http://www.google.com/?cheese=a%%7E%&steve=so%7Emething").toString());
    }
    
    @Test
    public void validEscapeSequence() throws Exception {
        assertEquals("http://www.google.com/?cheese=a%7E", new EscapingUriParser().parse("http://www.google.com/?cheese=a%7E").toString());
    }

}
