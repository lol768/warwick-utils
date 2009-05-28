package uk.ac.warwick.util.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.hibernate.util.EqualsHelper;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

/**
 * An attempt to make a more efficient version of PersistentDateTime
 * 
 * @author Mat
 */
public final class DateTimeDataType implements UserType {

    public static final DateTimeDataType INSTANCE = new DateTimeDataType();
    
    private static final Chronology DEFAULT_CHRONOLOGY = ISOChronology.getInstance();

    private static final int[] SQL_TYPES = new int[] { Types.TIMESTAMP };

    public DateTimeDataType() {
        // all user types must have an empty public constructor
    }

    public int[] sqlTypes() {
        return SQL_TYPES;
    }
    
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        Timestamp ts = rs.getTimestamp(names[0]);
        if (ts == null) {
            return null;
        }
        
        return new DateTime(ts.getTime(), DEFAULT_CHRONOLOGY);
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.TIMESTAMP);
        } else {
            Timestamp ts = new Timestamp(((DateTime)value).getMillis());
            st.setTimestamp(index, ts);
        }
    }

    public boolean isMutable() {
        // DateTimes are immutable
        return false;
    }

    public Object deepCopy(Object value) throws HibernateException {
        // "It is not necessary to copy immutable objects"
        return value;
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        // Just return the cached version
        return cached;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        // DateTimes are serializable, so we can just return the value
        return (Serializable) value;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return EqualsHelper.equals(x, y);
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        // "For immutable objects, it is safe to just return the first parameter"
        return original;
    }

    public Class<DateTime> returnedClass() {
        return DateTime.class;
    }

}
