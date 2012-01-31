package uk.ac.warwick.util.files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

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
    FileReference store(Storeable storeable, String requestedStoreName, UsingOutput delegate) throws IOException;
    
    /**
     * Stores a new uploaded file in the filestore and returns a reference.
     */
    FileReference store(Storeable storeable, String requestedStoreName, UploadedFileDetails uploadedFile) throws IOException;

    /**
     * Returns a reference to a file for this storeable.
     */
    FileReference get(Storeable storeable) throws FileNotFoundException;

    /**
     * Helper specialisation of {@link #get(Storeable)} which takes only a
     * file name, not a hash, and is guaranteed to return a local file reference
     * if one exists.
     * <p>
     * This is mainly used for returning source files.
     */
    LocalFileReference getForPath(Storeable storeable, String path) throws FileNotFoundException;

    /**
     * Callback which provides an OutputStream to write to. Implementations
     * do not need to close the OutputStream, but they must close any
     * input resources after doWith is called. Consequently, any method
     * that is passed a UsingOutput MUST call it, to ensure that the resources
     * are closed.
     */
    static interface UsingOutput {
        void doWith(OutputStream output) throws IOException;
    }
}
