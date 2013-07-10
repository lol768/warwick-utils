package uk.ac.warwick.util.core.lookup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor;
import uk.ac.warwick.util.httpclient.httpclient4.HttpRequestDecorator;
import uk.ac.warwick.util.httpclient.httpclient4.SimpleHttpMethodExecutor;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;
import uk.ac.warwick.util.web.Uri;

public class TwitterAuthenticationHttpRequestDecorator implements HttpRequestDecorator, InitializingBean {
    
    private static final Uri DEFAULT_OAUTH_ENDPOINT_URL = Uri.parse("https://api.twitter.com/oauth2/token");
    
    private final Uri oauthEndpointUri;
    
    private String consumerKey;
    
    private String consumerSecret;
    
    private String bearerToken;
    
    public TwitterAuthenticationHttpRequestDecorator() {
        this(DEFAULT_OAUTH_ENDPOINT_URL);
    }
    
    public TwitterAuthenticationHttpRequestDecorator(Uri theEndpointUri) {
        this.oauthEndpointUri = theEndpointUri;
    }
    
    public void decorate(HttpUriRequest request, HttpContext context) {
        request.addHeader("Authorization", "Bearer " + getBearerToken());
    }

    private synchronized String getBearerToken() {
        if (bearerToken != null) {
            return bearerToken;
        }
        
        // Generate a bearer token. Ref https://dev.twitter.com/docs/auth/application-only-auth
        try {
            String encodedKey = URLEncoder.encode(consumerKey, "UTF-8");
            String encodedSecret = URLEncoder.encode(consumerSecret, "UTF-8");
            
            String bearerCredentials = String.format("%s:%s", encodedKey, encodedSecret);
            String encodedBearerCredentials = new String(Base64.encodeBase64(bearerCredentials.getBytes("UTF-8")), "UTF-8");
            
            HttpMethodExecutor executor = new SimpleHttpMethodExecutor(Method.post);
            executor.setUrl(oauthEndpointUri);
            executor.addHeader("Authorization", "Basic " + encodedBearerCredentials);
            executor.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            
            executor.setPostBody(Collections.singletonList(new BasicNameValuePair("grant_type", "client_credentials")));
            
            this.bearerToken = executor.execute(new ResponseHandler<String>() {
                public String handleResponse(HttpResponse response)
                        throws ClientProtocolException, IOException {
                    String responseText = EntityUtils.toString(response.getEntity());
                    
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        throw new IllegalStateException(
                            String.format("Expected HTTP 200 from Twitter OAuth 2 endpoint; received %d (content %s)", response.getStatusLine().getStatusCode(), responseText)
                        );
                    }
                    
                    try {
                        JSONObject json = new JSONObject(responseText);
                        
                        String tokenType = json.getString("token_type");
                        if (!"bearer".equals(tokenType)) {
                            throw new IllegalStateException(String.format("Expected bearer token from Twitter API (was %s)", tokenType));
                        }
                        
                        return json.getString("access_token");
                    } catch (JSONException e) {
                        throw new IllegalStateException(String.format("Invalid JSON received from Twitter API (received %s)", responseText), e);
                    }
                }
            }).getRight();
            
            return bearerToken;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 no longer supported!?!!??!", e);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't fetch bearer token from Twitter OAuth!", e);
        }
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.isTrue(StringUtils.hasText(consumerKey), "Consumer key must be specified");
        Assert.isTrue(StringUtils.hasText(consumerSecret), "Consumer secret must be specified");
    }

}
