package uk.ac.warwick.util.virusscan.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;

import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

@Named
@Singleton
public class AsyncHttpClientImpl implements AsyncHttpClient {

    private final CloseableHttpAsyncClient httpClient;

    public AsyncHttpClientImpl() {
        this.httpClient =
            HttpAsyncClients.custom()
                .setDefaultConnectionConfig(
                    ConnectionConfig.custom()
                        .setBufferSize(8192)
                        .setCharset(StandardCharsets.UTF_8)
                        .build()
                )
                .setDefaultRequestConfig(
                    RequestConfig.custom()
                        .setConnectTimeout(5000) // 5 seconds
                        .setSocketTimeout(300000) // 5 minutes
                        .setExpectContinueEnabled(true)
                        .setRedirectsEnabled(false)
                        .build()
                )
                .setMaxConnPerRoute(5) // Only allow 5 concurrent connections per host
                .build();

        start();
    }

    public void start() {
        httpClient.start();
    }

    @PreDestroy
    public void destroy() throws Exception {
        httpClient.close();
    }

    public boolean isRunning() {
        return httpClient.isRunning();
    }

    public Future<HttpResponse> execute(HttpUriRequest request, FutureCallback<HttpResponse> callback) {
        return httpClient.execute(request, callback);
    }

    @Override
    public HttpAsyncClient get() {
        return httpClient;
    }
}
