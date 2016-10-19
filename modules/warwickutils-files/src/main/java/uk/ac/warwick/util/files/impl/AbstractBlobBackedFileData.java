package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.options.GetOptions;
import uk.ac.warwick.util.files.FileData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * FileData that is stored on externally in an object store. This doesn't make any
 * assertions about whether the data is stored under a URL hierarchy or
 * stored under a hash-based filename.
 */
public abstract class AbstractBlobBackedFileData implements FileData {

    private static final long TEMPURL_EXPIRY_SECS = 60 * 60; // One hour

    private static class BlobBackedByteSource extends ByteSource {

        private final BlobStore blobStore;
        private final String containerName;
        private final String blobName;
        private final long offset;
        private final long length;

        private BlobBackedByteSource(BlobStore blobStore, String containerName, String blobName, GetOptions options) {
            this(blobStore, containerName, blobName, -1, -1);
        }

        private BlobBackedByteSource(BlobStore blobStore, String containerName, String blobName, long offset, long length) {
            this.blobStore = blobStore;
            this.containerName = containerName;
            this.blobName = blobName;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public InputStream openStream() throws IOException {
            final GetOptions options;
            if (offset >= 0 && length > 0) {
                options = GetOptions.Builder.range(offset, (offset + length) - 1);
            } else {
                options = GetOptions.NONE;
            }

            // The payload from getting the blob isn't repeatable by default, so we need to get a new blob every time
            return blobStore.getBlob(containerName, blobName, options).getPayload().openStream();
        }

        @Override
        public ByteSource slice(long offset, long length) {
            long actualOffset = offset;
            if (this.offset >= 0 && length > 0) {
                actualOffset = this.offset + offset;
            }

            return new BlobBackedByteSource(blobStore, containerName, blobName, actualOffset, length);
        }

        @Override
        public boolean isEmpty() throws IOException {
            return blobStore.blobExists(containerName, blobName);
        }

        @Override
        public long size() throws IOException {
            if (length > 0) return length;

            BlobMetadata metadata = blobStore.blobMetadata(containerName, blobName);

            if (metadata == null) {
                return 0;
            } else {
                return metadata.getSize();
            }
        }

        @Override
        public String toString() {
            return "BlobBackedByteSource.asByteSource(" + containerName + "/" + blobName + ")";
        }
    }

    @Override
    public ByteSource asByteSource() {
        return new BlobBackedByteSource(getBlobStore(), getContainerName(), getBlobName(), GetOptions.NONE);
    }

    @Override
    public long length() {
        BlobMetadata metadata = getBlobStore().blobMetadata(getContainerName(), getBlobName());
        return metadata != null ? metadata.getSize() : 0L;
    }

    @Override
    public final boolean isExists() {
        return getBlobStore().blobExists(getContainerName(), getBlobName());
    }

    @Override
    public final URI getFileLocation() {
        return getBlobStore().getContext().getSigner().signGetBlob(getContainerName(), getBlobName(), TEMPURL_EXPIRY_SECS).getEndpoint();
    }

    @Override
    public final boolean isFileBacked() {
        return false;
    }

    @Override
    public final boolean delete() {
        // Sometimes, files don't exist any more. The end result is still "the file doesn't exist", so just quit early.
        if (!isExists()) { return true; }

        getBlobStore().removeBlob(getContainerName(), getBlobName());

        // This will have thrown an exception if the deletion failed
        return true;
    }

    public abstract BlobStore getBlobStore();

    public abstract String getBlobName();

    public abstract String getContainerName();

    @Override
    public final int hashCode() {
        return toString().hashCode();
    }

    @Override
    public final String toString() {
        return getBlobName();
    }

}
