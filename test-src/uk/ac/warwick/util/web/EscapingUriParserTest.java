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
    
    @Test
    public void escapeFragment() throws Exception {
        String input    = "http://www.google.co.uk/url?url=http://www2.warwick.ac.uk/services/library/mrc/subject_guides/trotskyite_sources/#sll&rct=j&q=socialist labour league&usg=afqjcngh3kmx21ykbmqtxrwkpgy48nofpg&sa=x&ei=cmvate-mooawhqf89pdica&ved=0ceiqygq";
        String expected = "http://www.google.co.uk/url?url=http://www2.warwick.ac.uk/services/library/mrc/subject_guides/trotskyite_sources/#sll&rct=j&q=socialist%20labour%20league&usg=afqjcngh3kmx21ykbmqtxrwkpgy48nofpg&sa=x&ei=cmvate-mooawhqf89pdica&ved=0ceiqygq";
        
        assertEquals(expected, new EscapingUriParser().parse(input).toString());
    }
    
    @Test
    public void emptyFragment() throws Exception {
        assertEquals("http://www.warwick.ac.uk/?q=yes#", new EscapingUriParser().parse("http://www.warwick.ac.uk/?q=yes#").toString());
    }

}
