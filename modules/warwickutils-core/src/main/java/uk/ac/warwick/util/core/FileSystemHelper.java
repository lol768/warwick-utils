package uk.ac.warwick.util.core;

import java.io.File;
import java.io.IOException;

/**
 * Facade for manipulating the file system.
 * 
 * @author xusqac
 */
public interface FileSystemHelper {
    /**
     * Move the source to the target. The target cannot exist.
     * 
     * @param source
     *            May be a file or directory and must exist.
     * @param target
     *            May be a file or directory and must not exist.
     * @return true if the operation succeeded.
     */
    boolean move(final File source, final File target) throws IOException;

    /**
     * Copy the specified file/directory into the target file/directory. Source
     * must exist and can be either a file or directory. Target cannot not exist
     * and must be of the same type as the source.
     */
    void recursiveCopy(final File source, final File target) throws IOException;

    /**
     * Copy the specified file/directory into the target file/directory. Source
     * must exist and can be either a file or directory. Target cannot exist and
     * must be of the same type as the source.
     */
    void copy(final File source, final File target) throws IOException;

    /**
     * Source may or may not exist. Target may or may not exist.
     * 
     * @return true if the target was created, false if it already exists.
     */
    void optionalCopy(final File source, final File target) throws IOException;

    /**
     * Delete the specified file. Thows exception if the file does not exist
     */
    void delete(final File target) throws IOException;

    /**
     * Delete the specified file if it exists, else do nothing
     */
    void deleteIfExists(final File target) throws IOException;

    /**
     * Delete the specified directory if it exists and it has no subfiles or
     * directories. If recurse is set to true, try to do the same for the parent
     * directory.
     */
    void deleteIfEmpty(final File target, final boolean recurse) throws IOException;
}
