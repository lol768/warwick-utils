package uk.ac.warwick.util.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.InputStreamSource;

import uk.ac.warwick.util.files.FileStore.UsingOutput;
import uk.ac.warwick.util.files.hash.HashString;

/**
 * The data portion of a file. This doesn't have a filename or
 * any name to speak of, because it can be shared by multiple
 * pages that have different paths. Generally it will be referenced
 * by multiple FileReferences, which can each store their own
 * path and point to the same FileData.
 */
public interface FileData {
    /**
     * Read data from the file via a given InputStream.
     */
    InputStream getInputStream() throws IOException;
    
    /**
     * Erase the original data and replace it with the data
     * written to the OutputStream given in the callback.
     * Handles opening and closing around the callback.
     * 
     * Depending on the implementation, this may result in a
     * copy of the underlying data being made (for example
     * if the data is shared with other pages).
     * 
     * TODO Perhaps this should return a new FileData instead, since it
     *    is new data and we want to try to treat FileData as semi-immutable
     *    once it's been created.
     * 
     * @return the new hash to identify this file (if the file store
     * uses hashes). If the return value is not null it should be
     * updated in the content fetcher, because you'll need it
     * to find the data again.
     */
    HashString overwrite(UsingOutput callback) throws IOException; 

    /**
     * See {@link #overwrite(UsingOutput)}
     */
    HashString overwrite(File file) throws IOException;

    /**
     * See {@link #overwrite(UsingOutput)}
     */
    HashString overwrite(byte[] contents) throws IOException;
    
    /**
     * See {@link #overwrite(byte[])}
     */
    HashString overwrite(String contents) throws IOException;
    
    /**
     * The length of the data in bytes.
     */
    long length();
    
    boolean isExists();
    
    /**
     * Only iff this returns true can you call  {@link #getRealFile()} and {@link #getRealPath()}
     * 
     * @return whether this data is stored somewhere as a file on the filesystem.
     */
    boolean isFileBacked();
    
    /**
     * If {@link #isFileBacked()}, this will return the absolute path to the
     * actual file holding the data. You MUST NOT try to OPEN or WRITE to
     * this file, as it may be shared between many pages. Use the overwrite
     * methods to write data.
     * <p>
     * I didn't want to add this method, but it is necessary in some places such
     * as SendFile support, where we do need to pass the real path over to 
     * Apache to serve the file.
     * 
     * @return the actual absolute file path IFF this is backed by a file.
     * @throws UnsupportedOperationException
     */
    String getRealPath();
    
    /**
     * @throws UnsupportedOperationException if there is no actual File.
     */
    File getRealFile();

    /**
     * Delete the underlying data. May throw an UnsupportedOperationException.
     * 
     * Note that if the implementation of FileData is a shared one then this
     * will delete the data for <em>everything</em> that referenes it.
     */
    boolean delete();

    InputStreamSource getInputStreamSource();

}
