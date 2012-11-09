package uk.ac.warwick.util.concurrency;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

public final class ImmediateFutureTest {
    
    @Test
    public void newInstance() throws Exception {
        Future<Integer> future = ImmediateFuture.of(7);
        assertNotNull(future);
        
        assertEquals(7, future.get().intValue());

        assertFalse(future.isCancelled());
        assertTrue(future.isDone());
    }
    
    @Test
    public void errorInstance() throws Exception {
        Exception ex = new IllegalStateException("Invalid");
        
        Future<Integer> future = ImmediateFuture.<Integer>error(ex);
        assertNotNull(future);
        
        try {
            future.get();
            fail("Expected exception");
        } catch (ExecutionException e) { 
            assertEquals(ex, e.getCause());
        }
    }

}
