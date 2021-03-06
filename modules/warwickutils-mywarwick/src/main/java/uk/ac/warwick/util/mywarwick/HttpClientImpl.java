package uk.ac.warwick.util.mywarwick;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import uk.ac.warwick.util.mywarwick.model.Configuration;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

@Named
@Singleton
public class HttpClientImpl implements HttpClient {

    private final CloseableHttpAsyncClient httpClient;

    @Inject
    public HttpClientImpl(Configuration config) {
        this.httpClient =
            HttpAsyncClients.custom()
                .setDefaultConnectionConfig(
                    ConnectionConfig.custom()
                        .setBufferSize(8192)
                        .setCharset(StandardCharsets.UTF_8)
                        .build()
                )
                .setDefaultRequestConfig(getRequestConfig())
                .setMaxConnPerRoute(config.getHttpMaxConnPerRoute())
                .setMaxConnTotal(config.getHttpMaxConn())
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
    public RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(2000) // 2 seconds (on first attempt)
                .setSocketTimeout(2000) // 2 seconds (on first attempt)
                .setExpectContinueEnabled(true)
                .setRedirectsEnabled(false)
                .build();
    }

    @Override
    public HttpAsyncClient get() {
        return httpClient;
    }
}
