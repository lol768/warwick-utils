package uk.ac.warwick.util.files;

import java.io.IOException;

public interface LocalFileStore extends FileStore {
    
    /**
     * Stores a new uploaded file in the filestore and returns a reference.
     */
    LocalFileReference storeLocalReference(Storeable storeable, UploadedFileDetails uploadedFile) throws IOException;

    LocalFileReference copy(LocalFileReference fileReference, Storeable target) throws IOException;
    
    LocalFileReference rename(LocalFileReference fileReference, Storeable target) throws IOException;

}
