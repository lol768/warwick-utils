package uk.ac.warwick.util.collections.google;

import junit.framework.TestCase;

import com.google.common.base.Predicate;

public class StringPredicatesTest extends TestCase {

    public void testStartsWith() {
        Predicate<String> p = StringPredicates.startsWith("a");
        assertTrue(p.apply("aardvark"));
        assertFalse(p.apply("zebra"));
        assertFalse(p.apply(null)); 
                
        Predicate<String> broken = StringPredicates.startsWith(null);
        assertFalse(broken.apply("anything"));
    }
    
    public void testIsStartOf() {
        Predicate<String> p = StringPredicates.isStartOf("fish");
        assertTrue(p.apply("f"));
        assertTrue(p.apply("fi"));
        assertFalse(p.apply("ish"));
        assertFalse(p.apply(null));
        
        Predicate<String> broken = StringPredicates.isStartOf(null);
        assertFalse(broken.apply("anything"));
    }
}
