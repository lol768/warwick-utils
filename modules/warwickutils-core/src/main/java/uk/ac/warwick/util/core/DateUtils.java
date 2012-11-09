package uk.ac.warwick.util.core;

import java.util.Calendar;
import java.util.Date;

/**
 * Singleton utility class.
 * 
 * It is recommended that you use JodaTime instead, as it has most of these
 * conveniences built in and is generally better than Date or Calendar.
 * 
 * @author cusebr
 */
public final class DateUtils {
    
    private static Calendar mockCalendar;
    
    private DateUtils() {
    }
    
    public static boolean isBetween(final Calendar cal, final Calendar earliest, final Calendar latest) {
        return (cal.equals(earliest) || cal.equals(latest) || 
                (cal.after(earliest) && cal.before(latest)));
    }

    /**
     * Return a date which represents the specified date rounded down to the earlist
     * millisecond.
     */
    public static Date roundDownToMS(final Date date) {
        if (date == null) return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    public static Calendar newCalendar() {
        if (mockCalendar == null) {
            return Calendar.getInstance(); 
        }
        return (Calendar)mockCalendar.clone();
    }
    
    /**
     * NOT threadsafe. Used for testing.
     * Does an action with a mockcalendar. only makes a difference if the
     * code inside uses DateUtils.newCalendar()
     */
    public static void useMockCalendar(final Calendar c, final Callback callback) {
        try {
            mockCalendar = c;
            callback.doSomething();
        } finally {
            mockCalendar = null;
        }
    }
    
    public static interface Callback {
       void doSomething();
    }
}

