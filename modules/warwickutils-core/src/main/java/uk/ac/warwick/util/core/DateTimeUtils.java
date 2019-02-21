package uk.ac.warwick.util.core;

import org.threeten.extra.Days;

import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.temporal.*;

/**
 * Utility functions to work with JSR310 JavaTime objects.
 * 
 * @author Mat
 */
public final class DateTimeUtils {

    public static Clock CLOCK_IMPLEMENTATION = Clock.systemDefaultZone();
	
	private static final int DAYS_PER_WEEK = 7;
	
    private DateTimeUtils() {}

    /**
     * Returns whether two DateTime objects represent the same day
     * (ignoring their time components).
     * 
     * Reflexive.
     */
    public static boolean equalsIgnoreTime(final Temporal a, final Temporal b) {
        return a.getLong(ChronoField.YEAR) == b.getLong(ChronoField.YEAR) && a.getLong(ChronoField.DAY_OF_YEAR) == b.getLong(ChronoField.DAY_OF_YEAR);
    }
    
    /** Synonym for equalsIgnoreTime **/
    public static boolean isSameDay(final Temporal a, final Temporal b) {
        return equalsIgnoreTime(a, b);
    }
    
    /**
     * Gets the difference in days between two Calendar objects, ignoring time (diff between 23:59 and 00:01 on consecutive days is 1)
     */
    public static int getDifferenceInDays(final Temporal a, final Temporal b) {
        LocalDate start = LocalDate.from(a);
        LocalDate end = LocalDate.from(b);
    	return Days.between(start, end).getAmount();
    }
    
    /**
     * Gets the difference in weeks between two Calendar objects, rounded UP
     */
    public static int getDifferenceInWeeks(final Temporal a, final Temporal b) {
    	int days = getDifferenceInDays(a, b);
    	int weeks = days / DAYS_PER_WEEK;
    	if (days % 7 != 0) {
    		weeks++;
    	}
    	return weeks;
    }

    public static boolean isBetween(final ChronoLocalDate dt, final ChronoLocalDate earliest, final ChronoLocalDate latest) {
        return (dt.isEqual(earliest) || dt.isEqual(latest) ||
            (dt.isAfter(earliest) && dt.isBefore(latest)));
    }

    public static boolean isBetween(final ChronoLocalDateTime<?> dt, final ChronoLocalDateTime<?> earliest, final ChronoLocalDateTime<?> latest) {
        return (dt.isEqual(earliest) || dt.isEqual(latest) ||
            (dt.isAfter(earliest) && dt.isBefore(latest)));
    }
    
    public static boolean isBetween(final ChronoZonedDateTime<?> dt, final ChronoZonedDateTime<?> earliest, final ChronoZonedDateTime<?> latest) {
        return (dt.isEqual(earliest) || dt.isEqual(latest) ||
                (dt.isAfter(earliest) && dt.isBefore(latest)));
    }

    /**
     * NOT threadsafe. Used for testing.
     * Does an action with a mockdatetime.
     */
    public static void useMockDateTime(final TemporalAccessor dt, final Runnable fn) {
        try {
            CLOCK_IMPLEMENTATION = Clock.fixed(toInstant(dt), ZoneId.systemDefault());
            fn.run();
        } finally {
            CLOCK_IMPLEMENTATION = Clock.systemDefaultZone();
        }
    }

    /**
     * Converts the common date-time types into an Instant. A date and a time are
     * required components - if there is no zone information then the default zone
     * is used.
     */
    private static Instant toInstant(final TemporalAccessor t) {
        if (t instanceof Instant) return (Instant) t;

        LocalDate date = TemporalQueries.localDate().queryFrom(t);
        LocalTime time = TemporalQueries.localTime().queryFrom(t);
        ZoneId zone = TemporalQueries.zone().queryFrom(t);
        ZoneOffset offset = TemporalQueries.offset().queryFrom(t);

        LocalDateTime datetime = date.atTime(time);
        if (zone != null) {
            return datetime.atZone(zone).toInstant();
        } else if (offset != null) {
            return datetime.atOffset(offset).toInstant();
        } else {
            return datetime.atZone(ZoneId.systemDefault()).toInstant();
        }
    }

}