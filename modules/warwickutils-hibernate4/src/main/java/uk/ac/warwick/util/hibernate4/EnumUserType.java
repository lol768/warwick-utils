package uk.ac.warwick.util.hibernate4;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;


public abstract class EnumUserType<E extends Enum<E>> implements UserType {
    private static final int[] SQL_TYPES = { Types.VARCHAR };

    private Class<E> clazz;

    protected EnumUserType(Class<E> c) {
        this.clazz = c;
    }

    public final int[] sqlTypes() {
        return SQL_TYPES;
    }

    public final Class<E> returnedClass() {
        return clazz;
    }

    @Override
    public final Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        String name = resultSet.getString(names[0]);
        return name == null ? null : Enum.valueOf(clazz, name);
    }

    @Override
    public final void nullSafeSet(PreparedStatement preparedStatement, Object value, int index, SessionImplementor session) throws HibernateException,
            SQLException {
        if (null == value) {
            preparedStatement.setNull(index, Types.VARCHAR);
        } else {
            preparedStatement.setString(index, ((Enum) value).name());
        }
    }

    public final Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public final boolean isMutable() {
        return false;
    }

    public final Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public final Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    public final Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    public final int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public final boolean equals(Object x, Object y) throws HibernateException {
        if (x == y)
            return true;
        if (null == x || null == y)
            return false;
        return x.equals(y);
    }
}