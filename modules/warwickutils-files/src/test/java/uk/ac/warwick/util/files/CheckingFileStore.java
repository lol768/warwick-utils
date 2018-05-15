/**
 * 
 */
package uk.ac.warwick.util.files;

import com.google.common.io.ByteSource;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.impl.FileBackedHashFileReference;
import uk.ac.warwick.util.files.impl.FileBackedLocalFileReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

class CheckingFileStore implements HashFileStore {
    
    private final File file;
    
    boolean created;
    
    public CheckingFileStore(File theFile) {
        this.file = theFile;
    }

    @Override
    public HashFileReference createHashReference(ByteSource in, String storeName) throws IOException {
        created = true;
        return new FileBackedHashFileReference(null, file, new HashString(storeName, "abcdef"));
    }

    @Override
    public HashFileReference storeHashReference(ByteSource in, String storeName) throws IOException {
        created = true;
        return new FileBackedHashFileReference(null, file, new HashString(storeName, "abcdef"));
    }

    @Override
    public FileReference get(Storeable storeable) throws FileNotFoundException {
        return new FileBackedLocalFileReference(null, file, storeable.getPath(), storeable.getStrategy());
    }

    @Override
    public FileReference store(Storeable storeable, String storeName, ByteSource in) throws IOException {
        return null;
    }

    @Override
    public FileStoreStatistics getStatistics() {
        return new DefaultFileStoreStatistics(this);
    }
    
}