package uk.ac.warwick.util.files.imageresize;

import uk.ac.warwick.util.files.FileReference;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

/**
 * Image resizer that uses a cache to reduce calls
 * to the _real_ resizer
 * 
 * Entries are stored as {cache root}/{original filename}@{width}x{height}
 * Entries are checked against the original file for stale-ness
 * No removal of cache entries is performed
 * No logging of access times/frequencies is done
 * If the cache can't be written to for some reason, it will just pass all requests 
 * through to the real resizer
 * 
 * @author cusaab
 *
 */
public final class CachingImageResizer implements FileExposingImageResizer {

    private final ImageResizer delegate;
    private final ScaledImageCache cache;
    
    public CachingImageResizer(final ImageResizer delegateResizer, final ScaledImageCache theCache) {
        this.delegate = delegateResizer;
        this.cache = theCache;
    }

    public CachingImageResizer(final ImageResizer delegateResizer, final File cacheDirectory) {
        this(delegateResizer, cacheDirectory, System.getProperty("warwick.imageResizer.separator", "@"));
    }
  
    public CachingImageResizer(final ImageResizer delegateResizer, final File cacheDirectory, final String cacheSeparator) {
        this(delegateResizer, new FileSystemScaledImageCache(cacheDirectory, cacheSeparator));
    }
    
    public void renderResized(final FileReference sourceFile, final Instant entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType) throws IOException {
        if (cache.contains(sourceFile, entityLastModified, maxWidth, maxHeight)){
            // there's the vague possibility that a cache file is deleted between checking cache.contains, and actually
            // serving it. But it doesn't seem worth the overhead of synchronizing to prevent this
            cache.serveFromCache(sourceFile, entityLastModified, out,maxWidth, maxHeight);
        } else {
            cache.cacheAndServe(sourceFile, entityLastModified, out, maxWidth,maxHeight, fileType, delegate);
        }
    }
    
    public File getResized(final FileReference sourceFile, final Instant entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType) throws IOException {
        if (cache instanceof FileExposingScaledImageCache) {
            if (!cache.contains(sourceFile, entityLastModified, maxWidth, maxHeight)){
                cache.createInCache(sourceFile, entityLastModified, maxWidth, maxHeight, fileType, delegate);
            }

            return ((FileExposingScaledImageCache)cache).getCacheFile(sourceFile, maxWidth, maxHeight);
        } else {
            throw new UnsupportedOperationException("Cache " + cache + " doesn't implement FileExposingScaledImageCache");
        }
    }
    
    public long getResizedImageLength(final FileReference sourceFile, final Instant entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType) {
        return cache.getFileSize(sourceFile, entityLastModified, maxWidth, maxHeight, fileType, delegate);
    }

    
}
