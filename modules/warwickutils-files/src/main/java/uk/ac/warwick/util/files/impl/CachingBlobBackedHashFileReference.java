package uk.ac.warwick.util.files.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSource;
import com.google.common.io.Closer;
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
import java.io.InputStream;
import java.nio.charset.Charset;

public class CachingBlobBackedHashFileReference extends AbstractFileReference implements HashFileReference {

    private final BlobBackedHashFileReference delegate;
    private final Cache<String, Blob> cache;

    public CachingBlobBackedHashFileReference(BlobBackedHashFileReference delegate, Cache<String, Blob> cache) {
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
                Blob cached = cache.getIfPresent(getBlobName());
                if (cached != null) {
                    blob = cached;
                } else {
                    super.refresh();

                    // Check that our blob is under the maximum size of an integer or we won't be able to stream to a byte[]
                    if (blob != null && blob.getMetadata().getContentMetadata().getContentLength() < Integer.MAX_VALUE) {
                        // Eagerly read the content of the blob for the cache
                        try {
                            Closer closer = Closer.create();
                            try {
                                InputStream in = closer.register(blob.getPayload().openStream());
                                Blob cacheableBlob = getBlobStore().blobBuilder(getBlobName())
                                        .payload(ByteStreams.toByteArray(in))
                                        .contentDisposition(blob.getMetadata().getContentMetadata().getContentDisposition())
                                        .contentLength(blob.getMetadata().getContentMetadata().getContentLength())
                                        .contentType(blob.getMetadata().getContentMetadata().getContentType())
                                        .contentEncoding(blob.getMetadata().getContentMetadata().getContentEncoding())
                                        .contentLanguage(blob.getMetadata().getContentMetadata().getContentLanguage())
                                        .contentMD5(blob.getMetadata().getContentMetadata().getContentMD5AsHashCode())
                                        .cacheControl(blob.getMetadata().getContentMetadata().getCacheControl())
                                        .expires(blob.getMetadata().getContentMetadata().getExpires())
                                        .userMetadata(blob.getMetadata().getUserMetadata())
                                        .build();
                                cacheableBlob.getMetadata().setSize(blob.getMetadata().getSize());

                                cache.put(getBlobName(), cacheableBlob);
                            } finally {
                                closer.close();
                            }
                        } catch (IOException e) {
                            cache.invalidate(getBlobName());
                        }
                    }
                }

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
