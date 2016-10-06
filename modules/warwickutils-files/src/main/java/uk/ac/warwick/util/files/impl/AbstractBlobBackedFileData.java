package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
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

    @Override
    public ByteSource asByteSource() {
        Blob blob = getBlobStore().getBlob(getContainerName(), getBlobName());

        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return blob.getPayload().openStream();
            }

            @Override
            public long size() throws IOException {
                if (blob == null) {
                    return 0;
                } else {
                    return blob.getMetadata().getSize();
                }
            }

            @Override
            public String toString() {
                return "AbstractBlobBackedFileData.asByteSource(" + getBlobName() + ")";
            }
        };
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
