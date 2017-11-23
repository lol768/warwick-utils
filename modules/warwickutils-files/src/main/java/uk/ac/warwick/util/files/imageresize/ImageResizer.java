package uk.ac.warwick.util.files.imageresize;

import uk.ac.warwick.util.files.FileReference;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

public interface ImageResizer {
    
    enum FileType { png, gif, jpg }

    void renderResized(final FileReference sourceFile, final Instant entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType)
            throws IOException;
    
    long getResizedImageLength(final FileReference sourceFile, final Instant entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType) throws IOException;

}