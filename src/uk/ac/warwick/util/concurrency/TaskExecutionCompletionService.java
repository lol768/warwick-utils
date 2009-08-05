package uk.ac.warwick.util.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class TaskExecutionCompletionService<T> extends
		ExecutorCompletionService<T> implements CompletionService<T> {
	
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
	public Future<T> poll(long timeout, TimeUnit unit)
			throws InterruptedException {
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
	 * 
	 * @throws ExecutionException Only if throwOnException is true
	 */
	public void waitForCompletion(boolean throwOnException) throws InterruptedException, ExecutionException {
		for (int i = submittedTasks; i > 0; i--) {
			try {
				take().get();
			} catch (ExecutionException e) {
				if (throwOnException) {
					throw e;
				}
			}
		}
	}

}
