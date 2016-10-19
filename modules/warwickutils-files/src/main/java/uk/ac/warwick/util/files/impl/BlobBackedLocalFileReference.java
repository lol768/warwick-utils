package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.apache.commons.io.FilenameUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.options.CopyOptions;
import org.joda.time.DateTime;
import uk.ac.warwick.util.files.*;
import uk.ac.warwick.util.files.Storeable.StorageStrategy;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.IOException;

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

    public FileData getData() {
        return data;
    }

    public String getFileName() {
        return uk.ac.warwick.util.core.spring.FileUtils.getFileName(path);
    }

    public String getPath() {
        return path;
    }
    
    public HashString getHash() {
        return null;
    }
    
    public LocalFileReference copyTo(FileReference target) throws IOException {
        return copyTo(target.toLocalReference().getPath());
    }
    
    public LocalFileReference copyTo(String newPath) throws IOException {
        blobStore.copyBlob(containerName, path, containerName, newPath, CopyOptions.NONE);
        return new BlobBackedLocalFileReference(fileStore, blobStore, containerName, newPath, storageStrategy);
    }
    
    public LocalFileReference renameTo(FileReference target) throws IOException {
        return renameTo(target.toLocalReference().getPath());
    }
    
    public LocalFileReference renameTo(String newPath) throws IOException {
        LocalFileReference renamed = copyTo(newPath);
        unlink();
        return renamed;
    }

    public boolean isLocal() {
        return true;
    }

    public DateTime getLastModified() {
        return data.getLastModified();
    }
    
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

        @Override
        public BlobStore getBlobStore() {
            return blobStore;
        }

        @Override
        public String getBlobName() {
            return path;
        }

        @Override
        public String getContainerName() {
            return containerName;
        }

        @Override
        public FileData overwrite(ByteSource in) throws IOException {
            fileStore.doStore(in, path, containerName, BlobBackedLocalFileReference.this);
            return this;
        }

        DateTime getLastModified() {
            Blob blob = blobStore.getBlob(containerName, path);
            return blob == null ? null : new DateTime(blob.getMetadata().getLastModified().getTime());
        }
        
    }
    
    private static class FileStoreable implements Storeable {
        
        private final String path;
        private final StorageStrategy strategy;
        
        FileStoreable(StorageStrategy theStrategy, String thePath) {
            this.strategy = theStrategy;
            this.path = thePath;
        }

        public StorageStrategy getStrategy() {
            return strategy;
        }

        public String getPath() {
            return path;
        }
        
        public HashString getHash() {
            return null;
        }
        
    }

    public void unlink() {
        getData().delete();
    }    

}