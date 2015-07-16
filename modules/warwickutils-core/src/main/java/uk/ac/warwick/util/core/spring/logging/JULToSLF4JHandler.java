package uk.ac.warwick.util.core.spring.logging;

import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JULI (java.util.logging) handler that redirects java.util.logging messages to SLF4J
 * http://wiki.apache.org/myfaces/Trinidad_and_Common_Logging
 */
public final class JULToSLF4JHandler extends Handler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JULToSLF4JHandler.class);

    @Override
    public void publish(LogRecord record) {
        Logger slf4j = getTargetLogger(record.getLoggerName());

        if (Level.SEVERE == record.getLevel()) {
            slf4j.error(toSLF4jMessage(record), record.getThrown());
        } else if (Level.WARNING == record.getLevel()) {
            slf4j.warn(toSLF4jMessage(record), record.getThrown());
        } else if (Level.INFO == record.getLevel()) {
            slf4j.info(toSLF4jMessage(record), record.getThrown());
        } else if (Level.FINE == record.getLevel()) {
            slf4j.debug(toSLF4jMessage(record), record.getThrown());
        }
    }

    static Logger getTargetLogger(String loggerName) {
        return LoggerFactory.getLogger(loggerName);
    }
    
    static Logger getTargetLogger(Class<?> loggerName) {
        return LoggerFactory.getLogger(loggerName);
    }
    
    private String toSLF4jMessage(LogRecord record) {
        String message = record.getMessage();
        // Format message
        try {
            Object[] parameters = record.getParameters();
            if (parameters != null && parameters.length != 0) {
                // Check for the first few parameters ?
                if (message.indexOf("{0}") >= 0 
                 || message.indexOf("{1}") >= 0
                 || message.indexOf("{2}") >= 0
                 || message.indexOf("{3}") >= 0) {
                    message = MessageFormat.format(message, parameters);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Could not write log to log4j", ex);
        }
        
        return message;
    }

    @Override
    public void flush() {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to do
    }
}
