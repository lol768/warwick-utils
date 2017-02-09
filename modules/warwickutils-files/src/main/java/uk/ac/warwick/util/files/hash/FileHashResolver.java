package uk.ac.warwick.util.files.hash;

import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface to lookup and resolve {@link HashFileReference}s. How the
 * implementation looks these up is not defined; they may be stored on the local
 * hard drive or stored remotely (with the details in a database). Either way, a
 * call to {@link #lookupByHash(HashFileStore, HashString, boolean)} will never return null, it will simply
 * return a {@link HashFileReference} where the
 * {@link HashFileReference#isExists()} method returns false.
 */
public interface FileHashResolver {

    /**
     * NEVER EVER CHANGE THIS VALUE!!! Changing this will *BREAK* existing references.
     */
    int SEPARATE_PATH_LIMIT = 10;

    // Name of the hash store where we store stuff by default 
    String STORE_NAME_DEFAULT = HashString.DEFAULT_STORE;

    // Name of the hash store where we store HTML
    String STORE_NAME_HTML = "html";

    /**
     * Returns true if and only if the hash exists in its backing store.
     */
    boolean exists(HashString hashString);
    
    /**
     * Lookup a {@link HashFileReference} by its hash, returning an empty one
     * (with {@link HashFileReference#isExists()} returning false) if the hash
     * doesn't exist.
     * <p>
     * Needs a {@link HashFileStore} to pass to the created reference.
     * @param storeNewHash
     */
    HashFileReference lookupByHash(HashFileStore store, HashString fileHash, boolean storeNewHash);

    /**
     * Generate a hash using the {@link FileHasher} supported by this resolver.
     * 
     * @throws IOException
     */
    HashString generateHash(InputStream is) throws IOException;

    /**
     * Remove a hash entirely when it has no more references 
     * @param reference Reference to this hash
     */
    void removeHash(HashFileReference reference);

}
