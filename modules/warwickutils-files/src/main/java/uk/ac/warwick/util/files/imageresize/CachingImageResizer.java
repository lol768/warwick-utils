package uk.ac.warwick.util.files.imageresize;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.util.core.spring.FileUtils;
import uk.ac.warwick.util.files.LocalFileReference;
import uk.ac.warwick.util.files.LocalFileStore;
import uk.ac.warwick.util.files.Storeable;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.*;
import java.time.ZonedDateTime;

/**
 * Image resizer that uses a FileStore cache to reduce calls
 * to the _real_ resizer
 *
 * Entries are stored as {cache container}/{original filename}@{width}x{height}
 * Entries are checked against the original file for stale-ness
 * No removal of cache entries is performed
 * No logging of access times/frequencies is done
 * If the cache can't be written to for some reason, it will just pass all requests 
 * through to the real resizer
 */
public final class CachingImageResizer implements ImageResizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingImageResizer.class);

    private final ImageResizer delegate;
    private final ImageCache cache;

    CachingImageResizer(final ImageResizer delegateResizer, final ImageCache theCache) {
        this.delegate = delegateResizer;
        this.cache = theCache;
    }

    public CachingImageResizer(final ImageResizer delegateResizer, final LocalFileStore fileStore, Storeable.StorageStrategy storageStrategy) {
        this(delegateResizer, new FileStoreScaledImageCache(fileStore, storageStrategy));
    }

    public void renderResized(final ByteSource source, final HashString hash, final ZonedDateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType) throws IOException {
        if (cache.contains(hash, entityLastModified, maxWidth, maxHeight)){
            // there's the vague possibility that a cache file is deleted between checking cache.contains, and actually
            // serving it. But it doesn't seem worth the overhead of synchronizing to prevent this
            cache.serveFromCache(hash, entityLastModified, out,maxWidth, maxHeight);
        } else {
            cache.cacheAndServe(source, hash, entityLastModified, out, maxWidth,maxHeight, fileType, delegate);
        }

    }

    public long getResizedImageLength(final ByteSource source, final HashString hash, final ZonedDateTime entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType) {
        return cache.getFileSize(source, hash, entityLastModified, maxWidth, maxHeight, fileType, delegate);
    }

    interface ImageCache {
        void cacheAndServe(final ByteSource source, final HashString hash, final ZonedDateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType, final ImageResizer resizer) throws IOException;
        void serveFromCache(final HashString hash, final ZonedDateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight) throws IOException;
        long getFileSize(final ByteSource source, final HashString hash, final ZonedDateTime entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType, final ImageResizer resizer);
        boolean contains(final HashString hash, final ZonedDateTime entityLastModified, final int maxWidth, final int maxHeight);
    }

    /**
     * This class does most/all of the hard work of maintaining the cache
     */
    static class FileStoreScaledImageCache implements ImageCache {

        private final LocalFileStore fileStore;

        private final Storeable.StorageStrategy storageStrategy;

        FileStoreScaledImageCache(LocalFileStore fileStore, Storeable.StorageStrategy storageStrategy) {
            this.fileStore = fileStore;
            this.storageStrategy = storageStrategy;
        }

        @Override
        public void cacheAndServe(ByteSource source, HashString hash, ZonedDateTime entityLastModified, OutputStream out, int maxWidth, int maxHeight, FileType fileType, ImageResizer resizer) throws IOException {
            try {
                createInCache(source, hash, entityLastModified, maxWidth, maxHeight, fileType, resizer);
                serveFromCache(hash, entityLastModified, out, maxWidth, maxHeight);
            } catch (IOException e) {
                LOGGER.error("Unable to update rendered image cache for " + getCacheFile(hash, maxWidth, maxHeight), e);
                // fall back to asking the delegate to render the file
                resizer.renderResized(source, hash, entityLastModified, out, maxWidth, maxHeight, fileType);
            }
        }

        @Override
        public void serveFromCache(HashString hash, ZonedDateTime entityLastModified, OutputStream out, int maxWidth, int maxHeight) throws IOException {
            getCacheFile(hash, maxWidth, maxHeight).asByteSource().copyTo(out);
        }

        @Override
        public long getFileSize(ByteSource source, HashString hash, ZonedDateTime entityLastModified, int maxWidth, int maxHeight, FileType fileType, ImageResizer resizer) {
            try {
                if (!contains(hash, entityLastModified, maxWidth, maxHeight)) {
                    createInCache(source, hash, entityLastModified, maxWidth, maxHeight, fileType, resizer);
                }

                return getCacheFile(hash, maxWidth, maxHeight).length();
            } catch (IOException e) {
                return -1L;
            }
        }

        @Override
        public boolean contains(HashString hash, ZonedDateTime entityLastModified, int maxWidth, int maxHeight) {
            LocalFileReference candidate = getCacheFile(hash, maxWidth, maxHeight);
            if (candidate.isExists()) {
                LOGGER.debug("Cache file " + candidate + " exists and is readable");
                if (entityLastModified.toInstant().isBefore(candidate.getLastModified())) {
                    LOGGER.debug("Cache file is not stale; returning cache hit" );
                    return true;
                }
            }
            return false;
        }

        private LocalFileReference getCacheFile(final HashString hash, final int maxWidth, final int maxHeight) {
            try {
                final String path = hash.toString() + "@" + maxWidth + "x" + maxHeight;

                return fileStore.getForPath(storageStrategy, path);
            } catch (FileNotFoundException e) {
                // This will never be thrown unless the storage strategy has MissingContentStrategy.Exception, which it doesn't
                throw new IllegalStateException(e);
            }
        }

        private void createInCache(final ByteSource source, final HashString hash, final ZonedDateTime entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType, final ImageResizer resizer) throws IOException {
            // Write the resized file to a temporary file first, then put it in the file reference and delete it
            File tempFile = File.createTempFile(hash.toString(), "@" + maxWidth + "x" + maxHeight);
            try {
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    resizer.renderResized(source, hash, entityLastModified, fos, maxWidth, maxHeight, fileType);
                }

                LocalFileReference newCacheEntry = getCacheFile(hash, maxWidth, maxHeight);
                newCacheEntry.overwrite(Files.asByteSource(tempFile));
            } finally {
                FileUtils.recursiveDelete(tempFile, false);
            }
        }
    }


}
