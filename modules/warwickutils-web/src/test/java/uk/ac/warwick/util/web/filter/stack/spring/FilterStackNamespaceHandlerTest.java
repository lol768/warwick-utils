package uk.ac.warwick.util.web.filter.stack.spring;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.ac.warwick.util.web.filter.stack.ConfigurableFilterStack;
import uk.ac.warwick.util.web.filter.stack.FilterStackSet;

@Ignore("don't know why maven hates this")
public class FilterStackNamespaceHandlerTest {
    
    @Test 
    public void filterStack() {
        BeanFactory f = new ClassPathXmlApplicationContext("/uk/ac/warwick/util/web/filter/stack/spring/filter-stack.xml");
        
        ConfigurableFilterStack stack = (ConfigurableFilterStack) f.getBean("uberFilter");
        assertNotNull(stack);
        assertEquals(2, stack.getFilterSets().size());
    }
    
    @Test
    public void filterStackWithInheritance() {
        BeanFactory f = new ClassPathXmlApplicationContext("/uk/ac/warwick/util/web/filter/stack/spring/filter-stack-inheritance.xml");
        
        ConfigurableFilterStack stack = (ConfigurableFilterStack) f.getBean("uberFilter");
        assertNotNull(stack);
        assertEquals(3, stack.getFilterSets().size());
        assertEquals("", stack.getFilterSets().get(0).getName());
        assertEquals("extendMe", stack.getFilterSets().get(1).getName());
        assertEquals("", stack.getFilterSets().get(2).getName());
        
        FilterStackSet extended = stack.getFilterSets().get(1);
        
        assertEquals(3, extended.getFilters().size());
        assertTrue(extended.isMatch("/edit/yes"));
        assertTrue(extended.isMatch("/admin/yes"));
        assertFalse(extended.isMatch("/render/yes"));
    }
    
    @Test
    public void filterStackWithInheritance2() {
        BeanFactory f = new ClassPathXmlApplicationContext("/uk/ac/warwick/util/web/filter/stack/spring/filter-stack-inheritance2.xml");
        
        ConfigurableFilterStack stack = (ConfigurableFilterStack) f.getBean("uberFilter");
        assertNotNull(stack);
        assertEquals(3, stack.getFilterSets().size());
        assertEquals("", stack.getFilterSets().get(0).getName());
        assertEquals("extendMe", stack.getFilterSets().get(1).getName());
        assertEquals("", stack.getFilterSets().get(2).getName());
        
        FilterStackSet extended = stack.getFilterSets().get(1);
        
        assertEquals(2, extended.getFilters().size());
        assertTrue(extended.isMatch("/edit/yes"));
        assertFalse(extended.isMatch("/edit/api/yes"));
        assertTrue(extended.isMatch("/1/yes"));
        assertTrue(extended.isMatch("/2/yes"));
        assertTrue(extended.isMatch("/3/yes"));
        assertTrue(extended.isMatch("/4/yes"));
        assertTrue(extended.isMatch("/5/yes"));
    }
    
}
