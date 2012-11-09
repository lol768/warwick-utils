package uk.ac.warwick.util.files;

import java.io.File;
import java.io.InputStream;

public interface UploadedFileDetails {
    
    long getFileSize();
    
    File getFile();

    /**
     * Creates a new InputStream for the contents of this uploaded file.
     */
    InputStream getContents();

}
