package uk.ac.warwick.util.httpclient.httpclient4;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.net.ProxySelector;
import java.nio.charset.Charset;

public final class MultiThreadedHttpClientFactory implements HttpClientFactory {

    public static final RequestConfig DEFAULT_REQUEST_CONFIG = RequestConfig.custom()
        .setConnectTimeout(30000) // 30 seconds
        .setSocketTimeout(30000) // 30 seconds
        .setExpectContinueEnabled(true)
        .setCircularRedirectsAllowed(true)
        .setRedirectsEnabled(true)
        .setMaxRedirects(10)
        .build();

    private static final MultiThreadedHttpClientFactory INSTANCE = new MultiThreadedHttpClientFactory();

    private final HttpClient client;

    public MultiThreadedHttpClientFactory() {
        ConnectionConfig connectionConfig =
            ConnectionConfig.custom()
                .setBufferSize(8192)
                .setCharset(Charset.forName("UTF-8"))
                .build();

        SocketConfig socketConfig =
            SocketConfig.custom()
                .setTcpNoDelay(true)
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        // Set the default concurrent connections per route to 5
        connectionManager.setDefaultMaxPerRoute(5);

        this.client =
            HttpClientBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .setDefaultRequestConfig(DEFAULT_REQUEST_CONFIG)
                .setDefaultSocketConfig(socketConfig)
                .setUserAgent("WarwickUtils HttpMethodExecutor, elab@warwick.ac.uk")
                .setConnectionManager(connectionManager)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(1, false)) // Retry each request once
                .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
                .build();
    }

    public HttpClient getClient() {
        return client;
    }

    public static MultiThreadedHttpClientFactory getInstance() {
        return INSTANCE;
    }

}
