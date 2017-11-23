package uk.ac.warwick.util.core.jodatime;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.*;

public final class DateTimeUtilsTest {
    
    @Test
    public void equalsIgnoreTime() {
    	LocalDateTime twelfthMorning = makeDateTime(12, 10);
		LocalDateTime twelfthEvening = makeDateTime(12, 16);
		LocalDateTime eleventhMorning = makeDateTime(11, 10);
        
        assertTrue(DateTimeUtils.equalsIgnoreTime(twelfthMorning, twelfthEvening));
        assertTrue(DateTimeUtils.equalsIgnoreTime(twelfthEvening, twelfthMorning));
        assertFalse(DateTimeUtils.equalsIgnoreTime(twelfthMorning, eleventhMorning));
        assertFalse(DateTimeUtils.equalsIgnoreTime(twelfthEvening, eleventhMorning));
    }
    
    @Test
    public void isSameDay() {
    	LocalDate dt = LocalDate.of(2009, 1, 1);
    	
    	assertTrue(DateTimeUtils.isSameDay(dt, dt.atTime(15, 1, 1, 0)));
    	assertFalse(DateTimeUtils.isSameDay(dt, dt.plusDays(1)));
    	assertTrue(DateTimeUtils.isSameDay(dt, dt.plusDays(0)));
    }
    
    @Test
    public void getDifferenceInDays() {
    	// This should round UP
		LocalDateTime dt = LocalDateTime.of(2009, 1, 1, 1, 0, 0, 0); //1am, 1st Jan 09
    	
    	assertEquals(0, DateTimeUtils.getDifferenceInDays(dt, dt.plusHours(1)));
    	assertEquals(1, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(1)));
    	assertEquals(1, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(1).plusHours(1)));
    	assertEquals(30, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(30)));
    	assertEquals(30, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(30).plusHours(1)));
    }
    
    @Test
    public void getDifferenceInWeeks() {
    	// This should round UP
    	LocalDateTime dt = LocalDateTime.of(2009, 1, 1, 1, 0, 0, 0); //1am, 1st Jan 09
    	
    	/* these are slightly different to TimeUtils' equivalent, but only where hours are involved
    	 * (time of day would be ignored as with getDifferenceInDays())
    	 */
    	
    	assertEquals(0, DateTimeUtils.getDifferenceInWeeks(dt, dt.plusHours(1)));
    	assertEquals(1, DateTimeUtils.getDifferenceInWeeks(dt, dt.plusWeeks(1)));
    	assertEquals(1, DateTimeUtils.getDifferenceInWeeks(dt, dt.plusWeeks(1).plusHours(1)));
    	assertEquals(2, DateTimeUtils.getDifferenceInWeeks(dt, dt.plusDays(8)));
    	assertEquals(5, DateTimeUtils.getDifferenceInWeeks(dt, dt.plusWeeks(5)));
    	assertEquals(5, DateTimeUtils.getDifferenceInWeeks(dt, dt.plusWeeks(5).plusHours(1)));
    }

    private LocalDateTime makeDateTime(int date, int hour) {
    	return LocalDate.of(2006, 11, date).atTime(LocalTime.now().withHour(hour));
    }

}
