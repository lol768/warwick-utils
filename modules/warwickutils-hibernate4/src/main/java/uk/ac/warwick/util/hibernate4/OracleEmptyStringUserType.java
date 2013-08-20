package uk.ac.warwick.util.hibernate4;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;

/**
 * Escapes empty strings and uses a keyword, so that dumbass Oracle doesn't
 * break the &lt;map&gt; type.
 */
public final class OracleEmptyStringUserType implements UserType {
    
    public static final OracleEmptyStringUserType INSTANCE = new OracleEmptyStringUserType();

    public static final String MARK_EMPTY = "<EmptyString/>";

    private static final int[] TYPES = { Types.VARCHAR };

    public int[] sqlTypes() {
        return TYPES;
    }

    public Class<String> returnedClass() {
        return String.class;
    }

    public boolean equals(Object x, Object y) {
        if (x == y)
            return true;
        if (x == null || y == null)
            return false;
        return x.equals(y);
    }

    public Object deepCopy(Object x) {
        return x;
    }

    public boolean isMutable() {
        return false;
    }

    @Override
    public String nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        String dbValue = StringType.INSTANCE.nullSafeGet(rs, names[0], session);
        if (dbValue != null) {
            return unescape(dbValue);
        } else {
            return null;
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        if (value != null) {
            String v = escape(value.toString());
            StringType.INSTANCE.nullSafeSet(st, v, index, session);
        } else {
            StringType.INSTANCE.nullSafeSet(st, null, index, session);
        }
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    public Serializable disassemble(Object value) {
        return (Serializable) deepCopy(value);
    }

    /**
     * Escape a string by quoting the string.
     */
    private String escape(String string) {
        return ((string == null) || (string.length() == 0)) ? MARK_EMPTY : string;
    }

    /**
     * Unescape by removing the quotes
     */
    private String unescape(String string) throws HibernateException {
        if ((string == null) || (MARK_EMPTY.equals(string))) {
            return "";
        }
        return string;
    }

    public int hashCode(Object string) throws HibernateException {
        return string.hashCode();
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
