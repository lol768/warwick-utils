package uk.ac.warwick.util.content.textile2;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.texttransformers.TextTransformer;

/**
 * Text transformer which sends the text to a remote Textile service, and retrieves the response.
 * 
 * @todo handle
 * 
 * @author cusebr
 */
public final class TextileTextTransformer implements TextTransformer {

	public static final String TEXTILE_SERVICE_LOCATION_PROPERTY_KEY = "textile.service.location";

	private static String TEXTILE_SERVICE_URL = "http://localhost:2000/textile";
	
	private static final int CONNECTION_TIMEOUT = 3000; //short timeout time

	public TextileTextTransformer() {
		setupTextile2ServiceLocation(null);
	}

	public TextileTextTransformer(String textile2ServiceLocation) {
		setupTextile2ServiceLocation(textile2ServiceLocation);
	}

	/**
	 * @param textile2ServiceLocation
	 */
	private void setupTextile2ServiceLocation(String textile2ServiceLocation) {
		if (textile2ServiceLocation != null) {
			TEXTILE_SERVICE_URL = textile2ServiceLocation;
		} else if (System.getProperty(TEXTILE_SERVICE_LOCATION_PROPERTY_KEY) != null) {
			TEXTILE_SERVICE_URL = System.getProperty(TEXTILE_SERVICE_LOCATION_PROPERTY_KEY);
		}
	}

	public MutableContent apply(final MutableContent mc) {
		try {
			HttpClient client = new HttpClient();
			PostMethod method = new PostMethod(TEXTILE_SERVICE_URL);
			
			NameValuePair textileData = new NameValuePair("textile", mc.getContent());
			
			method.setRequestBody(new NameValuePair[] { textileData });
			
			// set reasonable connection timeouts
	        HttpConnectionManagerParams params = client.getHttpConnectionManager().getParams();
	        params.setConnectionTimeout(CONNECTION_TIMEOUT);
	        params.setSoTimeout(CONNECTION_TIMEOUT);
	        
	        //don't retry!
	        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0,false));
			
			int returnCode = client.executeMethod(method);
			if (returnCode != HttpServletResponse.SC_OK) {
				throw new IllegalStateException("Error connecting with Textile server - response code was " + returnCode);
			}
			mc.setContent(method.getResponseBodyAsString().trim());
			return mc;
		} catch (HttpException e) {
			throw new IllegalStateException("Error connecting with Textile server - " + e.getMessage(), e);
		} catch (IOException e) {
			throw new IllegalStateException("Error connecting with Textile server - " + e.getMessage(), e);
		}
	}
}
