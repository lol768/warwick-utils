package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import uk.ac.warwick.util.core.spring.FileUtils;
import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileStore;
import uk.ac.warwick.util.files.FileStoreStatistics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;

import static com.google.common.base.Preconditions.*;

/**
 * FileData that is stored on the filesystem. This doesn't make any
 * assertions about whether the data is stored under a URL hierarchy or
 * stored under a hash-based filename - just that the bytes are on disk.
 */
@Configurable
public abstract class AbstractFileBackedFileData implements FileData {

    @Autowired(required = true)
    private DeletionBinHolder deletionBinHolder;

    private final FileStoreStatistics statistics;

    protected AbstractFileBackedFileData(FileStore fileStore) {
        this.statistics = fileStore.getStatistics();
    }

    @Override
    public final boolean isExists() {
        return getFile().exists(); 
    }

    @Override
    public ByteSource asByteSource() {
        return Files.asByteSource(getFile());
    }

    @Override
    public long length() {
        return isExists() ? getFile().length() : 0L;
    }

    @Override
    public final URI getFileLocation() {
        return getFile().toURI();
    }

    @Override
    public final boolean isFileBacked() {
        return true;
    }

    @Override
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

    protected class FileBackedByteSource extends StatisticsRecordingByteSource {

        private final File file;

        private FileBackedByteSource(File file) {
            super(statistics);
            this.file = checkNotNull(file);
        }

        @Override
        public InputStream openStream() throws IOException {
            return Files.asByteSource(file).openStream();
        }

        @Override
        public long size() throws IOException {
            return Files.asByteSource(file).size();
        }

        @Override
        public byte[] read() throws IOException {
            return statistics.time(() -> Files.asByteSource(file).read(), statistics::referenceRead);
        }

        @Override
        public CharSource asCharSource(Charset charset) {
            return new FileBackedCharSource(charset, this);
        }

        @Override
        public String toString() {
            return "FileBackedByteSource.asByteSource(" + file + ")";
        }

    }

    protected class FileBackedCharSource extends StatisticsRecordingCharSource {

        private final Charset charset;

        private final FileBackedByteSource byteSource;

        private FileBackedCharSource(Charset charset, FileBackedByteSource byteSource) {
            super(statistics);
            this.charset = checkNotNull(charset);
            this.byteSource = checkNotNull(byteSource);
        }

        @Override
        public Reader openStream() throws IOException {
            return new InputStreamReader(byteSource.openStream(), charset);
        }

        @Override
        public String toString() {
            return byteSource.toString() + ".asCharSource(" + charset + ")";
        }

    }

}
