package uk.ac.warwick.util.files.hash;

import static org.junit.Assert.*;

import org.junit.Test;


public class HashStringTest {
    @Test public void unqualified() {
        HashString hash = new HashString("3782139f8038493280438a8a0");
        assertEquals("3782139f8038493280438a8a0", hash.getHash());
        assertEquals(null, hash.getStoreName());
        assertTrue(hash.isDefaultStore());
    }
    
    @Test public void qualified() {
        HashString hash = new HashString("html/3782139f8038493280438a8a0");
        assertEquals("3782139f8038493280438a8a0", hash.getHash());
        assertEquals("html", hash.getStoreName());
        assertFalse(hash.isDefaultStore());
    }
    
    @Test public void defaultStore() {
        HashString hash = new HashString("default/12345678");
        assertEquals("12345678", hash.getHash());
        assertEquals("12345678", hash.toString());
        assertTrue(hash.isDefaultStore());
    }
    
    @Test public void defaultStoreConstructor() {
        HashString hash = new HashString("default", "12345678");
        assertEquals("12345678", hash.getHash());
        assertEquals("12345678", hash.toString());
        assertTrue(hash.isDefaultStore());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void emptyHash() throws Exception {
        new HashString("html/");
    }
    
    @Test public void nullInput() throws Exception {
        HashString hash = new HashString(null);
        assertTrue(hash.isEmpty());
    }
}
