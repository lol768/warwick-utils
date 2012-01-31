package uk.ac.warwick.util.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.joda.time.DateTime;

import uk.ac.warwick.util.core.spring.FileUtils;
import uk.ac.warwick.util.files.Storeable.StorageStrategy;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.impl.AbstractFileReference;

public class MockFileStore implements FileStore {
    
    public FileReference get(Storeable storeable) throws FileNotFoundException {
        return null;
    }

    public LocalFileReference getForPath(Storeable storeable, String path) throws FileNotFoundException {
        return null;
    }

    public FileReference store(Storeable storeable, String storeName, UploadedFileDetails uploadedFile) throws IOException {
        return null;
    }

    public LocalFileReference store(Storeable storeable, String storeName, UsingOutput delegate) throws IOException {
        return null;
    }

    public class MockFileReference extends AbstractFileReference implements LocalFileReference {
        
        private final String path;
        
        public MockFileReference(String path) {
            this.path = path;
        }

        public String getFileName() {
            return FileUtils.getFileName(path);
        }

        public String getPath() {
            return path;
        }
        
        public DateTime getLastModified() {
            return null;
        }
        
        public HashString getHash() {
            return null;
        }

        public LocalFileReference copyTo(String path) throws IOException {
            return null;
        }

        public FileData getData() {
            return null;
        }

        public FileReference copyTo(FileReference target) throws IOException {
            return null;
        }

        public boolean isLocal() {
            return true;
        }

        public File getFile() {
            return null;
        }

        public LocalFileReference renameTo(String path) throws IOException {
            return null;
        }

        public FileReference renameTo(FileReference target) throws IOException {
            return null;
        }

        public StorageStrategy getStorageStrategy() {
            return null;
        }

        public void unlink() {
        }
        
    }

}
