package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.LocalFileReference;

import java.net.URI;

public abstract class AbstractFileReference implements FileReference {

    @Override
    public final LocalFileReference toLocalReference() {
        if (isLocal()) {
            return (LocalFileReference) this;
        } else {
            throw new IllegalArgumentException("Not locally stored");
        }
    }

    @Override
    public final HashFileReference toHashReference() {
        if (!isLocal()) {
            return (HashFileReference) this;
        } else {
            throw new IllegalArgumentException("Locally stored");
        }
    }

    protected abstract FileData getData();

    @Override
    public final boolean isExists() {
        return getData().isExists();
    }

    @Override
    public final boolean delete() {
        return getData().delete();
    }

    @Override
    public ByteSource asByteSource() {
        return getData().asByteSource();
    }

    @Override
    public long length() {
        return getData().length();
    }

    @Override
    public final URI getFileLocation() {
        return getData().getFileLocation();
    }

    @Override
    public final boolean isFileBacked() {
        return getData().isFileBacked();
    }
}
