package uk.ac.warwick.util.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;


public class LoggerTest {
	private static final Logger LOGGER = Logger.getLogger();
    private static final TestLogger TEST_LOGGER = TestLoggerFactory.getTestLogger("uk.ac.warwick.util.core.LoggerTest");

	@Test public void logging() {
		assertEquals("uk.ac.warwick.util.core.LoggerTest", LOGGER.getName());

		LOGGER.info("Loading %s", "FTP Subsystem");
		LOGGER.error("Failed");

        LoggingEvent event1 = TEST_LOGGER.getLoggingEvents().get(0);
        assertEquals(Level.INFO, event1.getLevel());
        assertEquals("Loading FTP Subsystem", event1.getMessage());

        LoggingEvent event2 = TEST_LOGGER.getLoggingEvents().get(1);
        assertEquals(Level.ERROR, event2.getLevel());
        assertEquals("Failed", event2.getMessage());
	}
}
