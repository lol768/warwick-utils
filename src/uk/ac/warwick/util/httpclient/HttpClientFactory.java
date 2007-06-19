package uk.ac.warwick.util.httpclient;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

public final class HttpClientFactory {
    
    private static final MultiThreadedHttpConnectionManager CONNECTION_MANAGER = new MultiThreadedHttpConnectionManager();
    
    private static final HttpClient CLIENT = new HttpClient(CONNECTION_MANAGER);
    
    public static HttpClient getClient() {
        return CLIENT;
    }

}
