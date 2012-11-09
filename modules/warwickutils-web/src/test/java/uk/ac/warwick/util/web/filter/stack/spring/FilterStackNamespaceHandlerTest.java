package uk.ac.warwick.util.web.filter.stack.spring;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.ac.warwick.util.web.filter.stack.ConfigurableFilterStack;

public class FilterStackNamespaceHandlerTest {
    
    @Test 
    @Ignore("don't know why maven hates this")
    public void filterStack() {
        BeanFactory f = new ClassPathXmlApplicationContext("/uk/ac/warwick/util/web/filter/stack/spring/filter-stack.xml");
        
        ConfigurableFilterStack stack = (ConfigurableFilterStack) f.getBean("uberFilter");
        assertNotNull(stack);
        assertEquals(2, stack.getFilterSets().size());
    }
    
}
