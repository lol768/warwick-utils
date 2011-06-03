package uk.ac.warwick.util.httpclient.httpclient4;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
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
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

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

        // Handle GZIP and DEFLATE
        client.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(final HttpRequest request, final HttpContext context) throws HttpException,
                    IOException {
                if (!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", "gzip, deflate");
                }
            }
        });
        client.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(final HttpResponse response, final HttpContext context) throws HttpException,
                    IOException {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    Header ceheader = entity.getContentEncoding();
                    if (ceheader != null) {
                        for (HeaderElement codec: ceheader.getElements()) {
                            String codecname = codec.getName();
                            if ("gzip".equalsIgnoreCase(codecname)) {
                                response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                return;
                            } else if ("deflate".equals(codecname)) {
                                response.setEntity(new DeflateDecompressingEntity(response.getEntity()));
                                return;
                            }
                        }
                    }
                }
            }
        });

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

    static class GzipDecompressingEntity extends HttpEntityWrapper {
        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        public InputStream getContent() throws IOException, IllegalStateException {
            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();

            return new GZIPInputStream(wrappedin);
        }

        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }

    static class DeflateDecompressingEntity extends HttpEntityWrapper {
        public DeflateDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        public InputStream getContent() throws IOException, IllegalStateException {

            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();

            return new InflaterInputStream(wrappedin, new Inflater(true));
        }

        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }

}
