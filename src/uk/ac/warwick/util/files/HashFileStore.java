package uk.ac.warwick.util.files;

import java.io.IOException;

public interface HashFileStore extends FileStore {
    
    /**
     * Stores a new uploaded file in the filestore and returns a reference.
     * @throws IOException 
     */
    HashFileReference storeHashReference(UploadedFileDetails uploadedFile, String requestedStoreName) throws IOException;
    
    /**
     * Stores a new uploaded file in the filestore and returns a reference.
     * @throws IOException 
     */
    HashFileReference createHashReference(UsingOutput callback, String requestedStoreName) throws IOException;

}
