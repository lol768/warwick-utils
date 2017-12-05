package uk.ac.warwick.util.mywarwick;

import com.google.common.util.concurrent.Futures;
import com.typesafe.config.ConfigFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.JettyServer;
import uk.ac.warwick.util.mywarwick.model.TypesafeConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class HttpClientImplTest {
    HttpClientImpl client = new HttpClientImpl(new TypesafeConfiguration(ConfigFactory.empty()));

    @Before
    public void setup() {
        client.start();
    }

    @After
    public void teardown() throws Exception {
        client.destroy();
    }

    /**
     * We have been seeing socket timeout exceptions
     */
    @Test(timeout=20000L)
    public void parallelismTimeouts() throws Exception {
        JettyServer jetty = new JettyServer();
        Map<String, String> mappings = Collections.singletonMap(
            "/", JettyServer.SlowServlet.class.getName()
        );
        jetty.running(mappings, () -> {
            String url = jetty.serverAddress;
            List<Future<HttpResponse>> futures = IntStream.range(0, 100).mapToObj(i ->
                    client.execute(new HttpGet(url), null)
            ).collect(Collectors.toList());

            List<HttpResponse> responses = futures.stream().map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    fail("Failed response: " + e);
                    return null;
                }
            }).collect(Collectors.toList());

            System.out.println("Got "+ responses.size() + " responses");
        });
    }
}