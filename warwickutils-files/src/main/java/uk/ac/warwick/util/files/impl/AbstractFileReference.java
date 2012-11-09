package uk.ac.warwick.util.files.impl;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.core.io.InputStreamSource;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.FileStore.UsingOutput;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.LocalFileReference;
import uk.ac.warwick.util.files.hash.HashString;

public abstract class AbstractFileReference implements FileReference {

    public final LocalFileReference toLocalReference() {
        if (isLocal()) {
            return (LocalFileReference) this;
        } else {
            throw new IllegalArgumentException("Not locally stored");
        }
    }
    
    public final HashFileReference toHashReference() {
        if (!isLocal()) {
            return (HashFileReference) this;
        } else {
            throw new IllegalArgumentException("Locally stored");
        }
    }
    
    protected abstract FileData getData();
    
    public final boolean isExists() {
        return getData().isExists();
    }

    public final long length() {
        return getData().length();
    }

    public final boolean delete() {
        return getData().delete();
    }

    public final InputStream getInputStream() throws IOException {
        return getData().getInputStream();
    }

    public final InputStreamSource getInputStreamSource() {
        return getData().getInputStreamSource();
    }

    public final File getRealFile() {
        return getData().getRealFile();
    }

    public final String getRealPath() {
        return getData().getRealPath();
    }

    public final boolean isFileBacked() {
        return getData().isFileBacked();
    }

    public final HashString overwrite(UsingOutput callback) throws IOException {
        return getData().overwrite(callback);
    }

    public final HashString overwrite(File file) throws IOException {
        return getData().overwrite(file);
    }

    public final HashString overwrite(byte[] contents) throws IOException {
        return getData().overwrite(contents);
    }

    public final HashString overwrite(String contents) throws IOException {
        return getData().overwrite(contents);
    }
    
    public final String getContentsAsString() throws IOException {
        if (!isExists()) {
            return "";
        }
        
        return FileCopyUtils.copyToString(new InputStreamReader(getInputStream(), StringUtils.DEFAULT_ENCODING));
    }

}
