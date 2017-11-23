package uk.ac.warwick.util.hibernate4;

import org.jadira.usertype.dateandtime.threeten.PersistentDurationAsMillisLong;

/**
 * Persistent Duration class
 * 
 * @author Nick Kaijaks
 * @deprecated Use {@link PersistentDurationAsMillisLong}
 */
public final class DurationUserType extends PersistentDurationAsMillisLong {

    public static final DurationUserType INSTANCE = new DurationUserType();

}
