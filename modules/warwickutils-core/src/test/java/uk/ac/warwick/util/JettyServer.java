package uk.ac.warwick.util;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Dispatcher;
import org.mortbay.jetty.servlet.WebApplicationHandler;
import org.mortbay.servlet.MultiPartFilter;
import org.mortbay.util.InetAddrPort;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Pulled out of an abstract class, so you can use it on its own.
 *
 * Starts and runs a Jetty server. Simplest to use the {@link #running(Map, Callable)}
 * method, as it handles setup
 *
 * <pre>
 *     JettyServer jetty = new JettyServer();
 *     jetty.running(servlets, () -> {
 *        // your test code, referencing jetty.serverAddress
 *     });
 * </pre>
 */
public class JettyServer {
    private static final String SITEBUILDER_CONTENT_ENCODING = "UTF-8";

    // SBTWO-6192 Pick a random port between 20000 and 30000
    private final int port = (int)(Math.random() * 10000) + 20000;
    public final String serverAddress = "http://localhost:" + port + "/";
    private Server server;

    public void setupServer() throws Exception {
        System.setProperty("org.mortbay.util.URI.charset", SITEBUILDER_CONTENT_ENCODING);
        server = new Server();
        server.setTrace(true);
        server.addListener(new InetAddrPort(port));
    }

    public void stopServer() throws Exception {
        server.stop();
        server = null;
        System.setProperty("org.mortbay.util.URI.charset", "UTF-8");
    }

    /**
     * Listen on an additional port. Use in @BeforeClass before starting the server.
     */
    public void addPort(int port) throws Exception {
        server.addListener(new InetAddrPort(port));
    }

    public void startServer(Map<String, String> pathToClassMap) throws Exception {
        WebApplicationHandler handler = new WebApplicationHandler();
        for (Map.Entry<String, String> entry: pathToClassMap.entrySet()) {
            handler.addServlet(entry.getKey(), entry.getValue());
        }

        handler.defineFilter("mpfilter", MultiPartFilter.class.getName());
        handler.addFilterPathMapping("*", "mpfilter", Dispatcher.__REQUEST);
        server.getContext("/").addHandler(handler);
        server.start();
    }

    public <V> V running(Map<String, String> pathToClassMap, Callable<V> fn) throws Exception {
        try {
            if (server == null) {
                setupServer();
            }
            startServer(pathToClassMap);
            return fn.call();
        } finally {
            if (server != null) {
                stopServer();
            }
        }
    }

    @SuppressWarnings("serial")
    public static abstract class StatusCodeSettingServlet extends HttpServlet {

        public static int executionCount = 0;

        abstract int getCode();

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
            resp.setStatus(getCode());

            executionCount++;
        }
    }

    @SuppressWarnings("serial")
    public static class OKServlet extends StatusCodeSettingServlet {
        @Override
        int getCode() {
            return HttpServletResponse.SC_OK;
        }
    }

    @SuppressWarnings("serial")
    public static class NotFoundServlet extends StatusCodeSettingServlet {
        @Override
        int getCode() {
            return HttpServletResponse.SC_NOT_FOUND;
        }
    }

    @SuppressWarnings("serial")
    public static class MovedTemporarilyServlet extends StatusCodeSettingServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
            super.doGet(req, resp);

            resp.addHeader("Location", "http://www.google.com");
        }

        @Override
        int getCode() {
            return HttpServletResponse.SC_MOVED_TEMPORARILY;
        }
    }

    @SuppressWarnings("serial")
    public static class MovedPermanentlyServlet extends StatusCodeSettingServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
            super.doGet(req, resp);

            resp.addHeader("Location", "http://www.google.com");
        }

        @Override
        int getCode() {
            return HttpServletResponse.SC_MOVED_PERMANENTLY;
        }
    }

    @SuppressWarnings("serial")
    public static class GoneServlet extends StatusCodeSettingServlet {
        @Override
        int getCode() {
            return HttpServletResponse.SC_GONE;
        }
    }

    @SuppressWarnings("serial")
    public static class ForbiddenServlet extends StatusCodeSettingServlet {
        @Override
        int getCode() {
            return HttpServletResponse.SC_FORBIDDEN;
        }
    }

    @SuppressWarnings("serial")
    public static class UnauthorizedServlet extends StatusCodeSettingServlet {
        @Override
        int getCode() {
            return HttpServletResponse.SC_UNAUTHORIZED;
        }
    }

    @SuppressWarnings("serial")
    public static class PaymentRequiredServlet extends StatusCodeSettingServlet {
        @Override
        int getCode() {
            return HttpServletResponse.SC_PAYMENT_REQUIRED;
        }
    }

    @SuppressWarnings("serial")
    public static class ServiceUnavailableServlet extends StatusCodeSettingServlet {
        @Override
        int getCode() {
            return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        }
    }

    @SuppressWarnings("serial")
    public static class GatewayTimeoutServlet extends StatusCodeSettingServlet {
        @Override
        int getCode() {
            return HttpServletResponse.SC_GATEWAY_TIMEOUT;
        }
    }

    public static class SlowServlet extends StatusCodeSettingServlet {
        @Override
        int getCode() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Stop interrupting me", e);
            }
            return HttpServletResponse.SC_GATEWAY_TIMEOUT;
        }
    }
}
