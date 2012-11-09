package uk.ac.warwick.util.core.logging;

import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.log4j.Logger;

/**
 * A JULI (java.util.logging) handler that redirects java.util.logging messages to Log4J
 * http://wiki.apache.org/myfaces/Trinidad_and_Common_Logging
 */
public final class JULToLog4JHandler extends Handler {
    
    private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(JULToLog4JHandler.class);

    @Override
    public void publish(LogRecord record) {
        org.apache.log4j.Logger log4j = getTargetLogger(record.getLoggerName());
        org.apache.log4j.Level level = toLog4j(record.getLevel());
        if (level != org.apache.log4j.Level.OFF) {
        	log4j.log(level, toLog4jMessage(record), record.getThrown());
        }
    }

    static Logger getTargetLogger(String loggerName) {
        return Logger.getLogger(loggerName);
    }
    
    static Logger getTargetLogger(Class<?> loggerName) {
        return Logger.getLogger(loggerName);
    }
    
    private String toLog4jMessage(LogRecord record) {
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

    private org.apache.log4j.Level toLog4j(Level level) {
        //converts levels
        org.apache.log4j.Level log4jLevel;
        
        if (Level.SEVERE == level) {
            log4jLevel = org.apache.log4j.Level.ERROR;
        } else if (Level.WARNING == level) {
            log4jLevel = org.apache.log4j.Level.WARN;
        } else if (Level.INFO == level) {
            log4jLevel = org.apache.log4j.Level.INFO;
        } else if (Level.FINE == level) {
            log4jLevel = org.apache.log4j.Level.DEBUG;
        } else if (Level.OFF == level) {
            log4jLevel = org.apache.log4j.Level.OFF;
        } else {
            log4jLevel = org.apache.log4j.Level.OFF;
        }
        
        return log4jLevel;
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
