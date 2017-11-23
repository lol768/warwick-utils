package uk.ac.warwick.util.files.imageresize;

import uk.ac.warwick.util.files.FileReference;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

public interface ScaledImageCache {

    void cacheAndServe(final FileReference sourceFile, final Instant entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final ImageResizer.FileType fileType, final ImageResizer resizer) throws IOException;

    void serveFromCache(final FileReference sourceFile, final Instant entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight) throws IOException;

    long getFileSize(final FileReference sourceFile, final Instant entityLastModified, final int maxWidth, final int maxHeight, final ImageResizer.FileType fileType, final ImageResizer resizer);

    boolean contains(final FileReference sourceFile, final Instant entityLastModified, final int maxWidth, final int maxHeight);

    void createInCache(final FileReference sourceFile, final Instant entityLastModified, final int maxWidth, final int maxHeight, final ImageResizer.FileType fileType, final ImageResizer resizer) throws IOException;

}
