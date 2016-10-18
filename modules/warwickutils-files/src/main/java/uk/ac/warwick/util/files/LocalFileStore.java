package uk.ac.warwick.util.files;

import com.google.common.io.ByteSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Stream;

public interface LocalFileStore extends FileStore {

    /**
     * Helper specialisation of {@link #get(Storeable)} which takes only a
     * file name, not a hash, and is guaranteed to return a local file reference
     * if one exists.
     * <p>
     * This is mainly used for returning source files.
     * <p>
     * May throw a {@link FileNotFoundException} if the {@link uk.ac.warwick.util.files.Storeable.StorageStrategy}
     * is set to throw an Exception when a reference is missing.
     */
    LocalFileReference getForPath(Storeable.StorageStrategy storageStrategy, String path) throws FileNotFoundException;

    /**
     * List filenames under a base path - this won't include the base path in the returned results
     */
    Stream<String> list(Storeable.StorageStrategy storageStrategy, String basePath);
    
    /**
     * Stores a new uploaded file in the filestore and returns a reference.
     */
    LocalFileReference storeLocalReference(Storeable storeable, ByteSource in) throws IOException;

    LocalFileReference copy(LocalFileReference fileReference, Storeable target) throws IOException;
    
    LocalFileReference rename(LocalFileReference fileReference, Storeable target) throws IOException;

}
