package uk.ac.warwick.util.files;

import java.io.File;

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
    
    public Target select(UploadedFileDetails file) {
        return strategy;
    }

    public Target select(File temporaryFile) {
        return strategy;
    }

}
