package uk.ac.warwick.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Map;

/**
 * Adds support for firing up a Jetty jetty equipped with the servlet mappings of
 * your choice - perfect for unit testing something that needs to call a web service
 * without depending on the outside world.
 *
 * You will need your own @BeforeClass method where you call startServer, passing in
 * a map of servlets. Any other URL will 404. Then pass `serverAddress` to whatever
 * is making the requests.
 *
 * Extends @AbstractJUnit4FileBasedTest though it doesn't depend on it - I think it's
 * for some tests that needed support for both.
 */
public abstract class AbstractJUnit4JettyTest extends AbstractJUnit4FileBasedTest {

    private static final JettyServer jetty = new JettyServer();

    // For compatibility. Could update all the tests.
    protected String serverAddress = jetty.serverAddress;

    @BeforeClass
    public static void setupServer() throws Exception {
        jetty.setupServer();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        jetty.stopServer();
    }

    protected static void startServer(Map<String, String> pathToClassMap) throws Exception {
        jetty.startServer(pathToClassMap);
    }
}
