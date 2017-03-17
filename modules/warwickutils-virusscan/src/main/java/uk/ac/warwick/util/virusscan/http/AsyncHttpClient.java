package uk.ac.warwick.util.virusscan.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;

import javax.annotation.PreDestroy;
import javax.inject.Provider;
import java.util.concurrent.Future;

public interface AsyncHttpClient extends Provider<HttpAsyncClient> {
    void start();
    boolean isRunning();
    Future<HttpResponse> execute(HttpUriRequest request, FutureCallback<HttpResponse> callback);

    @PreDestroy
    void destroy() throws Exception;
}
