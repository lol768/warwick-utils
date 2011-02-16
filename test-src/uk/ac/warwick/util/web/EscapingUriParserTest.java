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
    
    @Test
    public void sbtwo4221() throws Exception {
        assertEquals("http://www2.warwick.ac.uk/fac/sci/eng/euo/modules/year3/es3b6/resources/paymentreceived?orderKey=UNIWARWICK%5EWARWICKWEB%5E094db28a2dc72dab012e01479ab07f58&paymentStatus=AUTHORISED&paymentAmount=10000&paymentCurrency=GBP&mac=29feed0776b3f295cc3172fdd857e949&jlbz=yUrpjPwfrpC2Mo1bATVuYdDNaFYgOPudeSpjsFSdXXc", new EscapingUriParser().parse("http://www2.warwick.ac.uk/fac/sci/eng/euo/modules/year3/es3b6/resources/paymentreceived?orderKey=UNIWARWICK^WARWICKWEB^094db28a2dc72dab012e01479ab07f58&paymentStatus=AUTHORISED&paymentAmount=10000&paymentCurrency=GBP&mac=29feed0776b3f295cc3172fdd857e949&jlbz=yUrpjPwfrpC2Mo1bATVuYdDNaFYgOPudeSpjsFSdXXc").toString());
    }
    
    @Test
    public void sbtwo4231() throws Exception {
        assertEquals("http://www.google.com/url?sa=t&source=web&cd=17&ved=0CDQQFjAGOAo&url=http%3A%2F%2Fwww2.warwick.ac.uk%2Falumni%2Fknowledge%2Fblogs%2F&rct=j&q=us%20army%20contructing%20office%20email%20addres%20in%20pon%20%23%20in%20helmand%20afghanistan&ei=3VFSTbWZEsP38AbE8MHTCQ&usg=AFQjCNEXLLP1vWYq-2pT67G8T6U7RHynBw", new EscapingUriParser().parse("http://www.google.com/url?sa=t&source=web&cd=17&ved=0CDQQFjAGOAo&url=http%3A%2F%2Fwww2.warwick.ac.uk%2Falumni%2Fknowledge%2Fblogs%2F&rct=j&q=us%20army%20contructing%20office%20email%20addres%20in%20pon%20%23%20in%20helmand%20afghanistan&ei=3VFSTbWZEsP38AbE8MHTCQ&usg=AFQjCNEXLLP1vWYq-2pT67G8T6U7RHynBw").toString());
    }

}
