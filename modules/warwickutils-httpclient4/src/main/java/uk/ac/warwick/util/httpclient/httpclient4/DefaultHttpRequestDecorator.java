package uk.ac.warwick.util.httpclient.httpclient4;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

/**
 * Default implementation of a decorator. It just returns the request
 */
public class DefaultHttpRequestDecorator implements HttpRequestDecorator {
	@Override
	public void decorate(HttpUriRequest request, HttpContext context) {
	    // Nothing to do
	}
}