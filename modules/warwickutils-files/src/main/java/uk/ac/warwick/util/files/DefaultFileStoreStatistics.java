package uk.ac.warwick.util.files;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.concurrent.atomic.AtomicLong;

public class DefaultFileStoreStatistics implements FileStoreStatistics {

    private final FileStore fileStore;

    private volatile boolean statisticsEnabled = false;

    private long startTime;

    private AtomicLong referenceOpenedCount = new AtomicLong();
    private AtomicLong referenceOpenedTime = new AtomicLong();
    private AtomicLong referenceReadCount = new AtomicLong();
    private AtomicLong referenceReadTime = new AtomicLong();
    private AtomicLong referenceWrittenCount = new AtomicLong();
    private AtomicLong referenceWrittenTime = new AtomicLong();
    private AtomicLong referenceDeletedCount = new AtomicLong();
    private AtomicLong referenceDeletedTime = new AtomicLong();
    private AtomicLong traversedCount = new AtomicLong();
    private AtomicLong traversedTime = new AtomicLong();

    public DefaultFileStoreStatistics(FileStore fileStore) {
        clear();
        this.fileStore = fileStore;
    }

    @Override
    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    @Override
    public void clear() {
        referenceOpenedCount.set(0);
        referenceOpenedTime.set(0);
        referenceReadCount.set(0);
        referenceReadTime.set(0);
        referenceWrittenCount.set(0);
        referenceWrittenTime.set(0);
        referenceDeletedCount.set(0);
        referenceDeletedTime.set(0);
        traversedCount.set(0);
        traversedTime.set(0);

        startTime = System.currentTimeMillis();
    }

    @Override
    public void referenceOpened(long timeTakenMs) {
        referenceOpenedCount.getAndIncrement();
        referenceOpenedTime.getAndAdd(timeTakenMs);
    }

    @Override
    public void referenceRead(long timeTakenMs) {
        referenceReadCount.getAndIncrement();
        referenceReadTime.getAndAdd(timeTakenMs);
    }

    @Override
    public void referenceWritten(long timeTakenMs) {
        referenceWrittenCount.getAndIncrement();
        referenceWrittenTime.getAndAdd(timeTakenMs);
    }

    @Override
    public void referenceDeleted(long timeTakenMs) {
        referenceDeletedCount.getAndIncrement();
        referenceDeletedTime.getAndAdd(timeTakenMs);
    }

    @Override
    public void traversed(long timeTakenMs) {
        traversedCount.getAndIncrement();
        traversedTime.getAndAdd(timeTakenMs);
    }

    public long getReferenceOpenedCount() {
        return referenceOpenedCount.get();
    }

    public long getReferenceOpenedTime() {
        return referenceOpenedTime.get();
    }

    public long getReferenceReadCount() {
        return referenceReadCount.get();
    }

    public long getReferenceReadTime() {
        return referenceReadTime.get();
    }

    public long getReferenceWrittenCount() {
        return referenceWrittenCount.get();
    }

    public long getReferenceWrittenTime() {
        return referenceWrittenTime.get();
    }

    public long getReferenceDeletedCount() {
        return referenceDeletedCount.get();
    }

    public long getReferenceDeletedTime() {
        return referenceDeletedTime.get();
    }

    public long getTraversedCount() {
        return traversedCount.get();
    }

    public long getTraversedTime() {
        return traversedTime.get();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("startTime", startTime)
            .append("referenceOpenedCount", referenceOpenedCount)
            .append("referenceOpenedTime", referenceOpenedTime)
            .append("referenceReadCount", referenceReadCount)
            .append("referenceReadTime", referenceReadTime)
            .append("referenceWrittenCount", referenceWrittenCount)
            .append("referenceWrittenTime", referenceWrittenTime)
            .append("referenceDeletedCount", referenceDeletedCount)
            .append("referenceDeletedTime", referenceDeletedTime)
            .append("traversedCount", traversedCount)
            .append("traversedTime", traversedTime)
            .toString();
    }
}
