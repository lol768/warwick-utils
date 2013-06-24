package uk.ac.warwick.util.files.imageresize;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.hash.impl.FileSystemBackedHashResolver;


/**
 * Image resizer that uses an on-disk cache to reduce calls
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
public final class CachingImageResizer implements ImageResizer {
    private static final Logger LOGGER = Logger.getLogger(CachingImageResizer.class);

    private final ImageResizer delegate;
    private final ImageCache cache;
    
    public CachingImageResizer(final ImageResizer delegateResizer, final ImageCache theCache) {
        this.delegate = delegateResizer;
        this.cache = theCache;
    }
  
    public CachingImageResizer(final ImageResizer delegateResizer, final File cacheDirectory) {
        this(delegateResizer, new FileSystemScaledImageCache(cacheDirectory));
    }
    
    public void renderResized(final FileReference sourceFile, final DateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType) throws IOException {
        if (cache.contains(sourceFile, entityLastModified, maxWidth, maxHeight)){
            // there's the vague possibility that a cache file is deleted between checking cache.contains, and actually
            // serving it. But it doesn't seem worth the overhead of synchronizing to prevent this
            cache.serveFromCache(sourceFile, entityLastModified, out,maxWidth, maxHeight);
        } else {
            cache.cacheAndServe(sourceFile, entityLastModified, out, maxWidth,maxHeight, fileType, delegate);
        }
        
    }
    
    public long getResizedImageLength(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType) {
        return cache.getFileSize(sourceFile, entityLastModified, maxWidth, maxHeight, fileType, delegate);
    }
    
    interface ImageCache {
        void cacheAndServe(final FileReference sourceFile, final DateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType, final ImageResizer resizer) throws IOException;
        void serveFromCache(final FileReference sourceFile, final DateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight) throws IOException;
        long getFileSize(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType, final ImageResizer resizer);
        boolean contains(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight);
    }

    /**
     * This class does most/all of the hard work of maintaining the cache
     * 
     * @author cusaab
     *
     */
    static class FileSystemScaledImageCache implements ImageCache {
        private File cacheRoot;

        public FileSystemScaledImageCache(final File cacheDir) {
            this.cacheRoot = cacheDir;
        }

        /**
         * Serve the specified file, at the specified resolution, from the cache
         * 
         * n.b. This method assumes that the file actually exists (i.e. that cache.contains() 
         *      is true for this file/resolution combination)
         * It will die with an IOException if it doesn't
         */
        public void serveFromCache(final FileReference sourceFile, final DateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight) throws IOException {
            FileInputStream fis = new FileInputStream(getCacheFile(sourceFile, maxWidth, maxHeight));
            try {
                FileCopyUtils.copy(fis, out);
            } finally {
                fis.close();
            }
        }
        
        public long getFileSize(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType, final ImageResizer resizer) {
            if (!contains(sourceFile, entityLastModified, maxWidth, maxHeight)) {
                try {
                    createInCache(sourceFile, entityLastModified, maxWidth, maxHeight, fileType, resizer);
                } catch (IOException e) {
                    return -1L;
                }
            }
            
            return getCacheFile(sourceFile, maxWidth, maxHeight).length();
        }
        
        // Slightly inefficient, as we make a local copy of the file first, then serve the bytes of that local copy
        // would be better if we served the bytes back to the client and wrote them into the cache file at the same time
        // but risky if the client disconnected halfway through
        //
        public void cacheAndServe(final FileReference sourceFile, final DateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType, final ImageResizer resizer) throws IOException {
            try {
                createInCache(sourceFile, entityLastModified, maxWidth, maxHeight, fileType, resizer);
                serveFromCache(sourceFile, entityLastModified, out, maxWidth, maxHeight);
            } catch (IOException e) {
                LOGGER.error("Unable to update rendered image cache for " + getCacheFile(sourceFile, maxWidth, maxHeight).getAbsolutePath(),e);
                // fall back to asking the delegate to render the file
                resizer.renderResized(sourceFile, entityLastModified, out, maxWidth, maxHeight, fileType);
            }
            
        }
        
        public void createInCache(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType, final ImageResizer resizer) throws IOException {
            File newCacheEntry = getCacheFile(sourceFile, maxWidth, maxHeight);

            File parentDir = newCacheEntry.getParentFile();
            Assert.isTrue(parentDir.mkdirs() || (parentDir.exists() && parentDir.isDirectory()));
            
            LazyCreationFileOutputStream fos = new LazyCreationFileOutputStream(newCacheEntry);
            resizer.renderResized(sourceFile, entityLastModified, fos, maxWidth, maxHeight, fileType);
            fos.close();
        }

        public boolean contains(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight) {
            File candidate = getCacheFile(sourceFile, maxWidth, maxHeight);
            if (candidate.exists() && candidate.canRead()) {
                LOGGER.debug("Cache file " + candidate.getAbsolutePath() + "exists and is readable");
                if (entityLastModified.isBefore(candidate.lastModified())) {
                    LOGGER.debug("Cache file is not stale; returning cache hit" );
                    return true;
                }
            }
            return false;
        }
        private File getCacheFile(final FileReference sourceFile, final int maxWidth, final int maxHeight) {
            String referencePath = FileSystemBackedHashResolver.partition(sourceFile.toHashReference().getHash().toString());
            
            return new File(cacheRoot,generateCacheKey(referencePath, maxWidth, maxHeight));
        }
        private String generateCacheKey(final String sourceFile, final int maxWidth, final int maxHeight) {
            return sourceFile + "@" + maxWidth + "x" + maxHeight;
        }
        
        
    }

    
}
