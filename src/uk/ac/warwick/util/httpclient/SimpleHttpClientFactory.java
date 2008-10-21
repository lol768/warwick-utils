package uk.ac.warwick.util.httpclient;

import org.apache.commons.httpclient.HttpClient;

/**
 * Returns a new HttpClient every time.
 */
public class SimpleHttpClientFactory implements HttpClientFactory {

	public HttpClient getClient() {
		return new HttpClient();
	}

}
