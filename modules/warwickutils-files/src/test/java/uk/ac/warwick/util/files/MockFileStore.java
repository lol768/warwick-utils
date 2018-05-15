package uk.ac.warwick.util.files;

import com.google.common.io.ByteSource;
import uk.ac.warwick.util.core.spring.FileUtils;
import uk.ac.warwick.util.files.Storeable.StorageStrategy;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.impl.AbstractFileReference;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;

public class MockFileStore implements FileStore {

    @Override
    public FileReference get(Storeable storeable) throws FileNotFoundException {
        return null;
    }

    @Override
    public FileReference store(Storeable storeable, String storeName, ByteSource in) throws IOException {
        return null;
    }

    public class MockFileReference extends AbstractFileReference implements LocalFileReference {

        private final String path;

        public MockFileReference(String path) {
            this.path = path;
        }

        @Override
        public String getFileName() {
            return FileUtils.getFileName(path);
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public Instant getLastModified() {
            return null;
        }

        @Override
        public HashString getHash() {
            return null;
        }

        @Override
        public LocalFileReference copyTo(String path) throws IOException {
            return null;
        }

        @Override
        public FileData<FileReference> getData() {
            return null;
        }

        @Override
        public FileReference copyTo(FileReference target) throws IOException {
            return null;
        }

        @Override
        public boolean isLocal() {
            return true;
        }

        @Override
        public LocalFileReference renameTo(String path) throws IOException {
            return null;
        }

        @Override
        public FileReference renameTo(FileReference target) throws IOException {
            return null;
        }

        @Override
        public FileReference overwrite(ByteSource in) throws IOException {
            return null;
        }

        @Override
        public StorageStrategy getStorageStrategy() {
            return null;
        }

        @Override
        public void unlink() {
        }

    }

    @Override
    public FileStoreStatistics getStatistics() {
        return new DefaultFileStoreStatistics(this);
    }
}
