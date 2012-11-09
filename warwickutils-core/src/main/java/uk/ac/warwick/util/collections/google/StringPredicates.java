package uk.ac.warwick.util.collections.google;


 
public final class StringPredicates {

    private StringPredicates() {
        // utility class
    }

    public static BasePredicate<String> startsWith(final String s) {
        return new BasePredicate<String>() {
            public boolean apply(String test) {
                if (s== null || test == null) return false;
                
                return test.startsWith(s);
            }            
        };
    }
    public static BasePredicate<String> isStartOf(final String s){
        return new BasePredicate<String>() {
            public boolean apply(String test) {
                if (s== null || test == null) return false;
                
                return s.startsWith(test);
            }
        };
    }
}
