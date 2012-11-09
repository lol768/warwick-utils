package uk.ac.warwick.util.files.hash.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import uk.ac.warwick.util.core.MaintenanceModeFlags;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.dao.HashInfoDAO;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.FileHasher;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.impl.HashBackedFileReference;

/**
 * Simple implementation of {@link FileHashResolver} that expects all hash
 * backed files to be stored locally on the disk in a predictable fashion, along
 * the lines of /var/storage/f/8/1/2/6/831789698fabc27372.data
 * 
 * Each hashResolver is given a directory for storing in and a name for this store.
 * The name is used when generating HashString values, and is used to qualify
 * the string. (The exception is when the storeName matches the name of the default
 * store - in this case the generated HashString is unqualified, so that they
 * are the same as existing hashes from before we added multiple stores.)
 */
public final class FileSystemBackedHashResolver implements FileHashResolver {
    
    /**
     * NEVER EVER CHANGE THIS VALUE!!! Changing this will *BREAK* existing references.
     */
    private static final int SEPARATE_PATH_LIMIT = 10;

    private final File storeLocation;
    private final String storeName;
    
    private final FileHasher hasher;
    
    private final HashInfoDAO dao;
    
    private final MaintenanceModeFlags flags;
    
    public FileSystemBackedHashResolver(FileHasher theHasher, String name, File location, HashInfoDAO sbd, MaintenanceModeFlags f) {
        this.hasher = theHasher;
        this.storeName = name;
        this.storeLocation = location;
        this.dao = sbd;
        this.flags = f;
    }

    public HashFileReference lookupByHash(HashFileStore store, HashString fileHash, boolean storeNewHash) {
        File file = resolve(fileHash, storeNewHash);
        return new HashBackedFileReference(store, file, fileHash);
    }
    
    /**
     * Partition a hash into a path.
     */
    public static String partition(String fileHash) {
        // Insert path separators for the first 5 sets of 2 characters
        StringBuilder path = new StringBuilder();
        
        String separator = FilenameUtils.separatorsToSystem("/");
        
        char[] hash = fileHash.toCharArray();
        for (int i = 0; i < hash.length; i++) {
            char ch = hash[i];
            
            if (i > 0 && (i % 2 == 0) && i <= SEPARATE_PATH_LIMIT) {
                path.append(separator);
            }
            
            path.append(ch);
        }
        
        path.append(".data");
        
        return path.toString();
    }
    
    /**
     * Given a hash string, resolves it to a point on the filesystem where this data
     * should be stored.
     * 
     * If the file doesn't already exist, it can also put a reference to this hash
     * in the database at this point, regardless of whether the file is subsequently
     * located.
     * 
     * @param storeNewHash 
     * 
     * @throws IllegalArgumentException if the HashString doesn't match the specified
     *      storeName of this resolver.
     */
    private File resolve(final HashString hashString, boolean storeNewHash) {
        if (!belongsToUs(hashString)) {
            throw new IllegalArgumentException("HashString name does not match resolver name");
        }
        
        // Remove all illegal characters from the hash, and lowercase it. Our
        // hasher implementation actually returns a hex string, so this isn't
        // strictly necessary.
        String fileHash = hashString.getHash().replaceAll("[^A-Za-z0-9_\\-\\.]", "_").toLowerCase();
        String path = partition(fileHash);
        
        File file = new File(storeLocation, FilenameUtils.separatorsToSystem(path));
        
        // New hash? Store it in the database.
        HashString safeHashString = new HashString(hashString.getStoreName(), fileHash);
        if (storeNewHash && dao.getHashByIdWithoutFlush(safeHashString.toString()) == null && !flags.isInMaintenanceMode()) {
            dao.hashCreated(safeHashString, file.length());
        }
        
        return file;
    }
    
    public HashString resolve (final File file, final String theStoreName ){
        String relativePath = storeLocation.toURI().relativize(file.toURI()).getPath();
        String convertedPath = relativePath.substring(0, relativePath.indexOf(".data"));
        String hash = convertedPath.replaceAll("/", "");
        HashString hashString = new HashString(theStoreName, hash);
        return hashString;
    }


    private boolean belongsToUs(HashString hashString) {
        hashString.getStoreName();
        return ((hashString.isDefaultStore() && STORE_NAME_DEFAULT.equals(storeName))
            || (storeName.equals(hashString.getStoreName())));
    }

    public HashString generateHash(InputStream is) throws IOException {
        // If the store name is the default, we leave the hash string as unqualified
        // (better than having some hashes starting "default/" and some not, when they
        // go to the same place).
        if (STORE_NAME_DEFAULT.equals(storeName)) {
            return new HashString(hasher.hash(is));
        }
        return new HashString(storeName, hasher.hash(is));
    }

    public void removeHash(HashFileReference reference) {
        dao.hashRemoved(reference.getHash());
        reference.delete();
    }


}
