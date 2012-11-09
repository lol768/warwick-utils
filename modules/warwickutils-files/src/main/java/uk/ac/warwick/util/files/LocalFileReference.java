package uk.ac.warwick.util.files;

import java.io.File;
import java.io.IOException;

import org.joda.time.DateTime;

import uk.ac.warwick.util.files.Storeable.StorageStrategy;
import uk.ac.warwick.util.files.impl.HashBackedFileReference;

/**
 * A file reference backed by local file system data.
 * <p>
 * This is the model for traditional file storage in Sitebuilder.
 */
public interface LocalFileReference extends FileReference {
    
    /**
     * Just the last part of the path, ie the name of the file without
     * the directories it is in.
     */
    String getFileName();

    /**
     * The absolute virtual path that identifies this particular file reference.
     * For a binary file this is equivalent to the page URL, but like the URL
     * is a virtual resource and may not exist as a path on disk.
     * <p>
     * If for some reason you need the real path, {@link FileData#getRealPath()} is
     * what you want. Avoid it unless you do really need it, though.
     */
    String getPath();
    
    File getFile();

    /**
     * This method should only be called on {@link LocalFileReference}s and
     * should definitely <strong>not</strong> exist on
     * {@link HashBackedFileReference} unless we store each hash reference in
     * the database by ID pointing from the content fetcher to the hash. This
     * used to delegate to the {@link FileData} but this is unsafe as a way of
     * determining the last modified date of hash references. See SBTWO-3630
     */
    DateTime getLastModified();
    
    /**
     * Copy this file reference to the specified local path.
     * <p>
     * Will return a new file reference pointing at the specified path.
     */
    LocalFileReference copyTo(final String newPath) throws IOException;

    /**
     * Rename this file reference to the new path.
     */
    LocalFileReference renameTo(final String newPath) throws IOException;
    
    StorageStrategy getStorageStrategy();

}
