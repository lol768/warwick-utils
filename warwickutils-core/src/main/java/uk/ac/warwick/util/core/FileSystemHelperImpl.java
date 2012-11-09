package uk.ac.warwick.util.core;

import java.io.File;
import java.io.IOException;

import uk.ac.warwick.util.core.spring.FileUtils;

public final class FileSystemHelperImpl implements FileSystemHelper {
    public boolean move(final File source, final File target) throws IOException {
        if (source.renameTo(target)) {
            return true;
        } else {
            /**
             * Java fails to move between file systems, so recursive copy and
             * then delete.
             */
            recursiveCopy(source, target);
            FileUtils.recursiveDelete(source);
            return true;
        }
    }

    public void recursiveCopy(final File source, final File target) throws IOException {
        FileUtils.copy(source, target, true);
    }

    public void copy(final File source, final File target) throws IOException {
        FileUtils.copy(source, target, false);
    }

    public void optionalCopy(final File source, final File target) throws IOException {
        if (!source.exists()) {
            return;
        }

        if (!target.exists()) {
            if (source.isDirectory()) {
                if (!target.mkdirs()) {
                    throw new IllegalStateException("Cannot create target directory: " + target);
                }
            } // else do not create as the file will be created in copy
        }

        // do not copy directories at all. FileUtils will always copy
        // directories! Sucks heh.
        if (source.isFile()) {
            FileUtils.copy(source, target, false);
        }
    }

    public void delete(final File target) throws IOException {
        FileUtils.recursiveDelete(target);
    }

    public void deleteIfEmpty(final File target, final boolean recurse) throws IOException {
        if (target.isDirectory() && target.list().length == 0) {
            // recursive, but empty :)
            FileUtils.recursiveDelete(target);
            if (recurse) {
                deleteIfEmpty(target.getParentFile(), true);
            }
        }
    }

    public void deleteIfExists(final File target) throws IOException {
        if (target.exists()) {
            delete(target);
        }

    }
}
