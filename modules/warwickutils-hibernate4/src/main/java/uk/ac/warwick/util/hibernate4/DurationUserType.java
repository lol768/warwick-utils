package uk.ac.warwick.util.hibernate4;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.usertype.UserType;
import org.joda.time.Duration;

/**
 * Persistent Duration class
 * 
 * @author Nick Kaijaks
 */
public final class DurationUserType implements UserType {

    public static final DurationUserType INSTANCE = new DurationUserType();

    private static final int[] SQL_TYPES = new int[] { Types.BIGINT };

    public DurationUserType() {
        // all user types must have an empty public constructor
    }

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object object) throws HibernateException, SQLException {
        return nullSafeGet(rs, names[0]);
    }

    public Object nullSafeGet(ResultSet rs, String name) throws SQLException {
        long millis = rs.getLong(name);
        if (rs.wasNull()) {
            return null;
        }
        
        return new Duration(millis);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.BIGINT);
        } else if (value instanceof Duration) {
            st.setLong(index, ((Duration)value).getMillis());
        } else if (value instanceof Long) {
            st.setLong(index, ((Long)value));
        } else if (value instanceof Integer) {
            st.setLong(index, ((Integer)value).longValue());
        } else {
            throw new IllegalStateException("value is a " + value.getClass().getName());
        }
    }

    public boolean isMutable() {
        // Durations are immutable
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
        // Durations are serializable, so we can just return the value
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

    public Class<Duration> returnedClass() {
        return Duration.class;
    }

}
