package uk.ac.warwick.util.files.impl;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.io.ByteSource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jclouds.blobstore.domain.Blob;
import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.IOException;

public class CachingBlobBackedHashFileReference extends AbstractFileReference implements HashFileReference {

    private final BlobBackedHashFileReference delegate;
    private final LoadingCache<String, Blob> cache;

    public CachingBlobBackedHashFileReference(BlobBackedHashFileReference delegate, LoadingCache<String, Blob> cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FileData<FileReference> getData() {
        return new CachedData(delegate.getData());
    }

    class CachedData extends AbstractBlobBackedFileData {

        private final BlobBackedHashFileReference.Data delegate;

        private CachedData(BlobBackedHashFileReference.Data data) {
            super(data.getStatistics(), data.getBlobStore(), data.getContainerName(), data.getBlobName());
            this.delegate = data;
        }

        @Override
        public HashFileReference overwrite(ByteSource in) throws IOException {
            return delegate.overwrite(in);
        }

        @Override
        public BlobBackedByteSource asByteSource() {
            return new CachedBlobBackedByteSource();
        }

        @Override
        public boolean delete() {
            throw new UnsupportedOperationException("Can't delete FileData from CachingBlobBackedHashFileReference");
        }

        private class CachedBlobBackedByteSource extends BlobBackedByteSource {

            private CachedBlobBackedByteSource() {
                super();
            }

            @Override
            synchronized void refresh() {
                blob = cache.get(getBlobName());
                totalLength = blob == null ? -1 : blob.getMetadata().getSize();
                payloadUsed = false;
            }

            @Override
            public String toString() {
                return "CachingBlobBackedHashFileReference.asByteSource(" + super.toString() + ")";
            }
        }

    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public HashString getHash() {
        return delegate.getHash();
    }

    @Override
    public boolean isLocal() {
        return delegate.isLocal();
    }

    @Override
    public void unlink() {
        // Do nothing
    }

    @Override
    public FileReference copyTo(FileReference target) {
        return new CachingBlobBackedHashFileReference(delegate.copyTo(target), cache);
    }

    @Override
    public FileReference renameTo(FileReference target) {
        return new CachingBlobBackedHashFileReference(delegate.renameTo(target), cache);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("delegate", delegate)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CachingBlobBackedHashFileReference that = (CachingBlobBackedHashFileReference) o;

        return new EqualsBuilder()
                .append(delegate, that.delegate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(delegate)
                .toHashCode();
    }
}
