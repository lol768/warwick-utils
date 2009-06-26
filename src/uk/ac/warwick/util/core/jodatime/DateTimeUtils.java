package uk.ac.warwick.util.core.jodatime;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.ReadableDateTime;
import org.joda.time.Weeks;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.LenientChronology;

/**
 * Utility functions to work with JodaTime {@link DateTime} objects.
 * 
 * @author Mat
 */
public final class DateTimeUtils {
	
	private static final int DAYS_PER_WEEK = 7;
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
    public static boolean equalsIgnoreTime(final ReadableDateTime a, final ReadableDateTime b) {
    	return a.getYear() == b.getYear() && a.getDayOfYear() == b.getDayOfYear();
    }
    
    /** Synonym for equalsIgnoreTime **/
    public static boolean isSameDay(final ReadableDateTime a, final ReadableDateTime b) {
        return equalsIgnoreTime(a, b);
    }
    
    /**
     * Gets the difference in days between two Calendar objects, ignoring time (diff between 23:59 and 00:01 on consecutive days is 1)
     */
    public static int getDifferenceInDays(final ReadableDateTime a, final ReadableDateTime b) {
    	DateMidnight start = new DateMidnight(a);
    	DateMidnight end = new DateMidnight(b);
    	return Days.daysBetween(start, end).getDays();
    }
    
    /**
     * Gets the difference in weeks between two Calendar objects, rounded UP
     */
    public static int getDifferenceInWeeks(final ReadableDateTime a, final ReadableDateTime b) {
    	int days = Days.daysBetween(new DateMidnight(a), new DateMidnight(b)).getDays();
    	int weeks = days / DAYS_PER_WEEK;
    	if (days % 7 != 0) {
    		weeks++;
    	}
    	return weeks;
    }
    
    public static boolean isBetween(final ReadableDateTime dateTime, final ReadableDateTime earliest, final ReadableDateTime latest) {
        return (dateTime.isEqual(earliest) || dateTime.isEqual(latest) || 
                (dateTime.isAfter(earliest) && dateTime.isBefore(latest)));
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