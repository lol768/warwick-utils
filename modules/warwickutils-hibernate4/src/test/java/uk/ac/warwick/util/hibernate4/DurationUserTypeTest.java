package uk.ac.warwick.util.hibernate4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.joda.time.Duration;
import org.junit.Test;

public final class DurationUserTypeTest {
    
    private Mockery m = new JUnit4Mockery();
    
    @Test
    public void get0Returns0() throws Exception {
        DurationUserType type = DurationUserType.INSTANCE;
        
        final String columnName = "MILLIS_POSTS_EDITABLE_FOR";
        
        final ResultSet rs = m.mock(ResultSet.class);
        m.checking(new Expectations() {{
            oneOf(rs).getLong(columnName); will(returnValue(0L));
            oneOf(rs).wasNull(); will(returnValue(false));
        }});
        
        Duration result = (Duration)type.nullSafeGet(rs, columnName);
        
        assertNotNull(result);
        assertTrue(result.getMillis() == 0L);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void getNumberReturnsNumber() throws Exception {
        DurationUserType type = DurationUserType.INSTANCE;
        
        final String columnName = "MILLIS_POSTS_EDITABLE_FOR";
        
        final ResultSet rs = m.mock(ResultSet.class);
        m.checking(new Expectations() {{
            oneOf(rs).getLong(columnName); will(returnValue(32768L));
            oneOf(rs).wasNull(); will(returnValue(false));
        }});
        
        Duration result = (Duration)type.nullSafeGet(rs, columnName);
        
        assertNotNull(result);
        assertTrue(result.getMillis() == 32768L);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void getNullReturnsNull() throws Exception {
        DurationUserType type = DurationUserType.INSTANCE;
        
        final String columnName = "MILLIS_POSTS_EDITABLE_FOR";
        
        final ResultSet rs = m.mock(ResultSet.class);
        m.checking(new Expectations() {{
            oneOf(rs).getLong(columnName); will(returnValue(0L));
            oneOf(rs).wasNull(); will(returnValue(true));
        }});
        
        Duration result = (Duration)type.nullSafeGet(rs, columnName);
        
        assertNull(result);
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void setLongReturnsNumber() throws Exception {
        DurationUserType type = DurationUserType.INSTANCE;
        
        final PreparedStatement st = m.mock(PreparedStatement.class);
        m.checking(new Expectations() {{
            oneOf(st).setLong(0, 42L);
        }});
        
        type.nullSafeSet(st, 42L, 0, null);
        
        m.assertIsSatisfied();
    }
        
    @Test
    public void setIntegerReturnsNumber() throws Exception {
        DurationUserType type = DurationUserType.INSTANCE;
        
        final PreparedStatement st = m.mock(PreparedStatement.class);
        m.checking(new Expectations() {{
            oneOf(st).setLong(0, 42);
        }});
        
        type.nullSafeSet(st, 42, 0, null);
        
        m.assertIsSatisfied();
    }
        
    @Test
    public void setNullReturnsNull() throws Exception {
        DurationUserType type = DurationUserType.INSTANCE;
        
        final PreparedStatement st = m.mock(PreparedStatement.class);
        m.checking(new Expectations() {{
            oneOf(st).setNull(0, Types.BIGINT);
        }});
        
        type.nullSafeSet(st, null, 0, null);
        
        m.assertIsSatisfied();
    }
    
    
    @Test
    public void trueIsCopied() throws Exception {
        DurationUserType type = DurationUserType.INSTANCE;
        
        Duration src = new Duration(678);
        
        Duration copy = (Duration)type.deepCopy(src);
        
        assertNotNull(copy);
        assertEquals(src, copy);
        assertTrue(copy.getMillis() == 678);
    }
        
    @Test
    public void nullIsCopied() throws Exception {
        DurationUserType type = DurationUserType.INSTANCE;
        
        Duration src = null;
        
        Duration copy = (Duration)type.deepCopy(src);
        
        assertNull(copy);
    }
    
    @Test
    public void equals() throws Exception {
        DurationUserType type = DurationUserType.INSTANCE;
        
        assertTrue(type.equals(0, 0));
        assertTrue(type.equals(890L, 890L));
        assertFalse(type.equals(890L, 0));
        assertFalse(type.equals(0, 890L));
    }
    
    @Test
    public void equalsWithNulls() throws Exception {
        DurationUserType type = DurationUserType.INSTANCE;
        
        assertFalse(type.equals(890L, null));
        assertFalse(type.equals(null, 890L));
        assertFalse(type.equals(0, null));
        assertFalse(type.equals(null, 0));
        
        // we want nulls to equate, so that we don't UPDATE foo SET null_field = null every time it's accessed
        assertTrue(type.equals(null, null));
    }

}
