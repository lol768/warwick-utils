package uk.ac.warwick.util.core;

import java.util.Calendar;

public final class TimeUtils {
    private static final int DAYS_IN_WEEK = 7;
    
    private TimeUtils() {}
    
    /**
     * Returns whether two Calendar objects represent the same day
     * (ignoring their time components).
     * 
     * Calendars are mutable, so this method is not thread safe.
     * Ensure they aren't modified, or use clones.
     * 
     * Reflexive.
     */
    public static boolean equalsIgnoreTime(final Calendar a, final Calendar b) {
        return (
                a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH)
             && a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
             && a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
        );
    }
    
    /** Synonym for equalsIgnoreTime **/
    public static boolean isSameDay(final Calendar a, final Calendar b) {
        return equalsIgnoreTime(a, b);
    }
    
    /**
     * Gets the difference in days between two Calendar objects, rounded UP (diff between 23:59 and 00:01 on consecutive days is 1)
     * 
     * TODO the time for this is O(n) for n days difference. Bad!
     */
    public static int getDifferenceInDays(final Calendar aOriginal, final Calendar b) {
        Calendar a = (Calendar)aOriginal.clone();
        int diff = 0;
        
        if (b.before(a) || b.equals(a)) {
            return 0;
        }
        
        while (!equalsIgnoreTime(a,b)) {
            a.add(Calendar.DATE, 1);
            diff++;
        }
        return diff;
    }
    
    /**
     * Gets the difference in weeks between two Calendar objects, rounded UP
     */
    public static int getDifferenceInWeeks(final Calendar aOriginal, final Calendar b) {
        int daysDiff = getDifferenceInDays(aOriginal, b);
        return (int)Math.ceil((double)daysDiff / DAYS_IN_WEEK);
    }
}
