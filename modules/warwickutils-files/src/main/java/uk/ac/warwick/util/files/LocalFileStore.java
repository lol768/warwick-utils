package uk.ac.warwick.util.files;

import com.google.common.io.ByteSource;

import java.io.IOException;

public interface LocalFileStore extends FileStore {
    
    /**
     * Stores a new uploaded file in the filestore and returns a reference.
     */
    LocalFileReference storeLocalReference(Storeable storeable, ByteSource in) throws IOException;

    LocalFileReference copy(LocalFileReference fileReference, Storeable target) throws IOException;
    
    LocalFileReference rename(LocalFileReference fileReference, Storeable target) throws IOException;

}
