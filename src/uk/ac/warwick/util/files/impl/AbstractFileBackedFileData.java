package uk.ac.warwick.util.files.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.InputStreamSource;

import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.core.spring.FileUtils;
import uk.ac.warwick.util.files.CopyToOutput;
import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.hash.HashString;

/**
 * FileData that is stored on the filesystem. This doesn't make any
 * assertions about whether the data is stored under a URL hierarchy or
 * stored under a hash-based filename - just that the bytes are on disk.
 */
@Configurable
public abstract class AbstractFileBackedFileData implements FileData, InputStreamSource {

    @Autowired(required = true)
    private DeletionBinHolder deletionBinHolder;

    public final boolean isExists() {
        return getFile().exists(); 
    }

    public final long length() {
        return getFile().length();
    }
    
    public final HashString overwrite(final File fileToCopy) throws IOException {
        return overwrite(new CopyToOutput(fileToCopy));
    }

    public final HashString overwrite(final byte[] contents) throws IOException {
        return overwrite(new CopyToOutput(new ByteArrayInputStream(contents)));
    }

    public final HashString overwrite(final String contents) throws IOException {
        return overwrite(StringUtils.create(contents));
    }

    public final InputStream getInputStream() throws IOException {
        return new FileInputStream(getFile());
    }
    
    public final InputStreamSource getInputStreamSource() {
        return this;
    }

    public final String getRealPath() {
        return getFile().getAbsolutePath();
    }

    public final boolean isFileBacked() {
        return true;
    }

    public final File getRealFile() {
        return getFile();
    }
    
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
