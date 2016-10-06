package uk.ac.warwick.util.files;

import com.google.common.io.ByteSource;

/**
 * Return the same strategy all the time, for easy testing.
 */
public final class StaticFileReferenceCreationStrategy implements FileReferenceCreationStrategy {

    private final Target strategy;   
    
    public StaticFileReferenceCreationStrategy(Target s) {
        this.strategy = s;
    }
    
    /**
     * A strategy that will always choose local storage.
     */
    public static StaticFileReferenceCreationStrategy hash() {
        return new StaticFileReferenceCreationStrategy(Target.hash);
    }
    
    public Target select(ByteSource in) {
        return strategy;
    }

}
