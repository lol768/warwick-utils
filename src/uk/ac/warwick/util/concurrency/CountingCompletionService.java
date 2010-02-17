package uk.ac.warwick.util.concurrency;

import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;

public interface CountingCompletionService<T> extends CompletionService<T> {
    
    /**
     * Wait for the completion of all submitted tasks.
     * <p>
     * Will return a list containing all of the results of successful tasks, in
     * the order they were successfully completed.
     * 
     * @throws ExecutionException
     *             Only if throwOnException is true
     */
    List<T> waitForCompletion(boolean throwOnException) throws InterruptedException, ExecutionException;

}
