package uk.ac.warwick.util.files.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.LineProcessor;
import uk.ac.warwick.util.files.FileStoreStatistics;

import java.io.IOException;

/**
 * Won't track .openStream() statistics
 */
public abstract class StatisticsRecordingCharSource extends CharSource {

    private final FileStoreStatistics statistics;

    public StatisticsRecordingCharSource(FileStoreStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public long copyTo(Appendable appendable) throws IOException {
        return statistics.time(() -> super.copyTo(appendable), statistics::referenceRead);
    }

    @Override
    public long copyTo(CharSink sink) throws IOException {
        return statistics.time(() -> super.copyTo(sink), statistics::referenceRead);
    }

    @Override
    public String read() throws IOException {
        return statistics.time(() -> super.read(), statistics::referenceRead);
    }

    @Override
    public String readFirstLine() throws IOException {
        return statistics.time(() -> super.readFirstLine(), statistics::referenceRead);
    }

    @Override
    public ImmutableList<String> readLines() throws IOException {
        return statistics.time(() -> super.readLines(), statistics::referenceRead);
    }

    @Override
    public <T> T readLines(LineProcessor<T> processor) throws IOException {
        return statistics.time(() -> super.readLines(processor), statistics::referenceRead);
    }

}
