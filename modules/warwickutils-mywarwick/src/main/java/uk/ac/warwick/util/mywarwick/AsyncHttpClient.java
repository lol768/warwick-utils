package uk.ac.warwick.util.mywarwick;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import javax.inject.Named;
import java.util.concurrent.Future;

@Named
public class AsyncHttpClient implements HttpAsyncClient {

    private CloseableHttpAsyncClient httpClient;

    public AsyncHttpClient() {
        httpClient = HttpAsyncClients.createDefault();
    }

    public void start() {
        httpClient.start();
    }

    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer httpAsyncRequestProducer, HttpAsyncResponseConsumer<T> httpAsyncResponseConsumer, HttpContext httpContext, FutureCallback<T> futureCallback) {
        return httpClient.execute(httpAsyncRequestProducer, httpAsyncResponseConsumer, httpContext, futureCallback);
    }

    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer httpAsyncRequestProducer, HttpAsyncResponseConsumer<T> httpAsyncResponseConsumer, FutureCallback<T> futureCallback) {
        return httpClient.execute(httpAsyncRequestProducer, httpAsyncResponseConsumer, futureCallback);
    }

    @Override
    public Future<HttpResponse> execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext, FutureCallback<HttpResponse> futureCallback) {
        return httpClient.execute(httpHost, httpRequest, httpContext, futureCallback);
    }

    @Override
    public Future<HttpResponse> execute(HttpHost httpHost, HttpRequest httpRequest, FutureCallback<HttpResponse> futureCallback) {
        return httpClient.execute(httpHost, httpRequest, futureCallback);
    }

    @Override
    public Future<HttpResponse> execute(HttpUriRequest httpUriRequest, HttpContext httpContext, FutureCallback<HttpResponse> futureCallback) {
        return httpClient.execute(httpUriRequest, httpContext, futureCallback);
    }

    @Override
    public Future<HttpResponse> execute(HttpUriRequest httpUriRequest, FutureCallback<HttpResponse> futureCallback) {
        return httpClient.execute(httpUriRequest, futureCallback);
    }
}
