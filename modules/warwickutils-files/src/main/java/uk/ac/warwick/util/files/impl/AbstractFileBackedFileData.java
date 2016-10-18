package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import uk.ac.warwick.util.core.spring.FileUtils;
import uk.ac.warwick.util.files.FileData;

import java.io.File;
import java.net.URI;

/**
 * FileData that is stored on the filesystem. This doesn't make any
 * assertions about whether the data is stored under a URL hierarchy or
 * stored under a hash-based filename - just that the bytes are on disk.
 */
@Configurable
public abstract class AbstractFileBackedFileData implements FileData {

    @Autowired(required = true)
    private DeletionBinHolder deletionBinHolder;

    @Override
    public final boolean isExists() {
        return getFile().exists(); 
    }

    @Override
    public ByteSource asByteSource() {
        return Files.asByteSource(getFile());
    }

    @Override
    public long length() {
        return isExists() ? getFile().length() : 0L;
    }

    @Override
    public final URI getFileLocation() {
        return getFile().toURI();
    }

    @Override
    public final boolean isFileBacked() {
        return true;
    }

    @Override
    public final boolean delete() {
        // Sometimes, files don't exist any more. The end result is still "the file doesn't exist", so just quit early.
        if (!isExists()) { return true; }
        
        FileUtils.recursiveDelete(getFile(), deletionBinHolder.getDeletionBinDirectory());
        
        // This will have thrown an exception if the deletion failed
        return true;
    }

    public abstract File getFile();

    @Override
    public final int hashCode() {
        return toString().hashCode();
    }

    @Override
    public final String toString() {
        return getFile().toString();
    }

    public final DeletionBinHolder getDeletionBinHolder() {
        return deletionBinHolder;
    }

    public final void setDeletionBinHolder(DeletionBinHolder deletionBinHolder) {
        this.deletionBinHolder = deletionBinHolder;
    }

}
