package uk.ac.warwick.util.concurrency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

/**
 * Provides an application-wide bean to execute parallel tasks.
 * 
 * @author Mat Mannion
 */
public final class TaskExecutionService implements ExecutorService, CompletionServiceProvider {
    
    private static final Logger LOGGER = Logger.getLogger(TaskExecutionService.class);

    private final ExecutorService delegate;

    public TaskExecutionService() {
        this(Executors.newCachedThreadPool());
    }
    
    public TaskExecutionService(int threadLimit) {
    	this(Executors.newFixedThreadPool(threadLimit));
    }
    
    public TaskExecutionService(final ExecutorService executor) {
        delegate = executor;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        LOGGER.debug("Terminating all tasks (timeout " + timeout + unit.toString() + ")");
        return delegate.awaitTermination(timeout, unit);
    }

    // In Java5, unfortunately we have to coax this into a Callable<T>  
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        LOGGER.debug("Invoking " + tasks.size() + " tasks: " + tasks);
        return delegate.invokeAll(new ArrayList<Callable<T>>(tasks));
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        LOGGER.debug("Invoking " + tasks.size() + " tasks: " + tasks + " with " + timeout + unit.toString() + " timeout");
        return delegate.invokeAll(new ArrayList<Callable<T>>(tasks), timeout, unit);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        LOGGER.debug("Invoking " + tasks.size() + " tasks: " + tasks);
        return delegate.invokeAny(new ArrayList<Callable<T>>(tasks));
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException {
        LOGGER.debug("Invoking " + tasks.size() + " tasks: " + tasks + " with " + timeout + unit.toString() + " timeout");
        try {
            return delegate.invokeAny(new ArrayList<Callable<T>>(tasks), timeout, unit);
        } catch (TimeoutException e) {
            throw new ExecutionException("The task timed out", e);
        }
    }

    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    public void shutdown() {
        LOGGER.info("Shutting down execution service");
        delegate.shutdown();
    }

    public List<Runnable> shutdownNow() {
        LOGGER.info("Shutting down execution service IMMEDIATELY");
        return delegate.shutdownNow();
    }

    public <T> Future<T> submit(Callable<T> task) {
        LOGGER.debug("Submitting task " + task + " to execution pool");
        return delegate.submit(task);
    }

    public Future<?> submit(Runnable task) {
        LOGGER.debug("Submitting task " + task + " to execution pool");
        return delegate.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        LOGGER.debug("Submitting task " + task + " to execution pool");
        return delegate.submit(task, result);
    }

    public void execute(Runnable command) {
        LOGGER.debug("Executing command " + command);
        delegate.execute(command);
    }
    
    /**
     * Initiates a new completion service
     */
    public <T> TaskExecutionCompletionService<T> newCompletionService() {
        return new TaskExecutionCompletionService<T>(this);
    }

}
