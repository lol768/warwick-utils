package uk.ac.warwick.util.hibernate4;

import org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime;

/**
 * An attempt to make a more efficient version of PersistentDateTime
 * 
 * @author Mat
 * @deprecated Use {@link PersistentLocalDateTime}
 */
public final class DateTimeDataType extends PersistentLocalDateTime {

    public static final DateTimeDataType INSTANCE = new DateTimeDataType();

}
