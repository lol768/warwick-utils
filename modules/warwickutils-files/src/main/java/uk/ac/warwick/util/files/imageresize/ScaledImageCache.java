package uk.ac.warwick.util.files.imageresize;

import org.joda.time.DateTime;
import uk.ac.warwick.util.files.FileReference;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface ScaledImageCache {

    void cacheAndServe(final FileReference sourceFile, final DateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final ImageResizer.FileType fileType, final ImageResizer resizer) throws IOException;

    void serveFromCache(final FileReference sourceFile, final DateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight) throws IOException;

    long getFileSize(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight, final ImageResizer.FileType fileType, final ImageResizer resizer);

    boolean contains(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight);

    void createInCache(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight, final ImageResizer.FileType fileType, final ImageResizer resizer) throws IOException;

}
