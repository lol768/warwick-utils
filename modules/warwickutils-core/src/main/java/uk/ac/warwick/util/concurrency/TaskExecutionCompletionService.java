package uk.ac.warwick.util.concurrency;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

public final class TaskExecutionCompletionService<T> extends ExecutorCompletionService<T> implements CountingCompletionService<T> {

    private int submittedTasks = 0;

    public TaskExecutionCompletionService(TaskExecutionService executor) {
        super(executor);
    }

    @Override
    public Future<T> submit(Callable<T> task) {
        submittedTasks++;

        return super.submit(task);
    }

    @Override
    public Future<T> submit(Runnable task, T result) {
        submittedTasks++;

        return super.submit(task, result);
    }

    @Override
    public Future<T> poll() {
        Future<T> future = super.poll();
        if (future != null) {
            submittedTasks--;
        }

        return future;
    }

    @Override
    public Future<T> poll(long timeout, TimeUnit unit) throws InterruptedException {
        Future<T> future = super.poll(timeout, unit);
        if (future != null) {
            submittedTasks--;
        }

        return future;
    }

    @Override
    public Future<T> take() throws InterruptedException {
        submittedTasks--;

        return super.take();
    }

    /**
     * Wait for the completion of all submitted tasks.
     * <p>
     * Will return a list containing all of the results of successful tasks, in
     * the order they were successfully completed.
     * 
     * @throws ExecutionException
     *             Only if throwOnException is true
     */
    public List<T> waitForCompletion(boolean throwOnException) throws InterruptedException, ExecutionException {
        List<T> results = Lists.newArrayList();

        for (int i = submittedTasks; i > 0; i--) {
            try {
                results.add(take().get());
            } catch (CancellationException | ExecutionException e) {
                if (throwOnException) {
                    throw e;
                }
            }
        }

        return results;
    }

}
