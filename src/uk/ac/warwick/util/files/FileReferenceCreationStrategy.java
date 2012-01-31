package uk.ac.warwick.util.files;

import java.io.File;

/**
 * A strategy for storing files. In this model, large files can be farmed out to
 * separate storage.
 */
public interface FileReferenceCreationStrategy {
    
    static enum Target {
        /** Store in standard URL-based hierarchy */
        local,
        /** Store by a hash of the data - though we don't specify _which_ hash store at this point. */
        hash
    };
    
    /**
     * Choose a storage strategy based on this FileDetails.
     */
    Target select(UploadedFileDetails file);
   
    /**
     * Choose a storage strategy based on this File. Obviously this is
     * a temporary file, as we haven't yet decided where to keep the data -
     * that's what this method is here to find out! 
     */
    Target select(File temporaryFile);

}
