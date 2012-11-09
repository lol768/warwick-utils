package uk.ac.warwick.util.core;

public class NumberUtils {
    
    private static final String prefix = "22250738";
    private static final String suffix = "585072012";
    
    /**
     * "2.2250738585072012e-308" is a string that cannot be parsed due to a Java bug.
     * 
     */
    public static final double parseDouble(String d) throws NumberFormatException {
       if ( isPossibleBugDouble(d) ) {
           throw new NumberFormatException("Problem parsing");
       }
       return Double.parseDouble(d);
    }

    /**
     * Is non-empty
     * Starts with a digit
     * Contains one of the two digit sequences found in the offending number
     */
    public static boolean isPossibleBugDouble(String d) {
        return 
            d.length() > 0
            && Character.isDigit(d.charAt(0))
            && containsPossibleBugDouble(d);
    }
    
    public static boolean containsPossibleBugDouble(String d) {
        return d.contains(prefix) || d.contains(suffix);
    }
    
}
