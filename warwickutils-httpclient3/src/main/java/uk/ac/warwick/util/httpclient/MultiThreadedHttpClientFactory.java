package uk.ac.warwick.util.httpclient;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

public final class MultiThreadedHttpClientFactory implements HttpClientFactory {
    
    private static final MultiThreadedHttpConnectionManager CONNECTION_MANAGER = new MultiThreadedHttpConnectionManager();
    
    private static final MultiThreadedHttpClientFactory INSTANCE = new MultiThreadedHttpClientFactory();
    
    private final HttpClient client = new HttpClient(CONNECTION_MANAGER);
    
    public HttpClient getClient() {
        return client;
    }
    
    public static MultiThreadedHttpClientFactory getInstance() {
        return INSTANCE;
    }

}
