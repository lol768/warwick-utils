package uk.ac.warwick.util.core.jodatime;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Test;

import uk.ac.warwick.util.core.jodatime.DateTimeUtils.Callback;

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
    	
    	assertEquals(0, DateTimeUtils.getDifferenceInDays(dt, dt.plusHours(1)));
    	assertEquals(1, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(1)));
    	assertEquals(1, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(1).plusHours(1)));
    	assertEquals(30, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(30)));
    	assertEquals(30, DateTimeUtils.getDifferenceInDays(dt, dt.plusDays(30).plusHours(1)));
    }
    
    @Test
    public void getDifferenceInWeeks() {
    	// This should round UP
    	DateTime dt = new DateTime().withDate(2009, 1, 1).withTime(1, 0, 0, 0); //1am, 1st Jan 09
    	
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
    
    @Test
    @SuppressWarnings("deprecation")
    public void mockDateTime() {
    	final DateTime d = new DateTime(2009,8,17, 12,40,20,0);
    	DateTimeUtils.useMockDateTime(new DateTime(2009,8,17, 12,40,20,0), new Callback() {
			public void doSomething() {
				assertEquals(d, DateTimeUtils.newDateTime());
				// All JodaTime objects will obey this pretend time. It is magic
				assertEquals(d, new DateTime());
			}
		});
    }
    
    private DateTime makeDateTime(int date, int hour) {
    	return new DateTime().withDate(2006, 11, date).withHourOfDay(hour);
    }
    
    

}
