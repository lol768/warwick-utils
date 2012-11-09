package uk.ac.warwick.util.web.bind;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Test;

public final class DateTimePropertyEditorTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void cantInstantiateWithoutFormats() throws Exception {
        new DateTimePropertyEditor(new String[0], true);
        
        fail("expected exception");
    }
    
    @Test
    public void fallsBackForYear() throws Exception {        
        DateTimePropertyEditor editor = new DateTimePropertyEditor(new String[] { "dd/MM/yy", "dd/MM/yyyy" }, true);
        editor.setAsText("12/01/2009");
        
        DateTime output = (DateTime)editor.getValue();
        assertNotNull(output);
        assertEquals(0, output.getMillisOfDay());
        assertEquals(12, output.getDayOfMonth());
        assertEquals(DateTimeConstants.JANUARY, output.getMonthOfYear());
        assertEquals(2009, output.getYear());
        assertEquals("12/01/09", editor.getAsText());
        
        editor.setAsText("12/01/09");
        
        output = (DateTime)editor.getValue();
        assertNotNull(output);
        assertEquals(0, output.getMillisOfDay());
        assertEquals(12, output.getDayOfMonth());
        assertEquals(DateTimeConstants.JANUARY, output.getMonthOfYear());
        assertEquals(2009, output.getYear());
    }
    
    @Test
    public void lenientParsing() throws Exception {
    	DateTimePropertyEditor editor = new DateTimePropertyEditor("ddMMyyHHmm", false, true);
    	editor.setAsText("3109090900");
    	
    	DateTime output = (DateTime)editor.getValue();
        assertNotNull(output);
        assertEquals(0, output.getMillisOfSecond());
        assertEquals(9, output.getHourOfDay());
        assertEquals(0, output.getMinuteOfHour());
        assertEquals(1, output.getDayOfMonth());
        assertEquals(DateTimeConstants.OCTOBER, output.getMonthOfYear());
        assertEquals(2009, output.getYear());
        assertEquals("0110090900", editor.getAsText());
    }

}
