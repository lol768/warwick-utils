package uk.ac.warwick.util.core.jodatime;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Test;

public final class DateTimeUtilsTest {
    
    @Test
    public void newLenientDateTimeDateOnly() throws Exception {       
        DateTime date = DateTimeUtils.newLenientDateTime(2009, 2, 30);
        assertNotNull(date);
        
        // we should have rolled to 2nd March 2009
        assertEquals(2, date.getDayOfMonth());
        assertEquals(3, date.getMonthOfYear());
        assertEquals(2009, date.getYear());
        assertEquals(0, date.getHourOfDay());
        assertEquals(0, date.getMinuteOfHour());
        assertEquals(0, date.getSecondOfMinute());
        assertEquals(0, date.getMillisOfSecond());
    }
    
    @Test
    public void newLenientDateTime() throws Exception {       
        // 17:90 (18:30) on 30th Feb (2nd Mar)
        DateTime date = DateTimeUtils.newLenientDateTime(2009, 2, 30, 17, 90, 0);
        assertNotNull(date);
        
        // we should have rolled to 2nd March 2009, 6.30pm
        assertEquals(2, date.getDayOfMonth());
        assertEquals(3, date.getMonthOfYear());
        assertEquals(2009, date.getYear());
        assertEquals(18, date.getHourOfDay());
        assertEquals(30, date.getMinuteOfHour());
        assertEquals(0, date.getSecondOfMinute());
        assertEquals(0, date.getMillisOfSecond());
    }
    
    @Test
    public void equalsIgnoreTime() {
    	DateTime twelfthMorning = makeDateTime(12, 10);
    	DateTime twelfthEvening = makeDateTime(12, 16);
    	DateTime eleventhMorning = makeDateTime(11, 10);
        
        assertTrue(DateTimeUtils.equalsIgnoreTime(twelfthMorning, twelfthEvening));
        assertTrue(DateTimeUtils.equalsIgnoreTime(twelfthEvening, twelfthMorning));
        assertFalse(DateTimeUtils.equalsIgnoreTime(twelfthMorning, eleventhMorning));
        assertFalse(DateTimeUtils.equalsIgnoreTime(twelfthEvening, eleventhMorning));
    }
    
    @Test
    public void isSameDay() {
    	DateTime dt = new DateTime().withDate(2009, 1, 1);
    	
    	assertTrue(DateTimeUtils.isSameDay(dt, dt.withTime(15, 1, 1, 0)));
    	assertFalse(DateTimeUtils.isSameDay(dt, dt.plusDays(1)));
    	assertTrue(DateTimeUtils.isSameDay(dt, dt.plusDays(0)));
    }
    
    @Test
    public void getDifferenceInDays() {
    	// This should round UP
    	DateTime dt = new DateTime().withDate(2009, 1, 1).withTime(1, 0, 0, 0); //1am, 1st Jan 09
    	
    	assertEquals(1, DateTimeUtils.getDifferenceInDays(dt, dt.plusHours(1)));
    	assertEquals(1, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(1)));
    	assertEquals(2, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(1).plusHours(1)));
    	assertEquals(30, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(30)));
    	assertEquals(31, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(30).plusHours(1)));
    }
    
    @Test
    public void getDifferenceInWeeks() {
    	// This should round UP
    	DateTime dt = new DateTime().withDate(2009, 1, 1).withTime(1, 0, 0, 0); //1am, 1st Jan 09
    	
    	assertEquals(1, DateTimeUtils.getDifferenceInWeeks(dt, dt.plusHours(1)));
    	assertEquals(1, DateTimeUtils.getDifferenceInWeeks(dt, dt.plusWeeks(1)));
    	assertEquals(2, DateTimeUtils.getDifferenceInWeeks(dt, dt.plusWeeks(1).plusHours(1)));
    	assertEquals(5, DateTimeUtils.getDifferenceInWeeks(dt, dt.plusWeeks(5)));
    	assertEquals(6, DateTimeUtils.getDifferenceInWeeks(dt, dt.plusWeeks(5).plusHours(1)));
    }
    
    private DateTime makeDateTime(int date, int hour) {
    	return new DateTime().withDate(2006, 11, date).withHourOfDay(hour);
    }

}
