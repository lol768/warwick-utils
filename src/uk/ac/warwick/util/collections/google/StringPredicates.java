package uk.ac.warwick.util.collections.google;

import com.google.common.base.Predicate;

 
public final class StringPredicates {

    private StringPredicates() {
        // utility class
    }

    public static Predicate<String> startsWith(final String s) {
        return new Predicate<String>() {
            public boolean apply(String test) {
                if (s== null || test == null) return false;
                
                return test.startsWith(s);
            }            
        };
    }
    public static Predicate<String> isStartOf(final String s){
        return new Predicate<String>() {
            public boolean apply(String test) {
                if (s== null || test == null) return false;
                
                return s.startsWith(test);
            }
        };
    }
}
