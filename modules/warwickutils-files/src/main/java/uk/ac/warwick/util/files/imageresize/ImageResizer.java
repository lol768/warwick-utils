package uk.ac.warwick.util.files.imageresize;

import com.google.common.io.ByteSource;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZonedDateTime;

public interface ImageResizer {
    
    enum FileType { png, gif, jpg }

    void renderResized(final ByteSource source, final HashString hash, final ZonedDateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType)
            throws IOException;

    long getResizedImageLength(final ByteSource source, final HashString hash, final ZonedDateTime entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType) throws IOException;

}