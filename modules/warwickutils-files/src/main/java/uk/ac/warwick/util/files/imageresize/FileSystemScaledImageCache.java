package uk.ac.warwick.util.files.imageresize;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.hash.impl.FileSystemBackedHashResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Entries are stored as {cache root}/{original filename}@{width}x{height}
 * Entries are checked against the original file for stale-ness
 * No removal of cache entries is performed
 * No logging of access times/frequencies is done
 */
public class FileSystemScaledImageCache implements FileExposingScaledImageCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemScaledImageCache.class);

    private final File cacheRoot;

    private final String separator;

    public FileSystemScaledImageCache(final File cacheDir, final String cacheSeparator) {
        this.cacheRoot = cacheDir;
        this.separator = cacheSeparator;
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

    public long getFileSize(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight, final ImageResizer.FileType fileType, final ImageResizer resizer) {
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
    public void cacheAndServe(final FileReference sourceFile, final DateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final ImageResizer.FileType fileType, final ImageResizer resizer) throws IOException {
        try {
            createInCache(sourceFile, entityLastModified, maxWidth, maxHeight, fileType, resizer);
            serveFromCache(sourceFile, entityLastModified, out, maxWidth, maxHeight);
        } catch (IOException e) {
            LOGGER.error("Unable to update rendered image cache for " + getCacheFile(sourceFile, maxWidth, maxHeight).getAbsolutePath(),e);
            // fall back to asking the delegate to render the file
            resizer.renderResized(sourceFile, entityLastModified, out, maxWidth, maxHeight, fileType);
        }

    }

    public void createInCache(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight, final ImageResizer.FileType fileType, final ImageResizer resizer) throws IOException {
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

    public File getCacheFile(final FileReference sourceFile, final int maxWidth, final int maxHeight) {
        String referencePath;
        if (sourceFile.isLocal()) {
            referencePath = sourceFile.getPath();
        } else {
            referencePath = FileSystemBackedHashResolver.partition(sourceFile.toHashReference().getHash().toString());
        }

        return new File(cacheRoot, generateCacheKey(referencePath, maxWidth, maxHeight));
    }

    private String generateCacheKey(final String sourceFile, final int maxWidth, final int maxHeight) {
        return sourceFile + separator + maxWidth + "x" + maxHeight;
    }

}
