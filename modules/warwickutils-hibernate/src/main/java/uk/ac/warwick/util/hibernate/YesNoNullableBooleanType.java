package uk.ac.warwick.util.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;

public final class YesNoNullableBooleanType implements EnhancedUserType {

    public static final YesNoNullableBooleanType INSTANCE = new YesNoNullableBooleanType();

    private static final int[] SQL_TYPES = new int[] { Types.VARCHAR };

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    public Class<Boolean> returnedClass() {
        return Boolean.class;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        
        Boolean bx = (Boolean) x;
        Boolean by = (Boolean) y;

        return bx.equals(by);
    }

    public int hashCode(Object object) throws HibernateException {
        return object.hashCode();
    }

    public Object nullSafeGet(ResultSet resultSet, String[] strings, Object object) throws HibernateException, SQLException {
        return nullSafeGet(resultSet, strings[0]);

    }

    public Object nullSafeGet(ResultSet resultSet, String string) throws SQLException {
        Object str = Hibernate.STRING.nullSafeGet(resultSet, string);
        
        if (str == null) {
            // Default to false
            return Boolean.FALSE;
        }

        return str.equals("Y");
    }

    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            Hibernate.STRING.nullSafeSet(preparedStatement, null, index);
        } else {
            Hibernate.STRING.nullSafeSet(preparedStatement, ((Boolean) value).booleanValue() ? "Y" : "N", index);
        }
    }

    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }

        return new Boolean((Boolean)value);
    }

    public boolean isMutable() {
        return false;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    public Object assemble(Serializable cached, Object value) throws HibernateException {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    public String objectToSQLString(Object object) {
        throw new UnsupportedOperationException();
    }

    public String toXMLString(Object object) {
        return object.toString();
    }

    public Object fromXMLString(String string) {
        return new Boolean(string);
    }

}
