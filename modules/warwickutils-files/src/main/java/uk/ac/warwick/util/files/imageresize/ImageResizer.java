package uk.ac.warwick.util.files.imageresize;

import java.io.IOException;
import java.io.OutputStream;

import org.joda.time.DateTime;

import uk.ac.warwick.util.files.FileReference;

public interface ImageResizer {
    
    public enum FileType { png, gif, jpg }

    void renderResized(final FileReference sourceFile, final DateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType)
            throws IOException;
    
    long getResizedImageLength(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType) throws IOException;

}