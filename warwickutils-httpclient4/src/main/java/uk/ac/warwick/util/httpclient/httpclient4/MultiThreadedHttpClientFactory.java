package uk.ac.warwick.util.httpclient.httpclient4;

import java.net.ProxySelector;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ResponseProcessCookies;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public final class MultiThreadedHttpClientFactory implements HttpClientFactory {

    private static final MultiThreadedHttpClientFactory INSTANCE = new MultiThreadedHttpClientFactory();

    private final HttpClient client;

    public MultiThreadedHttpClientFactory() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpProtocolParams.setUserAgent(params, "WarwickUtils HttpMethodExecutor, elab@warwick.ac.uk");

        HttpClientParams.setRedirecting(params, true);
        params.setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        params.setIntParameter(ClientPNames.MAX_REDIRECTS, 10);

        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        DefaultHttpClient client = new DefaultHttpClient(cm, params);
        
        // try resending the request once
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(1, false));

        // Don't care about response cookies
        client.removeResponseInterceptorByClass(ResponseProcessCookies.class);
        
        ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
                client.getConnectionManager().getSchemeRegistry(),
                ProxySelector.getDefault());
        client.setRoutePlanner(routePlanner);
        
        this.client = client;
    }

    public HttpClient getClient() {
        return client;
    }

    public static MultiThreadedHttpClientFactory getInstance() {
        return INSTANCE;
    }

}
