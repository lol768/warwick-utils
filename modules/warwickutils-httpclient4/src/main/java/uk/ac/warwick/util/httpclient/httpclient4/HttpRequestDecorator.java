package uk.ac.warwick.util.httpclient.httpclient4;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

/**
 * This interface can decorator a http request before
 * it is used, for example to add OAuth information.
 */
public interface HttpRequestDecorator {
	public void decorate(HttpUriRequest request, HttpContext context);	
}