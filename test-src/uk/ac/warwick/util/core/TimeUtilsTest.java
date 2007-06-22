package uk.ac.warwick.util.core;

import java.util.Calendar;

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
