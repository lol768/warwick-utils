package uk.ac.warwick.util.files.hash;

import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * FileHashResolver which contains a map of other hash resolvers. The key
 * is the store name in the hash string. When looking up a hash string it will
 * direct it to the correct file resolver to do the actual lookup.
 */
public final class CompositeFileHashResolver implements FileHashResolver {

    private final Map<String, FileHashResolver> resolvers;
    private final FileHashResolver defaultResolver;
    
    public CompositeFileHashResolver(Map<String, FileHashResolver> resolvers2) {
        this.resolvers = resolvers2;
        this.defaultResolver = this.resolvers.get(FileHashResolver.STORE_NAME_DEFAULT);
    }

    @Override
    public boolean exists(HashString fileHash) {
        if (fileHash.isDefaultStore()) {
            return defaultResolver.exists(fileHash);
        } else {
            FileHashResolver fileHashResolver = resolvers.get(fileHash.getStoreName());
            if (fileHashResolver == null) {
                throw new IllegalArgumentException("No hash resolver recognised hash " + fileHash.toString());
            }
            return fileHashResolver.exists(fileHash);
        }
    }

    public HashString generateHash(InputStream is) throws IOException {
        // Don't currently care who generates a hash so just use the default one
        return defaultResolver.generateHash(is);
    }

    public HashFileReference lookupByHash(HashFileStore store, HashString fileHash, boolean storeNewHash) {
        if (fileHash.isDefaultStore()) {
            return defaultResolver.lookupByHash(store, fileHash, storeNewHash);
        } else {
            FileHashResolver fileHashResolver = resolvers.get(fileHash.getStoreName());
            if (fileHashResolver == null) {
                throw new IllegalArgumentException("No hash resolver recognised hash " + fileHash.toString());
            }
            return fileHashResolver.lookupByHash(store, fileHash, storeNewHash);
        }
    }

    public void removeHash(HashFileReference reference) {
        // Again, it doesn't currently matter which store the reference is from - we
        // only keep a single database table of hash strings, and the reference knows
        // where its data is in order to delete it, so just use the default.
        this.defaultResolver.removeHash(reference);
    }

}
