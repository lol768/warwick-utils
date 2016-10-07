package uk.ac.warwick.util.files;

import com.google.common.io.ByteSource;

import java.io.IOException;

public interface HashFileStore extends FileStore {
    
    /**
     * Stores a new uploaded file in the filestore and returns a reference.
     * @throws IOException 
     */
    HashFileReference storeHashReference(ByteSource in, String requestedStoreName) throws IOException;
    
    /**
     * Stores a new uploaded file in the filestore and returns a reference.
     * @throws IOException 
     */
    HashFileReference createHashReference(ByteSource in, String requestedStoreName) throws IOException;

}
