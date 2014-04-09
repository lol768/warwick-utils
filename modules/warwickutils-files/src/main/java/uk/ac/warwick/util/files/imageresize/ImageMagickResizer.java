package uk.ac.warwick.util.files.imageresize;

import org.joda.time.DateTime;
import uk.ac.warwick.util.files.FileReference;

import java.io.IOException;
import java.io.OutputStream;

public class ImageMagickResizer implements ImageResizer {
	@Override
	public void renderResized(FileReference sourceFile, DateTime entityLastModified, OutputStream out, int maxWidth, int maxHeight, FileType fileType) throws IOException {

	}

	@Override
	public long getResizedImageLength(FileReference sourceFile, DateTime entityLastModified, int maxWidth, int maxHeight, FileType fileType) throws IOException {
		return 0;
	}
}
