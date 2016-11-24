package uk.ac.warwick.util.mywarwick;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.Future;

@Named
@Singleton
public class HttpClient implements Provider<HttpAsyncClient> {

    private CloseableHttpAsyncClient httpClient;

    public HttpClient() {
        httpClient = HttpAsyncClients.createDefault();
        httpClient.start();
    }

    public void start() {
        httpClient.start();
    }

    public boolean isRunning() {
        return httpClient.isRunning();
    }

    public Future<HttpResponse> execute(HttpUriRequest var1, FutureCallback<HttpResponse> var2) {
        return httpClient.execute(var1,var2);
    }

    @Override
    public HttpAsyncClient get() {
        return httpClient;
    }
}
