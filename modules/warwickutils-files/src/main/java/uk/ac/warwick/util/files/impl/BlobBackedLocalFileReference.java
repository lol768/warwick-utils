package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.apache.commons.io.FilenameUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.options.CopyOptions;
import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.LocalFileReference;
import uk.ac.warwick.util.files.Storeable.StorageStrategy;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.IOException;
import java.time.Instant;

/**
 * A type of reference that backs on to a single blob, and doesn't offer
 * any sort of data sharing or anything. There should only be a single
 * reference to a particular blob - it should behave basically the
 * same as regular file access.
 * <p>
 * Most of the time you shouldn't need to create one of these directly -
 * they will be created from FileStore. However, there are a few places
 * that require a FileReference and all you have is a File - in that case,
 * you can create one of these with a null FileStore, being aware that
 * some operations (copy) will not work.
 */
public final class BlobBackedLocalFileReference extends AbstractFileReference implements LocalFileReference {

    private final BlobStore blobStore;
    private final String containerName;

    private final Data data;
    private final String path;

    private final BlobStoreFileStore fileStore;
    private final StorageStrategy storageStrategy;

    /**
     * @param store FileStore required for certain operations. In some cases this can be null.
     * @param thepath The URL path to identify the file by.
     */
    public BlobBackedLocalFileReference(BlobStoreFileStore store, BlobStore blobStore, String containerName, String thepath, StorageStrategy theStorageStrategy) {
        this.fileStore = store;
        this.blobStore = blobStore;
        this.containerName = containerName;
        this.path = FilenameUtils.separatorsToUnix(thepath);
        this.data = new Data();
        this.storageStrategy = theStorageStrategy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FileData<FileReference> getData() {
        return data;
    }

    @Override
    public String getFileName() {
        return uk.ac.warwick.util.core.spring.FileUtils.getFileName(path);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public HashString getHash() {
        return null;
    }

    @Override
    public LocalFileReference copyTo(FileReference target) throws IOException {
        return copyTo(target.toLocalReference().getPath());
    }

    @Override
    public LocalFileReference copyTo(String newPath) throws IOException {
        fileStore.statistics.timeSafe(() -> blobStore.copyBlob(containerName, path, containerName, newPath, CopyOptions.NONE), fileStore.statistics::referenceWritten);
        return new BlobBackedLocalFileReference(fileStore, blobStore, containerName, newPath, storageStrategy);
    }

    @Override
    public LocalFileReference renameTo(FileReference target) throws IOException {
        return renameTo(target.toLocalReference().getPath());
    }

    @Override
    public LocalFileReference renameTo(String newPath) throws IOException {
        LocalFileReference renamed = copyTo(newPath);
        unlink();
        return renamed;
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public Instant getLastModified() {
        return data.getLastModified();
    }

    @Override
    public StorageStrategy getStorageStrategy() {
        return storageStrategy;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return getPath() + " (" + data.toString() + ")";
    }

    class Data extends AbstractBlobBackedFileData {

        private Data() {
            super(fileStore, blobStore, containerName, path);
        }

        @Override
        public LocalFileReference overwrite(ByteSource in) throws IOException {
            LocalFileReference thisReference = BlobBackedLocalFileReference.this;
            fileStore.doStore(in, path, containerName, thisReference);
            byteSource.invalidate();
            return thisReference;
        }

        Instant getLastModified() {
            return byteSource.getLastModified();
        }

    }

    @Override
    public void unlink() {
        getData().delete();
    }

}
