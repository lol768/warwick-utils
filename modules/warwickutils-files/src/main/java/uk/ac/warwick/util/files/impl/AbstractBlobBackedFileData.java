package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.options.GetOptions;
import org.joda.time.DateTime;
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

    private final BlobStore blobStore;

    private final String containerName;

    private final String blobName;

    protected final BlobBackedByteSource byteSource;

    protected AbstractBlobBackedFileData(BlobStore blobStore, String containerName, String blobName) {
        this.blobStore = blobStore;
        this.containerName = containerName;
        this.blobName = blobName;
        this.byteSource = new BlobBackedByteSource();
    }

    @Override
    public ByteSource asByteSource() {
        return byteSource;
    }

    @Override
    public long length() {
        return byteSource.size();
    }

    @Override
    public final boolean isExists() {
        return !byteSource.isEmpty();
    }

    @Override
    public final URI getFileLocation() {
        return blobStore.getContext().getSigner().signGetBlob(containerName, blobName, TEMPURL_EXPIRY_SECS).getEndpoint();
    }

    @Override
    public final boolean isFileBacked() {
        return false;
    }

    @Override
    public final boolean delete() {
        // Sometimes, files don't exist any more. The end result is still "the file doesn't exist", so just quit early.
        if (!isExists()) { return true; }

        blobStore.removeBlob(containerName, blobName);
        byteSource.invalidate();

        // This will have thrown an exception if the deletion failed
        return true;
    }

    @Override
    public final int hashCode() {
        return toString().hashCode();
    }

    @Override
    public final String toString() {
        return blobName;
    }

    protected class BlobBackedByteSource extends ByteSource {

        private final long offset;
        private final long length;

        private transient Blob blob;
        private transient long totalLength;
        private transient boolean payloadUsed;

        private BlobBackedByteSource() {
            this(-1, -1, -1);
        }

        private BlobBackedByteSource(long offset, long length, long totalLength) {
            this.offset = offset;
            this.length = length;
            this.totalLength = totalLength;
        }

        protected void invalidate() {
            blob = null;
            totalLength = -1;
            payloadUsed = false;
        }

        private synchronized void refresh() {
            final GetOptions options;
            if (offset >= 0 && length > 0) {
                options = GetOptions.Builder.range(offset, (offset + length) - 1);
            } else {
                options = GetOptions.NONE;
            }

            // The payload from getting the blob isn't repeatable by default, so we need to get a new blob every time
            blob = blobStore.getBlob(containerName, blobName, options);

            if (totalLength < 0) {
                if (blob == null) {
                    totalLength = -1;
                } else if (offset < 0 || length <= 0) {
                    totalLength = blob.getMetadata().getSize();
                } else {
                    totalLength = blobStore.blobMetadata(containerName, blobName).getSize();
                }
            }

            payloadUsed = false;
        }

        @Override
        public synchronized InputStream openStream() throws IOException {
            // The payload from getting the blob isn't repeatable by default, so we need to get a new blob every time
            if (blob == null || payloadUsed) {
                refresh();
            }

            payloadUsed = true;
            return blob.getPayload().openStream();
        }

        @Override
        public ByteSource slice(long offset, long length) {
            long actualOffset = offset;
            if (this.offset >= 0 && length > 0) {
                actualOffset = this.offset + offset;
            }

            if (totalLength < 0) {
                refresh();
            }

            return new BlobBackedByteSource(actualOffset, length, totalLength);
        }

        @Override
        public synchronized boolean isEmpty() {
            if (blob == null) refresh();

            return blob == null;
        }

        @Override
        public synchronized long size() {
            if (length > 0) {
                if (totalLength < 0) {
                    refresh();
                }

                if ((offset + length) > totalLength) {
                    return totalLength - offset;
                } else {
                    return length;
                }
            }

            if (blob == null) refresh();

            if (blob == null) {
                return 0;
            } else {
                return blob.getMetadata().getSize();
            }
        }

        public synchronized DateTime getLastModified() {
            if (blob == null) refresh();

            if (blob == null) {
                return null;
            } else {
                return new DateTime(blob.getMetadata().getLastModified().getTime());
            }
        }

        @Override
        public String toString() {
            return "BlobBackedByteSource.asByteSource(" + containerName + "/" + blobName + ")";
        }
    }

}
