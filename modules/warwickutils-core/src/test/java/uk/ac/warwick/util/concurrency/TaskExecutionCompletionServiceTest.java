package uk.ac.warwick.util.concurrency;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public final class TaskExecutionCompletionServiceTest {
	
	@Test
	public void waitForNothing() throws Exception {
		TaskExecutionService service = new TaskExecutionService(1);
		TaskExecutionCompletionService<Void> completionService = service.newCompletionService();
		
		completionService.waitForCompletion(true);
	}
	
	@Test(expected = ExecutionException.class)
	public void waitWithException() throws Exception {
		TaskExecutionService service = new TaskExecutionService(1);
		TaskExecutionCompletionService<Void> completionService = service.newCompletionService();
		
		completionService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				throw new NullPointerException();
			}
		});
		
		completionService.waitForCompletion(true);
	}
	
	@Test
	public void waitWithSwallowedException() throws Exception {
		TaskExecutionService service = new TaskExecutionService(1);
		TaskExecutionCompletionService<Void> completionService = service.newCompletionService();
		
		completionService.submit(new Callable<Void>() {
			public Void call() throws Exception {
				throw new NullPointerException();
			}
		});
		
		assertEquals(0, completionService.waitForCompletion(false).size());
	}
	
	@Test
	public void waitForCompletion() throws Exception {
		TaskExecutionService service = new TaskExecutionService(1);
		TaskExecutionCompletionService<Void> completionService = service.newCompletionService();
		
		final Monitor monitor = new Monitor();
		
		for (int i = 0; i<100; i++) {
			completionService.submit(new Callable<Void>() {
				public Void call() throws Exception {
					monitor.ping();
					return null;
				}
			});
		}
		
		assertEquals(100, completionService.waitForCompletion(true).size());
		
		assertEquals(100, monitor.completedTasks);
	}
	
	private class Monitor {
		
		private int completedTasks = 0;
		
		public synchronized void ping() {
			completedTasks++;
		}
		
	}

}
