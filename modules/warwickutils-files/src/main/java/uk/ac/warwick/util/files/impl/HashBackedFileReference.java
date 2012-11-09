package uk.ac.warwick.util.files.impl;

import java.io.File;
import java.io.IOException;

import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.FileStore.UsingOutput;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.hash.HashString;

public final class HashBackedFileReference extends AbstractFileReference implements HashFileReference {
    
    private final HashFileStore fileStore;
    private final FileData data;
    
    private File file;
    private HashString hash;
    
    public HashBackedFileReference(final HashFileStore store, final File backingFile, final HashString theHash) {
        this.fileStore = store;
        this.file = backingFile;
        this.hash = theHash;
        this.data = new Data();
    }

    public HashString getHash() {
        return hash; // he'll save every one of us
    }
    
    public String getPath() {
        return null;
    }
    
    public void update(File backingFile, HashString theHash) {
        this.file = backingFile;
        this.hash = theHash;
    }

    public HashFileReference copyTo(FileReference target) throws IOException {
        return new HashBackedFileReference(fileStore, file, hash);
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
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return hash + " (" + data.toString() + ")";
    }

    class Data extends AbstractFileBackedFileData {

        @Override
        public File getFile() {
            return file;
        }

        public HashString overwrite(UsingOutput callback) throws IOException {
            // Create a new file, storing it separately, and return the new hash
            HashFileReference newReference = fileStore.createHashReference(callback, getHash().getStoreName());
            
            update(newReference.getRealFile(), newReference.getHash());
            
            return hash;
        }
        
    }

    public void unlink() {
        // Do nothing - leave file data in place, and let the periodic
        // cleanup get rid of the data as necessary.
    }

}
