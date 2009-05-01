package uk.ac.warwick.util.core.jodatime;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.ReadableInstant;
import org.joda.time.Weeks;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.LenientChronology;

/**
 * Utility functions to work with JodaTime {@link DateTime} objects.
 * 
 * @author Mat
 */
public final class DateTimeUtils {
	
	private static DateTime mockDateTime;
	
    private DateTimeUtils() {}
    
    /**
     * Leniently return a DateTime with the year, month and day, set to midnight.
     * <p>
     * e.g. newLenientDateTime(2009, 2, 30) will return a DateTime for
     * 00:00:00.0 02/03/2009
     */
    public static DateTime newLenientDateTime(int year, int month, int day) {
        return new DateTime()
               .withChronology(LenientChronology.getInstance(ISOChronology.getInstance())) // lenient
               .withDate(year, month, day)
               .withMillisOfDay(0) // midnight
               .withChronology(ISOChronology.getInstance());
    }
    
    /**
     * Leniently return a DateTime with the date and time, milliseconds set to 0.
     * <p>
     * e.g. newLenientDateTime(2009, 2, 30, 17, 90, 00) will return a DateTime for
     * 18:30:00.0 02/03/2009
     */
    public static DateTime newLenientDateTime(int year, int month, int day, int hour, int minute, int seconds) {
        return new DateTime()
               .withChronology(LenientChronology.getInstance(ISOChronology.getInstance())) // lenient
               .withDate(year, month, day)
               .withTime(hour, minute, seconds, 0)
               .withChronology(ISOChronology.getInstance());
    }
    
    /**
     * Returns whether two DateTime objects represent the same day
     * (ignoring their time components).
     * 
     * Reflexive.
     */
    public static boolean equalsIgnoreTime(final ReadableInstant a, final ReadableInstant b) {
    	return new DateMidnight(a).isEqual(new DateMidnight(b));
    }
    
    /** Synonym for equalsIgnoreTime **/
    public static boolean isSameDay(final ReadableInstant a, final ReadableInstant b) {
        return equalsIgnoreTime(a, b);
    }
    
    /**
     * Gets the difference in days between two Calendar objects, rounded UP (diff between 23:59 and 00:01 on consecutive days is 1)
     */
    public static int getDifferenceInDays(final ReadableInstant a, final ReadableInstant b) {
    	Days d = Days.daysBetween(a, b);
    	int days = d.getDays();
    	
    	Duration duration = new Duration(a, b);
    	    	
    	// since we're rounding up, we need to check whether the period has any left overs
    	if (duration.minus(d.toStandardDuration()).getMillis() > 0) {
    		days++;
    	}
    	
    	return days;
    }
    
    /**
     * Gets the difference in weeks between two Calendar objects, rounded UP
     */
    public static int getDifferenceInWeeks(final ReadableInstant a, final ReadableInstant b) {
    	Weeks w = Weeks.weeksBetween(a, b);
    	int weeks = w.getWeeks();
    	
    	Duration duration = new Duration(a, b);
    	    	
    	// since we're rounding up, we need to check whether the period has any left overs
    	if (duration.minus(w.toStandardDuration()).getMillis() > 0) {
    		weeks++;
    	}
    	
    	return weeks;
    }
    
    public static boolean isBetween(final DateTime cal, final DateTime earliest, final DateTime latest) {
        return (cal.isEqual(earliest) || cal.isEqual(latest) || 
                (cal.isAfter(earliest) && cal.isBefore(latest)));
    }
    
    public static DateTime newDateTime() {
        if (mockDateTime == null) {
            return new DateTime(); 
        }
        return mockDateTime;
    }
    
    /**
     * NOT threadsafe. Used for testing.
     * Does an action with a mockdatetime. only makes a difference if the
     * code inside uses DateTimeUtils.newDateTime()
     */
    public static void useMockDateTime(final DateTime dt, final Callback callback) {
        try {
            mockDateTime = dt;
            callback.doSomething();
        } finally {
            mockDateTime = null;
        }
    }
    
    public static interface Callback {
       void doSomething();
    }

}