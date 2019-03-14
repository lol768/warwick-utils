package uk.ac.warwick.util.files.hash.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.common.io.ByteStreams;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.options.GetOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.impl.CachingBlobBackedHashFileReference;

import java.io.IOException;
import java.io.InputStream;

/**
 * A BlobBackedHashResolver that caches the content of responses it gets, up to a maximum byte size of the cache. Uses
 * Caffeine to do size-based eviction.
 *
 * This will eagerly read the contents of blobs fetched into the cache when they're fetched, which might not be what
 * you actually want!
 *
 * @link https://github.com/ben-manes/caffeine/wiki/Eviction
 */
public class CachingBlobStoreBackedHashResolver implements FileHashResolver, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingBlobStoreBackedHashResolver.class);

    private final BlobStoreBackedHashResolver delegate;

    // Initialised in afterPropertiesSet()
    private LoadingCache<String, Blob> cache;

    private long maximumSizeInBytes;

    public CachingBlobStoreBackedHashResolver(BlobStoreBackedHashResolver delegate) {
        this.delegate = delegate;
    }

    public void setMaximumSizeInBytes(long maximumSizeInBytes) {
        this.maximumSizeInBytes = maximumSizeInBytes;
    }

    public void setMaximumSizeAsPercentage(int percentage) {
        Assert.isTrue(percentage > 0 && percentage < 100, "Percentage must be > 0 and < 100");
        this.maximumSizeInBytes = (Runtime.getRuntime().maxMemory() * percentage) / 100;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.isTrue(maximumSizeInBytes > 0, "Maximum cache size should be set using either setMaximumSizeInBytes or setMaximumSizeAsPercentage");

        LOGGER.info(String.format("Initialising a Caffeine cache with a maximum size of %d bytes", maximumSizeInBytes));
        this.cache =
            Caffeine.newBuilder()
                .<String, Blob>weigher((k, b) -> {
                    long length = b.getMetadata().getContentMetadata().getContentLength();
                    if (length > Integer.MAX_VALUE) { // This will get rejected for caching anyway
                        return Integer.MAX_VALUE;
                    } else {
                        return (int) length;
                    }
                })
                .maximumWeight(maximumSizeInBytes)
                .recordStats()
                .build(key -> {
                    Blob blob = delegate.getBlobStore().getBlob(delegate.getContainerName(), key, GetOptions.NONE);

                    if (blob != null) {
                        // Eagerly read the content of the blob for the cache
                        try (InputStream in = blob.getPayload().openStream()) {
                            Blob cacheableBlob = delegate.getBlobStore().blobBuilder(key)
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

                            return cacheableBlob;
                        }
                    }

                    return null;
                });
    }

    @Override
    public boolean exists(HashString hashString) {
        if (cache.getIfPresent(hashString.getHash()) != null) {
            return true;
        }

        return delegate.exists(hashString);
    }

    @Override
    public HashFileReference lookupByHash(HashFileStore store, HashString fileHash, boolean storeNewHash) {
        return new CachingBlobBackedHashFileReference(delegate.lookupByHash(store, fileHash, storeNewHash), cache);
    }

    @Override
    public void removeHash(HashFileReference reference) {
        throw new UnsupportedOperationException("Refused to remove hash ");
    }

    @Override
    public HashString generateHash(InputStream is) throws IOException {
        return delegate.generateHash(is);
    }

    public long getCacheEstimatedSize() {
        return cache.estimatedSize();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public long getCacheCurrentSize() {
        return cache.policy().eviction().get().weightedSize().getAsLong();
    }

    public CacheStats getCacheStats() {
        return cache.stats();
    }

    Cache<String, Blob> getCache() {
        return cache;
    }
}
