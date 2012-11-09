package uk.ac.warwick.util.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.junit.Test;

public final class TaskExecutionServiceTest {
	
	@Test
	public void fixedSize() throws Exception {
		// Just one!
		TaskExecutionService service = new TaskExecutionService(1);
		
		List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		for (int i = 0; i<100; i++) {
			final int theInt = i+1;
			futures.add(service.submit(new Callable<Integer>() {
				public Integer call() throws Exception {
					Thread.sleep(10);
					return theInt;
				}
			}));
		}
		
		for (Future<Integer> future : futures) {
			future.get();
		}
	}

}
