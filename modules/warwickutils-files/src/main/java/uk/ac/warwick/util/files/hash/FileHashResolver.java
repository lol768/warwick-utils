package uk.ac.warwick.util.files.hash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;

/**
 * An interface to lookup and resolve {@link HashFileReference}s. How the
 * implementation looks these up is not defined; they may be stored on the local
 * hard drive or stored remotely (with the details in a database). Either way, a
 * call to {@link #lookupByHash(String)} will never return null, it will simply
 * return a {@link HashFileReference} where the
 * {@link HashFileReference#isExists()} method returns false.
 */
public interface FileHashResolver {

    // Name of the hash store where we store stuff by default 
    String STORE_NAME_DEFAULT = HashString.DEFAULT_STORE;
    // Name of the hash store where we store HTML
    String STORE_NAME_HTML = "html";
    
    /**
     * Lookup a {@link HashFileReference} by its hash, returning an empty one
     * (with {@link HashFileReference#isExists()} returning false) if the hash
     * doesn't exist.
     * <p>
     * Needs a {@link HashFileStore} to pass to the created reference.
     * @param storeNewHash
     */
    HashFileReference lookupByHash(HashFileStore store, HashString fileHash, boolean storeNewHash);
    
    HashString resolve (File file, String storeName );

    /**
     * Generate a hash using the {@link FileHasher} supported by this resolver.
     * 
     * @throws IOException
     */
    HashString generateHash(InputStream is) throws IOException;

//    /**
//     * Compares the list of known hashed files with the actual usages
//     * of hashed files, and returns the ones that aren't used.
//     * 
//     * It's VERY important that this checks all the places where
//     * hashes are referenced, otherwise the cleanup job will think
//     * hashes are unreferenced when they actually are.
//     */
//    Iterable<String> findUnreferencedHashes(HashFileStore store);

    /**
     * Remove a hash entirely when it has no more references 
     * @param reference Reference to this hash
     */
    void removeHash(HashFileReference reference);

}
