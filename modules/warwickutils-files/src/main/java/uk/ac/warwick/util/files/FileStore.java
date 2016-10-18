package uk.ac.warwick.util.files;

import com.google.common.io.ByteSource;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Abstraction of the filesystem for file backed content. Files are handled
 * indirectly through FileReferences - by never handling a File directly, we can
 * do things like have two separate references share the same data in one file.
 */
public interface FileStore {
    
    /**
     * Creates a new FileReference, using the given delegate to write the
     * initial data to it.
     * 
     * When you do store, don't forget to assign the returned FileReference
     * to the object that holds the data. (You'd be surprised at how
     * easy this is to miss out).
     * 
     * @param storeable
     *      The Storeable that the file reference should be associated with.
     * @param requestedStoreName
     *            The requested file store if it is being placed into one of multiple hash-based stores.
     *            If the file ends up getting placed in local store, this is ignored.
     */
    FileReference store(Storeable storeable, String requestedStoreName, ByteSource delegate) throws IOException;

    /**
     * Returns a reference to a file for this storeable.
     */
    FileReference get(Storeable storeable) throws FileNotFoundException;

}
