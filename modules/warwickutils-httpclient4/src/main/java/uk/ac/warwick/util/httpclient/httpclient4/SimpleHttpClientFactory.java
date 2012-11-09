package uk.ac.warwick.util.httpclient.httpclient4;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Returns a new HttpClient every time.
 */
public class SimpleHttpClientFactory implements HttpClientFactory {

	public HttpClient getClient() {
		return new DefaultHttpClient();
	}

}
