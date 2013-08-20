package uk.ac.warwick.util.hibernate4;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.jmock.Expectations;
import org.junit.Test;

public final class OracleEmptyStringUserTypeTest extends UserTypeTestBase {

    @Test
    public void getNotEmpty() throws Exception {
        OracleEmptyStringUserType type = OracleEmptyStringUserType.INSTANCE;
        
        final String columnName = "STRING_VALUE";
        
        final ResultSet rs = m.mock(ResultSet.class);
        m.checking(new Expectations() {{
            oneOf(rs).getString(columnName); will(returnValue("something"));
            oneOf(rs).wasNull(); will(returnValue(false));
        }});
        
        String str = type.nullSafeGet(rs, new String[] {columnName}, sessionImplementor, null);
        
        assertNotNull(str);
        assertEquals("something", str);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void getEmpty() throws Exception {
        OracleEmptyStringUserType type = OracleEmptyStringUserType.INSTANCE;
        
        final String columnName = "STRING_VALUE";
        
        final ResultSet rs = m.mock(ResultSet.class);
        m.checking(new Expectations() {{
            oneOf(rs).getString(columnName); will(returnValue(OracleEmptyStringUserType.MARK_EMPTY));
            oneOf(rs).wasNull(); will(returnValue(false));
        }});
        
        String str = type.nullSafeGet(rs, new String[] {columnName}, sessionImplementor, null);
        
        assertNotNull(str);
        assertEquals("", str);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void getNull() throws Exception {
        OracleEmptyStringUserType type = OracleEmptyStringUserType.INSTANCE;
        
        final String columnName = "STRING_VALUE";
        
        final ResultSet rs = m.mock(ResultSet.class);
        m.checking(new Expectations() {{
            oneOf(rs).getString(columnName); will(returnValue(null));
        }});
        
        String str = type.nullSafeGet(rs, new String[] {columnName}, sessionImplementor, null);
        assertNull(str);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void setNotEmpty() throws Exception {
        OracleEmptyStringUserType type = OracleEmptyStringUserType.INSTANCE;
        
        final PreparedStatement st = m.mock(PreparedStatement.class);
        m.checking(new Expectations() {{
            oneOf(st).setString(0, "something");
        }});
        
        type.nullSafeSet(st, "something", 0, sessionImplementor);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void setEmpty() throws Exception {
        OracleEmptyStringUserType type = OracleEmptyStringUserType.INSTANCE;
        
        final PreparedStatement st = m.mock(PreparedStatement.class);
        m.checking(new Expectations() {{
            oneOf(st).setString(0, OracleEmptyStringUserType.MARK_EMPTY);
        }});
        
        type.nullSafeSet(st, "", 0, sessionImplementor);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void setNull() throws Exception {
        OracleEmptyStringUserType type = OracleEmptyStringUserType.INSTANCE;
        
        final PreparedStatement st = m.mock(PreparedStatement.class);
        m.checking(new Expectations() {{
            oneOf(st).setNull(0, Types.VARCHAR);
        }});
        
        type.nullSafeSet(st, null, 0, sessionImplementor);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void copy() throws Exception {
        OracleEmptyStringUserType type = OracleEmptyStringUserType.INSTANCE;
        
        assertEquals("something", type.deepCopy("something"));
        assertEquals("", type.deepCopy(""));
        assertNull(type.deepCopy(null));
    }
    
    @Test
    public void equals() throws Exception {
        OracleEmptyStringUserType type = OracleEmptyStringUserType.INSTANCE;
        
        assertTrue(type.equals("something", "something"));
        assertTrue(type.equals(null, null));
        assertTrue(type.equals("", ""));
        assertTrue(type.equals(OracleEmptyStringUserType.MARK_EMPTY, OracleEmptyStringUserType.MARK_EMPTY));
        assertFalse(type.equals("", null));
        assertFalse(type.equals(null, ""));
    }

}
