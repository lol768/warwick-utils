package uk.ac.warwick.util.files.impl;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import uk.ac.warwick.util.files.FileStoreStatistics;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Won't track .openStream() statistics
 */
public abstract class StatisticsRecordingByteSource extends ByteSource {

    private final FileStoreStatistics statistics;

    public StatisticsRecordingByteSource(FileStoreStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public long copyTo(OutputStream output) throws IOException {
        return statistics.time(() -> super.copyTo(output), statistics::referenceRead);
    }

    @Override
    public long copyTo(ByteSink sink) throws IOException {
        return statistics.time(() -> super.copyTo(sink), statistics::referenceRead);
    }

    @Override
    public byte[] read() throws IOException {
        return statistics.time(() -> super.read(), statistics::referenceRead);
    }

    @Override
    public <T> T read(ByteProcessor<T> processor) throws IOException {
        return statistics.time(() -> super.read(processor), statistics::referenceRead);
    }

    @Override
    public HashCode hash(HashFunction hashFunction) throws IOException {
        return statistics.time(() -> super.hash(hashFunction), statistics::referenceRead);
    }

    @Override
    public boolean contentEquals(ByteSource other) throws IOException {
        return statistics.time(() -> super.contentEquals(other), statistics::referenceRead);
    }

}
