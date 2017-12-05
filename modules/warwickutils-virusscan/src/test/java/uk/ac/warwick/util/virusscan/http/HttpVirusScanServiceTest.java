package uk.ac.warwick.util.virusscan.http;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.warwick.util.AbstractJUnit4JettyTest;
import uk.ac.warwick.util.JettyServer;
import uk.ac.warwick.util.virusscan.VirusScanResult;
import uk.ac.warwick.util.virusscan.VirusScanServiceStatus;
import uk.ac.warwick.util.virusscan.conf.Configuration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class HttpVirusScanServiceTest extends AbstractJUnit4JettyTest {

    // Some virus inputs that the servlets listen for and change their behaviour based on
    private static final byte[] CLEAN_INPUT = "clean".getBytes(StandardCharsets.UTF_8);
    private static final byte[] VIRUS_INPUT = "virus".getBytes(StandardCharsets.UTF_8);
    private static final byte[] ERROR_INPUT = "error".getBytes(StandardCharsets.UTF_8);

    private static final String EXPECTED_API_KEY = "expected";
    private static final String BACKEND_DOWN_API_KEY = "backend-down";
    private static final String UNAVAILABLE_API_KEY = "unavailable";

    private MutableConfiguration configuration = new MutableConfiguration();

    private HttpVirusScanService service = new HttpVirusScanService(new AsyncHttpClientImpl(), configuration);

    @BeforeClass
    public static void setupServlets() throws Exception {
        startServer(new HashMap<String, String>() {{
            put("/scan", ScanServlet.class.getName());
            put("/service/healthcheck", HealthcheckServlet.class.getName());
        }});
    }

    @Before
    public void setup() throws Exception {
        configuration.setApiHost(serverAddress);
        configuration.setApiKey(EXPECTED_API_KEY); // Can be overridden for other tests to mimic behaviour
    }

    @Test(timeout = 1000L)
    public void scanClean() throws Exception {
        ByteSource input = ByteSource.wrap(CLEAN_INPUT);
        VirusScanResult result = service.scan(input).get();

        assertEquals(VirusScanResult.Status.clean, result.getStatus());
        assertFalse(result.getError().isPresent());
        assertFalse(result.getVirus().isPresent());
    }

    @Test(timeout = 1000L)
    public void scanVirus() throws Exception {
        ByteSource input = ByteSource.wrap(VIRUS_INPUT);
        VirusScanResult result = service.scan(input).get();

        assertEquals(VirusScanResult.Status.virus, result.getStatus());
        assertFalse(result.getError().isPresent());
        assertTrue(result.getVirus().isPresent());
        assertEquals("Cyrus", result.getVirus().get());
    }

    @Test(timeout = 1000L)
    public void scanError() throws Exception {
        ByteSource input = ByteSource.wrap(ERROR_INPUT);
        VirusScanResult result = service.scan(input).get();

        assertEquals(VirusScanResult.Status.error, result.getStatus());
        assertTrue(result.getError().isPresent());
        assertEquals("There was a problem checking the input", result.getError().get());
        assertFalse(result.getVirus().isPresent());
    }

    @Test(timeout = 1000L)
    public void scanBackendDown() throws Exception {
        configuration.setApiKey(BACKEND_DOWN_API_KEY);

        ByteSource input = ByteSource.wrap(CLEAN_INPUT);
        VirusScanResult result = service.scan(input).get();

        assertEquals(VirusScanResult.Status.error, result.getStatus());
        assertTrue(result.getError().isPresent());
        assertEquals("Error connecting to ClamAV service", result.getError().get());
        assertFalse(result.getVirus().isPresent());
    }

    @Test(timeout = 1000L, expected = ExecutionException.class)
    public void scanUnavailable() throws Exception {
        configuration.setApiKey(UNAVAILABLE_API_KEY);

        ByteSource input = ByteSource.wrap(CLEAN_INPUT);
        service.scan(input).get();
    }

    @Test(timeout = 1000L)
    public void statusAvailable() throws Exception {
        VirusScanServiceStatus status = service.status().get();
        assertTrue(status.isAvailable());
        assertEquals("ClamAV available", status.getStatusMessage());
    }

    @Test(timeout = 1000L)
    public void statusBackendDown() throws Exception {
        configuration.setApiKey(BACKEND_DOWN_API_KEY);

        VirusScanServiceStatus status = service.status().get();
        assertFalse(status.isAvailable());
        assertEquals("Error connecting to ClamAV service", status.getStatusMessage());
    }

    @Test(timeout = 1000L)
    public void statusUnavailable() throws Exception {
        configuration.setApiKey(UNAVAILABLE_API_KEY);

        VirusScanServiceStatus status = service.status().get();
        assertFalse(status.isAvailable());
        assertEquals("Error connecting to virus scan service", status.getStatusMessage());
    }

    public static class ScanServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String apiKey = req.getHeader("Authorization");
            assertTrue(apiKey.toLowerCase().startsWith("api-key "));
            apiKey = apiKey.substring(8);

            if (apiKey.equals(EXPECTED_API_KEY)) {
                byte[] input = ByteStreams.toByteArray(req.getInputStream());
                if (Arrays.equals(input, CLEAN_INPUT)) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"status\":\"clean\"}");
                } else if (Arrays.equals(input, VIRUS_INPUT)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"status\":\"virus\",\"virus\":\"Cyrus\"}");
                } else if (Arrays.equals(input, ERROR_INPUT)) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write("{\"status\":\"error\",\"error\":\"There was a problem checking the input\"}");
                }
            } else if (apiKey.equals(BACKEND_DOWN_API_KEY)) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"status\":\"error\",\"error\":\"Error connecting to ClamAV service\"}");
            } else if (apiKey.equals(UNAVAILABLE_API_KEY)) {
                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        }
    }

    public static class HealthcheckServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String apiKey = req.getHeader("Authorization");
            assertTrue(apiKey.toLowerCase().startsWith("api-key "));
            apiKey = apiKey.substring(8);

            if (apiKey.equals(EXPECTED_API_KEY)) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"data\":[{\"name\":\"clamav\",\"status\":\"okay\",\"message\":\"ClamAV available\",\"testedAt\":\"2017-03-16T17:11:12.005Z\"}]}");
            } else if (apiKey.equals(BACKEND_DOWN_API_KEY)) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"data\":[{\"name\":\"clamav\",\"status\":\"error\",\"message\":\"Error connecting to ClamAV service\",\"testedAt\":\"2017-03-16T17:11:12.005Z\"}]}");
            } else if (apiKey.equals(UNAVAILABLE_API_KEY)) {
                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        }
    }

    private static class MutableConfiguration implements Configuration {
        private String apiHost;
        private String apiKey;

        @Override
        public String getApiHost() {
            return apiHost;
        }

        public void setApiHost(String apiHost) {
            this.apiHost = apiHost;
        }

        @Override
        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

}