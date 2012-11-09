package uk.ac.warwick.util.files;

import java.io.File;

import uk.ac.warwick.util.files.Storeable.StorageStrategy;

public abstract class HashOnlyStorageStrategy implements StorageStrategy {

    public final File getRootDirectory() {
        throw new UnsupportedOperationException("This strategy only supports hash references");
    }

    public final boolean isSupportsLocalReferences() {
        return false;
    }    

}
