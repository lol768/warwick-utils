package uk.ac.warwick.util.hibernate4;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.jmock.Expectations;
import org.junit.Test;

public final class YesNoNullableBooleanTypeTest extends UserTypeTestBase {
    
    @Test
    public void getYReturnsTrue() throws Exception {
        YesNoNullableBooleanType type = YesNoNullableBooleanType.INSTANCE;
        
        final String columnName = "IS_TRUE";
        
        final ResultSet rs = m.mock(ResultSet.class);
        m.checking(new Expectations() {{
            one(rs).getString(columnName); will(returnValue("Y"));
            one(rs).wasNull(); will(returnValue(false));
        }});
        
        Boolean b = (Boolean)type.nullSafeGet(rs, columnName, sessionImplementor);
        
        assertNotNull(b);
        assertTrue(b.booleanValue());
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void getNReturnsFalse() throws Exception {
        YesNoNullableBooleanType type = YesNoNullableBooleanType.INSTANCE;
        
        final String columnName = "IS_TRUE";
        
        final ResultSet rs = m.mock(ResultSet.class);
        m.checking(new Expectations() {{
            one(rs).getString(columnName); will(returnValue("N"));
            one(rs).wasNull(); will(returnValue(false));
        }});
        
        Boolean b = (Boolean)type.nullSafeGet(rs, columnName, sessionImplementor);
        
        assertNotNull(b);
        assertFalse(b.booleanValue());
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void getNullReturnsFalse() throws Exception {
        YesNoNullableBooleanType type = YesNoNullableBooleanType.INSTANCE;
        
        final String columnName = "IS_TRUE";
        
        final ResultSet rs = m.mock(ResultSet.class);
        m.checking(new Expectations() {{
            one(rs).getString(columnName); will(returnValue(null));
        }});
        
        Boolean b = (Boolean)type.nullSafeGet(rs, columnName, sessionImplementor);
        
        assertNotNull(b);
        assertFalse(b.booleanValue());
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void setTrueReturnsY() throws Exception {
        YesNoNullableBooleanType type = YesNoNullableBooleanType.INSTANCE;
        
        final PreparedStatement st = m.mock(PreparedStatement.class);
        m.checking(new Expectations() {{
            one(st).setString(0, "Y");
        }});
        
        type.nullSafeSet(st, true, 0, sessionImplementor);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void setFalseReturnsN() throws Exception {
        YesNoNullableBooleanType type = YesNoNullableBooleanType.INSTANCE;
        
        final PreparedStatement st = m.mock(PreparedStatement.class);
        m.checking(new Expectations() {{
            one(st).setString(0, "N");
        }});
        
        type.nullSafeSet(st, false, 0, sessionImplementor);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void setNullReturnsNull() throws Exception {
        YesNoNullableBooleanType type = YesNoNullableBooleanType.INSTANCE;
        
        final PreparedStatement st = m.mock(PreparedStatement.class);
        m.checking(new Expectations() {{
            one(st).setNull(0, Types.VARCHAR);
        }});
        
        type.nullSafeSet(st, null, 0, sessionImplementor);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void trueIsCopied() throws Exception {
        YesNoNullableBooleanType type = YesNoNullableBooleanType.INSTANCE;
        
        boolean b = true;
        
        Boolean copy = (Boolean)type.deepCopy(b);
        
        assertNotNull(copy);
        assertEquals(b, copy);
        assertTrue(copy);
    }
    
    @Test
    public void falseIsCopied() throws Exception {
        YesNoNullableBooleanType type = YesNoNullableBooleanType.INSTANCE;
        
        boolean b = false;
        
        Boolean copy = (Boolean)type.deepCopy(b);
        
        assertNotNull(copy);
        assertEquals(b, copy);
        assertFalse(copy);
    }
    
    @Test
    public void nullIsCopied() throws Exception {
        YesNoNullableBooleanType type = YesNoNullableBooleanType.INSTANCE;
        
        Boolean b = null;
        
        Boolean copy = (Boolean)type.deepCopy(b);
        
        assertNull(copy);
    }
    
    @Test
    public void equals() throws Exception {
        YesNoNullableBooleanType type = YesNoNullableBooleanType.INSTANCE;
        
        assertTrue(type.equals(true, true));
        assertTrue(type.equals(false, false));
        assertFalse(type.equals(false, true));
        assertFalse(type.equals(true, false));
    }
    
    @Test
    public void equalsWithNulls() throws Exception {
        YesNoNullableBooleanType type = YesNoNullableBooleanType.INSTANCE;
        
        assertFalse(type.equals(true, null));
        assertFalse(type.equals(null, true));
        assertFalse(type.equals(false, null));
        assertFalse(type.equals(null, false));
        
        // null is mysteriously equal to null
        assertTrue(type.equals(null, null));
    }

}
