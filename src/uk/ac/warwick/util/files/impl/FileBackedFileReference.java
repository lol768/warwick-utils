package uk.ac.warwick.util.files.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;

import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.FileStore.UsingOutput;
import uk.ac.warwick.util.files.LocalFileReference;
import uk.ac.warwick.util.files.LocalFileStore;
import uk.ac.warwick.util.files.Storeable;
import uk.ac.warwick.util.files.Storeable.StorageStrategy;
import uk.ac.warwick.util.files.hash.HashString;

/**
 * A type of reference that backs on to a single file, and doesn't offer
 * any sort of data sharing or anything. There should only be a single
 * reference to a particular file - it should behave basically the
 * same as regular file access.
 * <p>
 * Most of the time you shouldn't need to create one of these directly -
 * they will be created from FileStore. However, there are a few places
 * that require a FileReference and all you have is a File - in that case,
 * you can create one of these with a null FileStore, being aware that
 * some operations (copy) will not work.
 */
public final class FileBackedFileReference extends AbstractFileReference implements LocalFileReference {

    private final File file;
    private final Data data;
    private final String path;
    
    private final LocalFileStore fileStore;
    private final StorageStrategy storageStrategy;
    
    /**
     * @param store FileStore required for certain operations. In some cases this can be null.
     * @param f
     * @param thepath The URL path to identify the file by. 
     */
    public FileBackedFileReference(LocalFileStore store, File f, String thepath, StorageStrategy theStorageStrategy) {
        this.fileStore = store;
        this.file = f;
        this.path = FilenameUtils.separatorsToUnix(thepath);
        this.data = new Data();
        this.storageStrategy = theStorageStrategy;
    }

    public FileData getData() {
        return data;
    }

    public String getFileName() {
        return uk.ac.warwick.util.core.spring.FileUtils.getFileName(path);
    }

    public String getPath() {
        return path;
    }
    
    public HashString getHash() {
        return null;
    }
    
    public LocalFileReference copyTo(FileReference target) throws IOException {
        return copyTo(target.toLocalReference().getPath());
    }
    
    public LocalFileReference copyTo(String target) throws IOException {
        return copyTo(new FileStoreable(storageStrategy, target));
    }

    private LocalFileReference copyTo(Storeable target) throws IOException {
        return fileStore.copy(this, target);
    }
    
    public LocalFileReference renameTo(FileReference target) throws IOException {
        return renameTo(target.toLocalReference().getPath());
    }
    
    public LocalFileReference renameTo(String target) throws IOException {
        return renameTo(new FileStoreable(storageStrategy, target));
    }

    private LocalFileReference renameTo(Storeable target) throws IOException {
        return fileStore.rename(this, target);
    }

    public File getFile() {
        return file;
    }
    
    public boolean isLocal() {
        return true;
    }

    public DateTime getLastModified() {
        return data.getLastModified();
    }
    
    public StorageStrategy getStorageStrategy() {
        return storageStrategy;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return getPath() + " (" + data.toString() + ")";
    }

    class Data extends AbstractFileBackedFileData {
        
        public DateTime getLastModified() {
            return new DateTime(getFile().lastModified());
        }

        @Override
        public File getFile() {
            return file;
        }

        public HashString overwrite(UsingOutput callback) throws IOException {
            FileOutputStream os = new FileOutputStream(file);
            try {
                callback.doWith(os);
            } finally {
                os.close();
            }
            return null;
        }
        
    }
    
    private static class FileStoreable implements Storeable {
        
        private final String path;
        private final StorageStrategy strategy;
        
        public FileStoreable(StorageStrategy theStrategy, String thePath) {
            this.strategy = theStrategy;
            this.path = thePath;
        }

        public StorageStrategy getStrategy() {
            return strategy;
        }

        public String getPath() {
            return path;
        }
        
        public HashString getHash() {
            return null;
        }
        
    }

    public void unlink() {
        getData().delete();
    }    

}
