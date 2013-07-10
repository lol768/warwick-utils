package uk.ac.warwick.util;

import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Dispatcher;
import org.mortbay.jetty.servlet.WebApplicationHandler;
import org.mortbay.servlet.MultiPartFilter;
import org.mortbay.util.InetAddrPort;


public abstract class AbstractJUnit4JettyTest extends AbstractJUnit4FileBasedTest {
    private static final String SITEBUILDER_CONTENT_ENCODING = "ISO-8859-1";
    // SBTWO-6192 Pick a random port between 20000 and 30000
    protected static final int PORT = (int)(Math.random() * 10000) + 20000;
    protected String serverAddress = "http://localhost:" + PORT + "/";
    private static Server server;

    @BeforeClass
    public static void setupServer() throws Exception {
        System.setProperty("org.mortbay.util.URI.charset", SITEBUILDER_CONTENT_ENCODING);
        server = new Server();
        server.setTrace(true);
        server.addListener(new InetAddrPort(PORT));
    }

    @AfterClass
    public static void stopServer() throws Exception {
        server.stop();
        server = null;
        System.setProperty("org.mortbay.util.URI.charset", "UTF-8");
    }

    /**
     * Listen on an additional port. Use in @BeforeClass before starting the server.
     */
    protected static void addPort(int port) throws Exception {
        server.addListener(new InetAddrPort(port));
    }
     
    protected static void startServer(Map<String, String> pathToClassMap) throws Exception {
        WebApplicationHandler handler = new WebApplicationHandler();
        for (Map.Entry<String, String> entry: pathToClassMap.entrySet()) {
            handler.addServlet(entry.getKey(), entry.getValue());
        }
        
        handler.defineFilter("mpfilter", MultiPartFilter.class.getName());
        handler.addFilterPathMapping("*", "mpfilter",Dispatcher.__REQUEST);
        server.getContext("/").addHandler(handler);
        server.start();
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

}
