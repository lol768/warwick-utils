package uk.ac.warwick.util.files.imageresize;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.joda.time.DateTime;

import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;

/**
 * Image resizer that allows access to a File containing the resized (or not resized) image.
 * Do not write to the returned File!
 */
public interface FileExposingImageResizer extends ImageResizer {
	File getResized(final FileReference sourceFile, final DateTime entityLastModified, final int maxWidth, final int maxHeight, final FileType fileType) throws IOException;
}
