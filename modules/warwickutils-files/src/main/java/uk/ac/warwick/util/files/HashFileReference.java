package uk.ac.warwick.util.files;

import uk.ac.warwick.util.files.hash.HashString;


/**
 * A file reference that can is looked up and served by a hash rather than a
 * filename.
 */
public interface HashFileReference extends FileReference {
    
    /**
     * The full hash string that identifies this reference uniquely.
     */
    HashString getHash();

}
