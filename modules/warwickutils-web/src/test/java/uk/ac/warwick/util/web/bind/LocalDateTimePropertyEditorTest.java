package uk.ac.warwick.util.web.bind;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoField;

import static org.junit.Assert.*;

public final class LocalDateTimePropertyEditorTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void cantInstantiateWithoutFormats() throws Exception {
        new LocalDateTimePropertyEditor(new String[0], true);
        
        fail("expected exception");
    }
    
    @Test
    public void fallsBackForYear() throws Exception {        
        LocalDateTimePropertyEditor editor = new LocalDateTimePropertyEditor(new String[] { "HH:mm dd/MM/yy", "HH:mm dd/MM/yyyy" }, true);
        editor.setAsText("00:00 12/01/2009");
        
        LocalDateTime output = (LocalDateTime)editor.getValue();
        assertNotNull(output);
        assertEquals(0, output.getLong(ChronoField.MILLI_OF_DAY));
        assertEquals(12, output.getDayOfMonth());
        assertEquals(Month.JANUARY, output.getMonth());
        assertEquals(2009, output.getYear());
        assertEquals("00:00 12/01/09", editor.getAsText());
        
        editor.setAsText("00:00 12/01/09");
        
        output = (LocalDateTime)editor.getValue();
        assertNotNull(output);
        assertEquals(0, output.getLong(ChronoField.MILLI_OF_DAY));
        assertEquals(12, output.getDayOfMonth());
        assertEquals(Month.JANUARY, output.getMonth());
        assertEquals(2009, output.getYear());
    }
    
    @Test
    public void lenientParsing() throws Exception {
    	LocalDateTimePropertyEditor editor = new LocalDateTimePropertyEditor("ddMMyyHHmm", false, true);
    	editor.setAsText("3109090900");

        LocalDateTime output = (LocalDateTime)editor.getValue();
        assertNotNull(output);
        assertEquals(0, output.getLong(ChronoField.MILLI_OF_SECOND));
        assertEquals(9, output.getHour());
        assertEquals(0, output.getMinute());
        assertEquals(1, output.getDayOfMonth());
        assertEquals(Month.OCTOBER, output.getMonth());
        assertEquals(2009, output.getYear());
        assertEquals("0110090900", editor.getAsText());
    }

}
