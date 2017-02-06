package uk.ac.warwick.util.files;

import uk.ac.warwick.util.files.hash.HashString;

import java.io.IOException;

/**
 * A reference to some binary data, as stored by {@link FileData}, which may or
 * may not be shared by other {@link FileReference}s.
 * <p>
 * File references can be identified by {@link #getPath()} and
 * {@link #getHash()}, of which at least one is guaranteed not to return null.
 *
 *
 */
public interface FileReference extends FileData<FileReference> {

    /**
     * The absolute virtual path that identifies this particular file reference.
     * For a binary file this is equivalent to the page URL, but like the URL is
     * a virtual resource and may not exist as a path on disk.
     * <p>
     * If for some reason you need the real path, {@link FileData#getFileLocation()}
     * is what you want. Avoid it unless you do really need it, though.
     */
    String getPath();

    /**
     * The hash that identifies this file reference.
     */
    HashString getHash();

    /**
     * Returns true if this reference is backed by a physical file, stored in a
     * physical point on the disk. This is the model for all "traditionally"
     * stored files in Sitebuilder.
     *
     * (Slightly confusing term, but local means it's NOT hash based, even though
     * most hash based referenced WILL store their data as a file on disk.)
     *
     * <p>
     * Iff this returns true, then {@link #toLocalReference()} will succeed.
     */
    boolean isLocal();

    /**
     * The equivalent of delete for regular files - the actual behaviour
     * will depend on the implementation. Local file references most likely
     * will delete the content. Hash references may simply do nothing and
     * leave cleanup to handle things.
     *
     * After calling this method, it makes sense to unset or reset the variable
     * holding it, or else you may be pointing at a nonexistent file.
     */
    void unlink();

    /**
     * Copy this {@link FileReference} to another. If the {@link FileReference}s
     * are of differing types, the behaviour is undefined.
     * <p>
     * Returns the target FileReference for chaining.
     */
    FileReference copyTo(final FileReference target) throws IOException;

    /**
     * This will throw an IllegalArgumentException if the FileReference is of
     * differing types or (e.g.) belongs to a different store.
     */
    FileReference renameTo(final FileReference target) throws IOException;

    /**
     * This is expected to throw an exception unless {@link #isLocal()}
     * returns true.
     */
    LocalFileReference toLocalReference();

    /**
     * This is expected to throw an exception unless {@link #isLocal()}
     * returns false.
     */
    HashFileReference toHashReference();
}
