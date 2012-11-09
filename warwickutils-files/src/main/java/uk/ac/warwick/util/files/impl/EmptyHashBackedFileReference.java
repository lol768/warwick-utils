package uk.ac.warwick.util.files.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.InputStreamSource;

import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.files.CopyToOutput;
import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.FileStore.UsingOutput;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.hash.HashString;

public final class EmptyHashBackedFileReference extends AbstractFileReference implements HashFileReference {
    
    private final HashFileStore fileStore;
    private final String storeName;
    private final Data data = new Data();
    
    public EmptyHashBackedFileReference(final HashFileStore store, final String theStoreName) {
        this.fileStore = store;
        this.storeName = theStoreName;
    }

    public HashString getHash() {
        return null;
    }
    
    public String getPath() {
        return null;
    }

    public HashFileReference copyTo(FileReference target) throws IOException {
        return new EmptyHashBackedFileReference(fileStore, storeName);
    }

    public HashFileReference renameTo(FileReference target) throws IOException {
        return this;
    }

    public FileData getData() {
        return data;
    }

    public boolean isLocal() {
        return false;
    }

    class Data implements FileData {
        public boolean delete() {
            return true;
        }

        public InputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException("This file reference doesn't exist");
        }

        public InputStreamSource getInputStreamSource() {
            throw new UnsupportedOperationException("This file reference doesn't exist");
        }

        public File getRealFile() {
            throw new UnsupportedOperationException("This file reference doesn't exist");
        }

        public String getRealPath() {
            throw new UnsupportedOperationException("This file reference doesn't exist");
        }

        public boolean isExists() {
            return false;
        }

        public boolean isFileBacked() {
            return false;
        }

        public long length() {
            return 0;
        }
        
        public HashString overwrite(final File fileToCopy) throws IOException {
            return overwrite(new CopyToOutput(fileToCopy));
        }

        public HashString overwrite(final byte[] contents) throws IOException {
            return overwrite(new CopyToOutput(new ByteArrayInputStream(contents)));
        }

        public HashString overwrite(final String contents) throws IOException {
            return overwrite(StringUtils.create(contents));
        }

        public HashString overwrite(UsingOutput callback) throws IOException {
            // Create a new file, storing it seperately, and return the new hash
            HashFileReference newReference = fileStore.createHashReference(callback, storeName);
            return newReference.getHash();
        }

        public String getContentsAsString() throws IOException {
            return "";
        }
    }

    public void unlink() {
        // Doesn't make sense
    }
    

}
