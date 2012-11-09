package uk.ac.warwick.util.collections;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.ac.warwick.util.collections.LazyList.Factory;

import com.google.common.collect.Lists;

public final class LazyListTest {
    
    private final Object value = new Object();
    
    private final Factory<Object> factory = new Factory<Object>() {
        public Object create() {
            return value;
        }
    };
    
    @Test
    public void getOutOfBounds() throws Exception {
        List<Object> original = Lists.newArrayList();
        LazyList<Object> lazy = LazyList.decorate(original, factory);
        
        for (int i=0; i<100; i++) {
            assertSame(value, lazy.get(i));
        }
    }
    
    @Test
    public void getInFutureFillsWithLazyFactory() throws Exception {
        List<Object> original = Lists.newArrayList();
        LazyList<Object> lazy = LazyList.decorate(original, factory);
        
        assertSame(value, lazy.get(100));
        
        for (int i=0; i<100; i++) {
            assertSame(value, lazy.get(i));
        }
    }

}
