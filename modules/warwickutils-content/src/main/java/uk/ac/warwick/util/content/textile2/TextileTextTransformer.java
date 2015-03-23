package uk.ac.warwick.util.content.textile2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
            HttpClient client = HttpClientBuilder.create().build();
			HttpPost method = new HttpPost(TEXTILE_SERVICE_URL);

            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("textile", mc.getContent()));
            method.setEntity(new UrlEncodedFormEntity(nvps));

            RequestConfig config =
                RequestConfig.custom()
                    .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
                    .setConnectTimeout(CONNECTION_TIMEOUT)
                    .setSocketTimeout(CONNECTION_TIMEOUT)
                    .build();

            method.setConfig(config);
	        
			HttpResponse response = client.execute(method);
			if (response.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
				throw new IllegalStateException("Error connecting with Textile server - response code was " + response.getStatusLine().getStatusCode());
			}
			mc.setContent(EntityUtils.toString(response.getEntity()).trim());
			return mc;
		} catch (IOException e) {
			throw new IllegalStateException("Error connecting with Textile server - " + e.getMessage(), e);
		}
	}
}
