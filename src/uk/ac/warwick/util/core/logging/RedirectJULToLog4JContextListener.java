package uk.ac.warwick.util.core.logging;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;

import org.springframework.web.context.ContextLoaderListener;

import com.google.common.collect.Lists;

public final class RedirectJULToLog4JContextListener extends ContextLoaderListener {

    private Handler activeHandler;

    private List<Handler> oldHandlers = Lists.newArrayList();

    private Level handlerLevel = Level.FINE;

    private Level rootLevel = Level.INFO;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            JULToLog4JHandler.getTargetLogger(RedirectJULToLog4JContextListener.class).info(
                    "start(): Redirecting java.util.logging to Log4J...");
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            // remove old handlers
            for (Handler handler: rootLogger.getHandlers()) {
                oldHandlers.add(handler);
                rootLogger.removeHandler(handler);
            }
            // add our own
            activeHandler = new JULToLog4JHandler();
            activeHandler.setLevel(handlerLevel);
            rootLogger.addHandler(activeHandler);
            rootLogger.setLevel(rootLevel);
            // done, let's check it right away!!!

            Logger.getLogger(RedirectJULToLog4JContextListener.class.getName()).info("started: sending JDK log messages to Log4J");
        } catch (Exception exc) {
            JULToLog4JHandler.getTargetLogger(RedirectJULToLog4JContextListener.class).error("start() failed", exc);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.removeHandler(activeHandler);
        // Put all the old handlers back.
        for (Handler oldHandler: oldHandlers) {
            rootLogger.addHandler(oldHandler);
        }
        Logger.getLogger(JULToLog4JHandler.class.getName()).info("stopped");
    }

}
