package uk.ac.warwick.util.files;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface FileStoreStatistics extends Serializable {

    @FunctionalInterface
    interface FileStoreOperation {
        void run() throws IOException;
    }

    @FunctionalInterface
    interface FileStoreSupplier<T> {
        T get() throws IOException;
    }

    /**
     * Are statistics enabled? If this returns false, no statistics will be recorded.
     */
    boolean isStatisticsEnabled();

    default void time(FileStoreOperation operation, Consumer<Long> statisticsConsumer) throws IOException {
        if (!isStatisticsEnabled()) {
            operation.run();
        } else {
            long started = System.currentTimeMillis();
            operation.run();
            statisticsConsumer.accept(System.currentTimeMillis() - started);
        }
    }

    default <T> T time(FileStoreSupplier<T> supplier, Consumer<Long> statisticsConsumer) throws IOException {
        if (!isStatisticsEnabled()) {
            return supplier.get();
        } else {
            long started = System.currentTimeMillis();
            T result = supplier.get();
            statisticsConsumer.accept(System.currentTimeMillis() - started);
            return result;
        }
    }

    default void timeSafe(Runnable operation, Consumer<Long> statisticsConsumer) {
        try {
            time(operation::run, statisticsConsumer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    default <T> T timeSafe(Supplier<T> supplier, Consumer<Long> statisticsConsumer) {
        try {
            return time(supplier::get, statisticsConsumer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    void referenceOpened(long timeTakenMs);
    void referenceRead(long timeTakenMs);
    void referenceWritten(long timeTakenMs);
    void referenceDeleted(long timeTakenMs);
    void traversed(long timeTakenMs);

    /**
     * Resets all statistics
     */
    void clear();

}
