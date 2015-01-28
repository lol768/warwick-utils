package uk.ac.warwick.util.queue;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.warwick.util.queue.ActiveMQQueueProviderTest.EncodeVideoJob;
import uk.ac.warwick.util.queue.ActiveMQQueueProviderTest.GrabMetadataJob;
import uk.ac.warwick.util.queue.activemq.ActiveMQQueueProvider;
import uk.ac.warwick.util.queue.conversion.AnnotationJsonObjectConverter;

/**
 * Tests the default queue provider in embedded mode, in a 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"test-queue-context.xml"})
public class ActiveMQQueueProviderSpringTest {
    
    @Autowired
    private ActiveMQQueueProvider queueProvider;
    
    @Resource(name="sitebuilderJobQueue")
    private Queue sitebuilderQueue;
    
    @Resource(name="unimportantStuffQueue")
    private Queue unimportantStuffQueue;
    
    @Test public void spring() {
        assertNotNull(queueProvider);
    }
    
    @DirtiesContext
    @Test public void annotationConverter() throws Exception {
        Mockery m = new JUnit4Mockery();
        
        // GrabMetadataJob's mapping is defined through annotations.
        GrabMetadataJob job = new GrabMetadataJob();
        job.setPageUrl("/services/wibble.ogg");
        
        final QueueListener listener = m.mock(QueueListener.class);
        m.checking(new Expectations(){{
            exactly(1).of(listener).isListeningToQueue(); will(returnValue(true));
            exactly(1).of(listener).onReceive(with(hasProperty("pageUrl", equal("/services/wibble.ogg"))));
        }});
        sitebuilderQueue.setSingleListener(listener);
      
        sitebuilderQueue.send(job);
        Thread.sleep(300);
        
        m.assertIsSatisfied();
    }
    
    @SuppressWarnings("unchecked")
    @DirtiesContext
    @Test public void moreAnnotationConversion() throws Exception {
        Mockery m = new JUnit4Mockery();
        
        final QueueListener listener = m.mock(QueueListener.class);
        m.checking(new Expectations(){{
            exactly(1).of(listener).isListeningToQueue(); will(returnValue(true));
            exactly(1).of(listener).onReceive(with(allOf(
                    any((SendForHelp.class)),
                    hasProperty("from", equal("alan@introuble.example.com")),
                    hasProperty("to", equal("bill@savetheday.example.com")),
                    hasProperty("transientData", is(nullValue()))
            )));
        }});
        unimportantStuffQueue.setSingleListener(listener);
        
        SendForHelp help = new SendForHelp();
        help.setFrom("alan@introuble.example.com");
        help.setTo("bill@savetheday.example.com");
        help.setTransientData("This shouldn't get stored in the message.");
        unimportantStuffQueue.send(help);
        
        Thread.sleep(300);
        
        m.assertIsSatisfied();
    }
}
