package uk.ac.warwick.util.hibernate;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

@SuppressWarnings("unchecked")
public abstract class AbstractEnumUserTypeTest<E extends Enum<E>> {
    
    private final Mockery m = new JUnit4Mockery();
    
    protected abstract E[] values();
    
    protected abstract EnumUserType<E> getUserType();
    
    @Test
    public final void get() throws Exception {
        EnumUserType<E> type = getUserType();
        
        final String columnName = "ENUM_VALUE";
        for (final E enumValue : values()) {
            final ResultSet rs = m.mock(ResultSet.class, "rs_" + enumValue.name());            
            m.checking(new Expectations() {{
                one(rs).getString(columnName); will(returnValue(enumValue.name()));
            }});
            
            E returnedValue = (E)type.nullSafeGet(rs, new String[] {columnName}, null);
            assertNotNull(returnedValue);
            assertEquals(enumValue, returnedValue);
            
            m.assertIsSatisfied();
        }
    }
    
    @Test
    public final void set() throws Exception {
        EnumUserType<E> type = getUserType();
        
        for (final E enumValue : values()) {
            final PreparedStatement st = m.mock(PreparedStatement.class, "ps_" + enumValue.name());
            m.checking(new Expectations() {{
                one(st).setString(0, enumValue.name());
            }});
            
            type.nullSafeSet(st, enumValue, 0);
            
            m.assertIsSatisfied();
        }
    }
    
    @Test
    public final void getNull() throws Exception {
        EnumUserType<E> type = getUserType();
        
        final String columnName = "ENUM_VALUE";
        final ResultSet rs = m.mock(ResultSet.class, "rs");            
        m.checking(new Expectations() {{
            one(rs).getString(columnName); will(returnValue(null));
        }});
        
        E returnedValue = (E)type.nullSafeGet(rs, new String[] {columnName}, null);
        assertNull(returnedValue);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public final void setNull() throws Exception {
        EnumUserType<E> type = getUserType();
        
        final PreparedStatement st = m.mock(PreparedStatement.class, "ps");
        m.checking(new Expectations() {{
            one(st).setNull(0, Types.VARCHAR);
        }});
        
        type.nullSafeSet(st, null, 0);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public final void copy() throws Exception {
        EnumUserType<E> type = getUserType();
        
        for (final E enumValue : values()) {
            assertEquals(enumValue, type.deepCopy(enumValue));
        }
        
        assertNull(type.deepCopy(null));
    }
    
    @Test
    public final void equals() throws Exception {
        EnumUserType<E> type = getUserType();
        
        for (final E enumValue : values()) {
            assertTrue(type.equals(enumValue, enumValue));
            
            for (final E otherValue : values()) {
                if (otherValue != enumValue) {
                    assertFalse(type.equals(enumValue, otherValue));
                    assertFalse(type.equals(otherValue, enumValue));
                }
            }
            
            assertFalse(type.equals(enumValue, null));
            assertFalse(type.equals(null, enumValue));
            assertTrue(type.equals(null, null));
        }
    }

}
