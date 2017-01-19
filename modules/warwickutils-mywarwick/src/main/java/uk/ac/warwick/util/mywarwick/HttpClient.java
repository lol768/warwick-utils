package uk.ac.warwick.util.mywarwick;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.springframework.beans.factory.DisposableBean;

import javax.inject.Provider;
import java.util.concurrent.Future;

public interface HttpClient extends Provider<HttpAsyncClient>, DisposableBean {
    void start();
    boolean isRunning();
    Future<HttpResponse> execute(HttpUriRequest request, FutureCallback<HttpResponse> callback);
}
