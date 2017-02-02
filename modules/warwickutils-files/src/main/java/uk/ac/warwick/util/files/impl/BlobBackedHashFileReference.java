package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.jclouds.blobstore.BlobStore;
import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.IOException;

public final class BlobBackedHashFileReference extends AbstractFileReference implements HashFileReference {

    private final HashFileStore fileStore;

    private BlobStore blobStore;
    private String containerName;

    private HashString hash;
    private transient FileData data;

    public BlobBackedHashFileReference(final HashFileStore store, final BlobStore blobStore, final String containerName, final HashString theHash) {
        this.fileStore = store;
        this.blobStore = blobStore;
        this.containerName = containerName;
        update(theHash);
    }

    public HashString getHash() {
        return hash; // he'll save every one of us
    }

    public String getPath() {
        return null;
    }

    private void update(HashString theHash) {
        this.hash = theHash;
        this.data = new Data(blobStore, containerName, theHash.getHash());
    }

    public HashFileReference copyTo(FileReference target) {
        return new BlobBackedHashFileReference(fileStore, blobStore, containerName, hash);
    }

    public HashFileReference renameTo(FileReference target) {
        return this;
    }

    public FileData getData() {
        return data;
    }

    public boolean isLocal() {
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return hash + " (" + data.toString() + ")";
    }

    class Data extends AbstractBlobBackedFileData {

        private Data(BlobStore blobStore, String containerName, String blobName) {
            super(blobStore, containerName, blobName);
        }

        @Override
        public FileReference overwrite(ByteSource in) throws IOException {
            // Create a new file, storing it separately, and return the new hash
            HashFileReference newReference = fileStore.createHashReference(in, getHash().getStoreName());
            update(newReference.getHash());

            return newReference;
        }

    }

    public void unlink() {
        // Do nothing - leave file data in place, and let the periodic
        // cleanup get rid of the data as necessary.
    }

}
