package uk.ac.warwick.util.files.hash.impl;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.springframework.beans.factory.InitializingBean;
import uk.ac.warwick.util.core.MaintenanceModeFlags;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.dao.HashInfoDAO;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.FileHasher;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.impl.BlobBackedHashFileReference;

import java.io.IOException;
import java.io.InputStream;

public class BlobStoreBackedHashResolver implements FileHashResolver, InitializingBean {

    private final BlobStore blobStore;

    private final String storeName;

    private final String containerName;

    private final FileHasher hasher;

    private final HashInfoDAO dao;

    private final MaintenanceModeFlags flags;

    public BlobStoreBackedHashResolver(BlobStoreContext context, String prefix, String name, FileHasher hasher, HashInfoDAO dao, MaintenanceModeFlags flags) {
        this.blobStore = context.getBlobStore();
        this.storeName = name;
        this.containerName = prefix + name;
        this.hasher = hasher;
        this.dao = dao;
        this.flags = flags;
    }

    public boolean exists(HashString hashString) {
        if (!belongsToUs(hashString)) {
            throw new IllegalArgumentException("HashString name does not match resolver name");
        }

        // New hash? Store it in the database.
        return blobStore.blobExists(containerName, hashString.getHash());
    }

    @Override
    public HashFileReference lookupByHash(HashFileStore store, HashString hashString, boolean storeNewHash) {
        if (!belongsToUs(hashString)) {
            throw new IllegalArgumentException("HashString name does not match resolver name");
        }

        // New hash? Store it in the database.
        HashString safeHashString = new HashString(hashString.getStoreName(), hashString.getHash());
        if (storeNewHash && dao.getHashByIdWithoutFlush(safeHashString.toString()) == null && !flags.isInMaintenanceMode()) {
            BlobMetadata metadata = blobStore.blobMetadata(containerName, hashString.getHash());
            dao.hashCreated(safeHashString, metadata == null ? 0L : metadata.getSize());
        }

        return new BlobBackedHashFileReference(store, blobStore, containerName, hashString);
    }

    private boolean belongsToUs(HashString hashString) {
        return ((hashString.isDefaultStore() && STORE_NAME_DEFAULT.equals(storeName))
            || (storeName.equals(hashString.getStoreName())));
    }

    @Override
    public HashString generateHash(InputStream is) throws IOException {
        // If the store name is the default, we leave the hash string as unqualified
        // (better than having some hashes starting "default/" and some not, when they
        // go to the same place).
        if (STORE_NAME_DEFAULT.equals(storeName)) {
            return new HashString(hasher.hash(is));
        }

        return new HashString(storeName, hasher.hash(is));
    }

    @Override
    public void removeHash(HashFileReference reference) {
        dao.hashRemoved(reference.getHash());
        reference.delete();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Create the container if it doesn't exist
        if (!blobStore.containerExists(containerName))
            blobStore.createContainerInLocation(null, containerName);
    }
}
