package uk.ac.warwick.util.csv;

/**
 * Strange interface/factory that makes DuplicateResolvers, whose job it is to
 * return alternate values when something already exists.
 * 
 * 
 * 
 * @author cusebr
 */
public abstract class DuplicateResolver <T>  {
    
    public abstract T getAlternate(T existingThing);
    
    public static DuplicateResolver<String> incrementingNumber() {
        return new IncrementingNumberDuplicateResolver();
    }
    
    static class IncrementingNumberDuplicateResolver extends DuplicateResolver<String> {
        private int number = 2;
        @Override
        public String getAlternate(String existingName) {
            String result = existingName + " " + number;
            number++;
            return result;
        }
    }
}
