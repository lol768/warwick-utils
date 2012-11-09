package uk.ac.warwick.util.concurrency;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

import uk.ac.warwick.util.concurrency.TaskExecutionService;

public final class ImmediateCallableTest {
    
    @Test
    public void newInstance() throws Exception {
        TaskExecutionService service = new TaskExecutionService(1);
        Future<Integer> future = service.submit(ImmediateCallable.newInstance(7));
        assertNotNull(future);
        
        assertEquals(7, future.get().intValue());

        assertFalse(future.isCancelled());
        assertTrue(future.isDone());
    }
    
    @Test
    public void errorInstance() throws Exception {
        Exception ex = new IllegalStateException("Invalid");
        
        TaskExecutionService service = new TaskExecutionService(1);
        Future<Integer> future = service.submit(ImmediateCallable.<Integer>errorInstance(ex));
        assertNotNull(future);
        
        try {
            future.get();
            fail("Expected exception");
        } catch (ExecutionException e) { 
            assertEquals(ex, e.getCause());
        }
    }

}
