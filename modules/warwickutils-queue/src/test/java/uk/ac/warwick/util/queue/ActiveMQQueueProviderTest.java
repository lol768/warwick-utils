package uk.ac.warwick.util.queue;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.util.queue.activemq.ActiveMQQueueProvider;
import uk.ac.warwick.util.queue.conversion.ItemType;
import uk.ac.warwick.util.queue.conversion.JsonMessageConverter;
import uk.ac.warwick.util.queue.conversion.JsonObjectConverter;
import uk.ac.warwick.util.queue.conversion.SimpleFieldConverter;

/**
 * Tests programmatic creation of the default queue provider, sending
 * and receiving messages, and defining message converters to serialize to JSON.
 */
public class ActiveMQQueueProviderTest {
    
    private QueueProvider queueProvider;
    private Queue queue;
    private Queue unrelatedQueue;
    
    @Before public void create() throws Exception {     
        queueProvider = ActiveMQQueueProvider.createEmbeddedBroker();
        
        queue = queueProvider.getQueue("Test.Messages");
        unrelatedQueue = queueProvider.getQueue("Test.SomeOtherMessages");
    }
    
    @Test public void sendStringMessage() throws Exception {        
        MyListener listener = new MyListener();
        
        // default message converter will convert to and from strings.
        queue.addListener((String)null, listener);
        
        queue.send("Hello from JUnit");
        queue.send("Hello from JUnit again");
        Thread.sleep(300);
        
        assertEquals(2, listener.getMessagesReceived());
    }
    
    @Test public void sendJsonMessage() throws Exception {
        JsonMessageConverter jsonConverter = new JsonMessageConverter();
        Map<String, JsonObjectConverter> converters = new HashMap<String, JsonObjectConverter>();
        converters.put("EncodeVideoJob", new SimpleFieldConverter(EncodeVideoJob.class, asList("filename", "bitrate", "format")));
        converters.put("GrabMetadataJob", new SimpleFieldConverter(GrabMetadataJob.class, asList("pageUrl")));
        jsonConverter.setObjectConverters(converters);
        queue.setMessageConverter(jsonConverter);
        
        unrelatedQueue.send("Hello!");
        
        Mockery m = new JUnit4Mockery();

        final QueueListener listener = m.mock(QueueListener.class);
        m.checking(new Expectations(){{
            exactly(1).of(listener).isListeningToQueue(); will(returnValue(true));
            one(listener).onReceive(with(any(EncodeVideoJob.class)));
            one(listener).onReceive(with(any(GrabMetadataJob.class)));
        }});
        queue.setSingleListener(listener);
        
        EncodeVideoJob encode = new EncodeVideoJob();
        encode.setFilename("myfile.mp4");
        encode.setBitrate(9000);
        encode.setFormat("H264");
        queue.send(encode);
        
        GrabMetadataJob metadata = new GrabMetadataJob();
        metadata.setPageUrl("/services/its/myfile.mp4");
        queue.send(metadata);
        
        Thread.sleep(300);
      
        m.assertIsSatisfied();
    }
    
    @Test public void nonListeningListener() throws Exception {
        Mockery m = new JUnit4Mockery();

        final QueueListener listener = m.mock(QueueListener.class);
        m.checking(new Expectations(){{
            exactly(1).of(listener).isListeningToQueue(); will(returnValue(false));
        }});
        queue.setSingleListener(listener);
        
        queue.send("Pow!");
        
        Thread.sleep(300);
        m.assertIsSatisfied();
    }
    
    @After public void destroy() throws Exception {    
        queueProvider.destroy();
    }
    
    @AfterClass public static void checkEmbeddedProvider() {
        assertFalse( "Embedded queue shouldn't create activemq-data directory", 
                new File("activemq-data").exists() );
    }
    
    public static class EncodeVideoJob {
        private String filename;
        private String format;
        private int bitrate;
        public String getFilename() {
            return filename;
        }
        public void setFilename(String filename) {
            this.filename = filename;
        }
        public String getFormat() {
            return format;
        }
        public void setFormat(String format) {
            this.format = format;
        }
        public int getBitrate() {
            return bitrate;
        }
        public void setBitrate(int bitrate) {
            this.bitrate = bitrate;
        }
    }
    
    @ItemType("GrabMetadataJob")
    @JsonAutoDetect
    public static class GrabMetadataJob {
        private String pageUrl;
        public String getPageUrl() {
            return pageUrl;
        }
        public void setPageUrl(String pageUrl) {
            this.pageUrl = pageUrl;
        }
    }
    
    public static class MyListener implements QueueListener {
        private int messagesReceived;
        public void onReceive(Object message) {
            messagesReceived++;
        }
        public int getMessagesReceived() {
            return messagesReceived;
        }
        public boolean isListeningToQueue() {
            return true;
        }
        
    }
}
