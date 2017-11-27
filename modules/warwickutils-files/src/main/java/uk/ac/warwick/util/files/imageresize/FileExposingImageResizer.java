package uk.ac.warwick.util.files.imageresize;

import uk.ac.warwick.util.files.FileReference;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

/**
 * Image resizer that allows access to a File containing the resized (or not resized) image.
 * Do not write to the returned File!
 */
public interface FileExposingImageResizer extends ImageResizer {
	File getResized(final FileReference sourceFile, final Instant entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType) throws IOException;
}
