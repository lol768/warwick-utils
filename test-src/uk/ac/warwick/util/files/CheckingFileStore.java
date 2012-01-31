/**
 * 
 */
package uk.ac.warwick.util.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.impl.FileBackedFileReference;
import uk.ac.warwick.util.files.impl.HashBackedFileReference;

class CheckingFileStore implements HashFileStore {
    
    private final File file;
    
    boolean created;
    
    public CheckingFileStore(File theFile) {
        this.file = theFile;
    }

    public HashFileReference createHashReference(UsingOutput callback, String storeName) throws IOException {
        // must call the callback so that it can close streams it is holding.
        callback.doWith(new ByteArrayOutputStream());
        created = true;
        return new HashBackedFileReference(null, file, new HashString(storeName, "abcdef"));
    }

    public HashFileReference storeHashReference(UploadedFileDetails uploadedFile, String storeName) throws IOException {
        created = true;
        return new HashBackedFileReference(null, file, new HashString(storeName, "abcdef"));
    }

    public FileReference get(Storeable storeable) throws FileNotFoundException {
        return new FileBackedFileReference(null, file, storeable.getPath(), storeable.getStrategy());
    }

    public LocalFileReference getForPath(Storeable storeable, String path) throws FileNotFoundException {
        return null;
    }

    public FileReference store(Storeable storeable, String storeName, UploadedFileDetails uploadedFile) throws IOException {
        return null;
    }

    public LocalFileReference store(Storeable storeable, String storeName, UsingOutput delegate) throws IOException {
        return null;
    }
    
}