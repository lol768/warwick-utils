package uk.ac.warwick.util.mywarwick;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpClientImplTest {
    HttpClientImpl client = new HttpClientImpl();

    @Before
    public void setup() {
        client.start();
    }

    @After
    public void teardown() throws Exception {
        client.destroy();
    }

    @Test
    public void parallelismTimeouts() throws Exception {

    }
}