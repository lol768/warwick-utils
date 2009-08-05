package uk.ac.warwick.util.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.springframework.util.StopWatch;


public class LoggerTest {
	private static final Logger LOGGER = Logger.getLogger();
	
	private static final org.apache.log4j.Logger LOGGER2 = org.apache.log4j.Logger.getLogger(LoggerTest.class); 
	
//	@Test public void speed() {
//		final List<String> messages = new ArrayList<String>();
//		
//		for(int i=0; i<100000;i++) {
//			LOGGER2.info("Message");
//			LOGGER.info("MSAASFE");
//		}
//		StopWatch w = new StopWatch();
//		
//		w.start("log5j");
//		for(int i=0; i<1000000;i++) {
//			LOGGER.info("Message %d", i);
//		}
//		w.stop();
//		
//		w.start("log4j");
//		for(int i=0; i<1000000;i++) {
//			LOGGER2.info("Message " + i);
//		}
//		w.stop();
//		
//		System.out.println(w.prettyPrint());
//	}
	
	@Test public void logging() {
		final List<String> messages = new ArrayList<String>();
		assertEquals("uk.ac.warwick.util.core.LoggerTest", LOGGER.getName());
		LOGGER.addAppender(new Appender() {
			public void setName(String arg0) {}
			
			public void setLayout(Layout arg0) {}
			
			public void setErrorHandler(ErrorHandler arg0) {}
			
			public boolean requiresLayout() {return false;}
			
			public String getName() { return "Hello"; }
			
			public Layout getLayout() {
				return null;
			}
			
			public Filter getFilter() {
				return null;
			}
			
			public ErrorHandler getErrorHandler() {
				return null;
			}
			
			public void doAppend(LoggingEvent arg0) {
				messages.add("[" + arg0.getLevel().toString() + "] " + arg0.getMessage().toString());
			}
			
			public void close() {}
			
			public void clearFilters() {}
			
			public void addFilter(Filter arg0) {};
		});
		LOGGER.info("Loading %s", "FTP Subsystem");
		LOGGER.error("Failed");
		assertEquals("[INFO] Loading FTP Subsystem", messages.get(0));
		assertEquals("[ERROR] Failed", messages.get(1));
	}
}
