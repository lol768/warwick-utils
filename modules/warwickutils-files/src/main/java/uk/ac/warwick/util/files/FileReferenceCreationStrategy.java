package uk.ac.warwick.util.files;

import com.google.common.io.ByteSource;

/**
 * A strategy for storing files. In this model, large files can be farmed out to
 * separate storage.
 */
public interface FileReferenceCreationStrategy {
    
    enum Target {
        /** Store in standard URL-based hierarchy */
        local,
        /** Store by a hash of the data - though we don't specify _which_ hash store at this point. */
        hash
    }
   
    /**
     * Choose a storage strategy based on this ByteSource. Obviously this is
     * a temporary file, as we haven't yet decided where to keep the data -
     * that's what this method is here to find out! 
     */
    Target select(ByteSource in);

}
