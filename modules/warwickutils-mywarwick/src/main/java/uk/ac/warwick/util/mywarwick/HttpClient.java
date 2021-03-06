package uk.ac.warwick.util.mywarwick;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;

import javax.annotation.PreDestroy;
import javax.inject.Provider;
import java.util.concurrent.Future;

public interface HttpClient extends Provider<HttpAsyncClient> {
    void start();
    boolean isRunning();
    Future<HttpResponse> execute(HttpUriRequest request, FutureCallback<HttpResponse> callback);
    RequestConfig getRequestConfig();

    @PreDestroy
    void destroy() throws Exception;
}
