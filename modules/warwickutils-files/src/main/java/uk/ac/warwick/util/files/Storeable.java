package uk.ac.warwick.util.files;

import java.io.File;

import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.HashString;

/**
 * Indicates that this class is Storeable, that is that it can be stored in a
 * {@link FileStore} to create {@link FileReference}s which can be retrieved
 * later.
 */
public interface Storeable {

    interface StorageStrategy {
        enum MissingContentStrategy {
            /** Return a FileBackedFileReference pointing to a non-existant File */
            Local,
            
            /** Return an EmptyHashBackedFileReference */
            Hash,
            
            /** Throw a FileNotFoundException if the content doesn't exist */
            Exception 
        };
        
        /**
         * Return the root path. For filesystem storeables this will be a path that can
         * be resolved to a directory; for blob store it will be the container name.
         */
        String getRootPath();

        /**
         * Return the {@link MissingContentStrategy} for returning a
         * FileReference when the content doesn't exist.
         */
        MissingContentStrategy getMissingContentStrategy();
        
        /**
         * Return the default Hash store name.
         */
        String getDefaultHashStore();
        
        /**
         * Returns true if this storeable supports local references.
         */
        boolean isSupportsLocalReferences();
    }

    /**
     * Return the path related to this Storeable. <em>Should</em> return
     * <code>null</code> if {@link #getHash()} does not return null.
     */
    String getPath();

    /**
     * Return a hash related to this Storeable that can be resolved using a
     * {@link FileHashResolver}.
     */
    HashString getHash();

    /**
     * Get the {@link StorageStrategy} for this Storeable. This allows us to
     * discern how to retrieve the actual files that are backed by this
     * Storeable.
     */
    StorageStrategy getStrategy();

}
