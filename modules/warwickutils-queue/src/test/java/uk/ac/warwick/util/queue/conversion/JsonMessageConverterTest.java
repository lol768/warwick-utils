package uk.ac.warwick.util.queue.conversion;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import javax.jms.Session;
import javax.jms.TextMessage;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.action.CustomAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="service-beans.xml")
public class JsonMessageConverterTest {
    
    private Mockery m = new JUnit4Mockery();
    
    // Specified in service-beans.xml - will have BeanFactory set on it
    private JsonMessageConverter converter;
    
    @Test public void autowiring() throws Exception {
        
        final TestItem myObject = new TestItem("Aska",900)
            .addChild( new TestItem("Sasha", 300 ) )
            .addChild( new TestItem("Vaska", 350 )
                        .addChild( new TestItem("Danuta", 200 )));
        
        
        
        final Session session = m.mock(Session.class);
        m.checking(new Expectations(){{
            oneOf(session).createTextMessage(with(any(String.class))); 
                will(returnTextMessage().expectingProperty("itemType", "my-message-type"));
        }});
        
        TextMessage message = (TextMessage) converter.toMessage(myObject, session);
        String text = message.getText();
        
        System.err.println(text);
        
        assertThat( text, allOf(containsString("age"), not(containsString("testServiceBean"))));
        
        TestItem recreatedObject = (TestItem) converter.fromMessage(message);
        assertThat( recreatedObject, hasProperty("age", is(900)));
        assertThat( recreatedObject, hasProperty("testServiceBean", is(not(nullValue()))));
        
        // deep wiring
        assertThat( recreatedObject.getChildren().get(0), hasProperty("testServiceBean", is(not(nullValue()))));
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
