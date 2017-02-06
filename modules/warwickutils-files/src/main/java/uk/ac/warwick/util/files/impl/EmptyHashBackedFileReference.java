package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.IOException;
import java.net.URI;

public final class EmptyHashBackedFileReference extends AbstractFileReference implements HashFileReference {

    private final HashFileStore fileStore;
    private final String storeName;
    private final Data data = new Data();

    public EmptyHashBackedFileReference(final HashFileStore store, final String theStoreName) {
        this.fileStore = store;
        this.storeName = theStoreName;
    }

    @Override
    public HashString getHash() {
        return null;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public HashFileReference copyTo(FileReference target) throws IOException {
        return new EmptyHashBackedFileReference(fileStore, storeName);
    }

    @Override
    public HashFileReference renameTo(FileReference target) throws IOException {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public FileData<FileReference> getData() {
        return data;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    class Data implements FileData {
        @Override
        public boolean delete() {
            return true;
        }

        @Override
        public HashFileReference overwrite(ByteSource in) throws IOException {
            // Create a new file, store it separately, return the new hash, leave this empty ref for GC
            return fileStore.createHashReference(in, storeName);
        }

        @Override
        public ByteSource asByteSource() {
            return ByteSource.empty();
        }

        @Override
        public long length() {
            return 0L;
        }

        @Override
        public URI getFileLocation() {
            throw new UnsupportedOperationException("This file reference doesn't exist");
        }

        @Override
        public boolean isExists() {
            return false;
        }

        @Override
        public boolean isFileBacked() {
            return false;
        }
    }

    public void unlink() {
        // Doesn't make sense
    }


}
