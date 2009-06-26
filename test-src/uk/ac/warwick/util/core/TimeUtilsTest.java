package uk.ac.warwick.util.core;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.joda.time.DateTime;

import sun.util.calendar.CalendarUtils;
import uk.ac.warwick.util.core.jodatime.DateTimeUtils;

import junit.framework.TestCase;

public class TimeUtilsTest extends TestCase {
    public void testEqualsIgnoreTime() {
        Calendar twelfthMorning = makeCalendar(12, 10);
        Calendar twelfthEvening = makeCalendar(12, 16);
        Calendar eleventhMorning = makeCalendar(11, 10);
        
        assertTrue(TimeUtils.equalsIgnoreTime(twelfthMorning, twelfthEvening));
        assertTrue(TimeUtils.equalsIgnoreTime(twelfthEvening, twelfthMorning));
        assertFalse(TimeUtils.equalsIgnoreTime(twelfthMorning, eleventhMorning));
        assertFalse(TimeUtils.equalsIgnoreTime(twelfthEvening, eleventhMorning));
    }
    
    public void testGetDifferenceInDays() {
    	Calendar a = Calendar.getInstance();
    	a.set(2009, Calendar.JUNE, 25, 10, 0, 0);
    	a.set(Calendar.MILLISECOND, 0);
    	
    	Calendar b = Calendar.getInstance();
    	b.set(2009, Calendar.JUNE, 26, 11, 0, 0);
    	b.set(Calendar.MILLISECOND, 0);
    	
    	assertEquals(1, TimeUtils.getDifferenceInDays(a, b));
    	
    	// Copied and modified from DateUtils to check for compatibility
    	
    	DateTime dt = new DateTime().withDate(2009, 1, 1).withTime(1, 0, 0, 0); //1am, 1st Jan 09
    	check(0, dt, dt.plusHours(1));
    	check(1, dt, dt.plusDays(1));
    	check(1, dt, dt.plusDays(1).plusHours(1));
    	check(30, dt, dt.plusDays(30));
    	check(30, dt, dt.plusDays(30).plusHours(1));
    }
    
    public void testGetDifferenceInWeeks() {
    	DateTime t = new DateTime(2009,6,25, 10,0,0,0);
    	
    	assertEquals(1, TimeUtils.getDifferenceInWeeks(t.toCalendar(null), t.plusDays(1).toCalendar(null)));
    	assertEquals(1, TimeUtils.getDifferenceInWeeks(t.toCalendar(null), t.plusDays(7).toCalendar(null)));
    	assertEquals(2, TimeUtils.getDifferenceInWeeks(t.toCalendar(null), t.plusDays(8).toCalendar(null)));
    }
    
    private void check(int difference, DateTime a, DateTime b) {
    	assertEquals(difference, TimeUtils.getDifferenceInDays(a.toCalendar(null), b.toCalendar(null)));
    }
    
    /**
     * Check that when adding DATE, it does roll over to the next month
     * instad of just skipping back to the start of the next month.
     */
    public void testCalendarAddDay() {
        Calendar endOfNovember = makeCalendar(30, 10);
        assertEquals(10, endOfNovember.get(Calendar.MONTH));
        endOfNovember.add(Calendar.DATE, 1);
        assertEquals(1, endOfNovember.get(Calendar.DATE));
        assertEquals(11, endOfNovember.get(Calendar.MONTH));
    }
    
    private Calendar makeCalendar(int date, int hour) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 2006);
        c.set(Calendar.MONTH, 10);
        c.set(Calendar.DAY_OF_MONTH, date);
        c.set(Calendar.HOUR_OF_DAY, hour);
        return c;
    }
}
