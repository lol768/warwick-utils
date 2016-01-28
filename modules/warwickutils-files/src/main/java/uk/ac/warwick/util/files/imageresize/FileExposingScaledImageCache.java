package uk.ac.warwick.util.files.imageresize;

import uk.ac.warwick.util.files.FileReference;

import java.io.File;

public interface FileExposingScaledImageCache extends ScaledImageCache {

    File getCacheFile(final FileReference sourceFile, final int maxWidth, final int maxHeight);

}
