package uk.ac.warwick.util.queue.conversion;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import javax.jms.Session;
import javax.jms.TextMessage;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.action.CustomAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("service-beans.xml")
public class JsonMessageConverterTest {
    
    private Mockery m = new JUnit4Mockery();
    
    private JsonMessageConverter converter;
    
    @Test public void autowiring() throws Exception {
        
        final TestMessage myObject = new TestMessage();
        myObject.setAge(900);
        myObject.setName("Aska");
        
        final Session session = m.mock(Session.class);
        m.checking(new Expectations(){{
            oneOf(session).createTextMessage(with(any(String.class))); 
                will(returnTextMessage().expectingProperty("itemType", "my-message-type"));
        }});
        
        TextMessage message = (TextMessage) converter.toMessage(myObject, session);
        String text = message.getText();
        
        assertThat( text, allOf(containsString("age"), not(containsString("testServiceBean"))));
        
        TestMessage recreatedObject = (TestMessage) converter.fromMessage(message);
        assertThat( recreatedObject, hasProperty("age", is(900)));
        assertThat( recreatedObject, hasProperty("testServiceBean", is(not(nullValue()))));
    }
    
    private TextMessageAction returnTextMessage() {
        return new TextMessageAction();
    }
    
    private class TextMessageAction extends CustomAction {
        public TextMessageAction() {
            super("return TextMessage");
        }
        final TextMessage t = m.mock(TextMessage.class);
        public Object invoke(final Invocation invocation) throws Throwable {
            m.checking(new Expectations(){{
                allowing(t).getText(); will(returnValue(invocation.getParameter(0)));
            }});
            return t;
        }
        public TextMessageAction expectingProperty(final String name, final String value) throws Exception {
            m.checking(new Expectations(){{
                oneOf(t).setStringProperty(name, value);
                allowing(t).getStringProperty(name); will(returnValue(value));
            }});
            return this;
        }
    }

    @Autowired
    public void setConverter(JsonMessageConverter converter) {
        this.converter = converter;
    }
}
