package uk.ac.warwick.util.web;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.BitSet;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.springframework.util.StringUtils;

/**
 * URL is immutable, but sometimes you want to just change
 * one part of a URL. This simplifies the task.
 * 
 * Setters return the builder for method chaining.
 */
public class URLBuilder {
    private static final BitSet ALLOWED_PATH_CHARACTERS;
    
    static {
        ALLOWED_PATH_CHARACTERS = new BitSet(256);
        
        //standard URL characters
        ALLOWED_PATH_CHARACTERS.set(';');
        ALLOWED_PATH_CHARACTERS.set('/');
        ALLOWED_PATH_CHARACTERS.set('?');
        ALLOWED_PATH_CHARACTERS.set(':');
        ALLOWED_PATH_CHARACTERS.set('@');
        ALLOWED_PATH_CHARACTERS.set('&');
        ALLOWED_PATH_CHARACTERS.set('=');
        ALLOWED_PATH_CHARACTERS.set('+');
        ALLOWED_PATH_CHARACTERS.set('$');
        ALLOWED_PATH_CHARACTERS.set(',');
        ALLOWED_PATH_CHARACTERS.set('-');
        ALLOWED_PATH_CHARACTERS.set('_');
        ALLOWED_PATH_CHARACTERS.set('.');
        ALLOWED_PATH_CHARACTERS.set('!');
        ALLOWED_PATH_CHARACTERS.set('~');
        ALLOWED_PATH_CHARACTERS.set('*');
        ALLOWED_PATH_CHARACTERS.set('\'');
        ALLOWED_PATH_CHARACTERS.set('(');
        ALLOWED_PATH_CHARACTERS.set(')');
        
        // ignore already escaped characters
        ALLOWED_PATH_CHARACTERS.set('%');
        
        // alphanumeric
        for (int i = 'a'; i <= 'z'; i++) {
            ALLOWED_PATH_CHARACTERS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            ALLOWED_PATH_CHARACTERS.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            ALLOWED_PATH_CHARACTERS.set(i);
        }
    }
    
	private String protocol;
	private String host;
	private int port = -1;
	private String path;
	private String query;
	
	public URLBuilder(URL existingUrl) {
		protocol = existingUrl.getProtocol();
		host = existingUrl.getHost();
		port = existingUrl.getPort();
		path = existingUrl.getPath();
		query = existingUrl.getQuery();
		
		sanitise();
	}
	
	public URLBuilder(String urlString) throws MalformedURLException {
		this(new URL(urlString));
	}

	public URL toURL() throws MalformedURLException {
		String file = path;
		if (query != null && !query.isEmpty()) {
			file += "?" + query;
		}
		return new URL(protocol,host,port,file);
	}
	
	public URI toURI() throws MalformedURLException {
	    try {
	        return toURL().toURI();
	    } catch (URISyntaxException e) {
	        throw new MalformedURLException(e.getMessage());
	    }
	}
	
	@Override
	public String toString() {
	    try {
	        return toURL().toExternalForm();
	    } catch (MalformedURLException e) {
	        return "**MALFORMED** " + protocol + "://" + host + ":" + port + path + (query == null ? "" : "?" + query);
	    }
	}

	public String getProtocol() {
		return protocol;
	}

	public URLBuilder setProtocol(String protocol) {
		this.protocol = protocol;
		return this;
	}

	public String getHost() {
		return host;
	}

	public URLBuilder setHost(String host) {
		this.host = host;
		return this;
	}

	public int getPort() {
		return port;
	}

	public URLBuilder setPort(int port) {
		this.port = port;
		return this;
	}

	public String getPath() {
		return path;
	}

	public URLBuilder setPath(String path) {
		this.path = path;
		return this;
	}

	public String getQuery() {
		return query;
	}

	public URLBuilder setQuery(String query) {
		this.query = query;
		return this;
	}
	
	public URLBuilder setQuery(URLQuery query) {
		return setQuery(query.toQueryString());
	}
	
	public void sanitise() {
	    if (StringUtils.hasText(path) && !"/".equals(path)) {
	        try {
    	        StringBuilder pathSb = new StringBuilder();
    	        URLCodec codec = new URLCodec("UTF-8");
    	        
    	        String[] parts = path.split("\\/");
    	        for (int i = 0; i < parts.length ; i++) {
    	            String part = parts[i];
    	            
    	            if (StringUtils.hasText(part)) {
    	                pathSb.append(codec.encode(part));
    	            }
    	            
    	            // Only append / at the end if it was there originally
    	            if (i != parts.length-1 || path.endsWith("/")) {
    	                pathSb.append("/");
    	            }
    	        }
    	        
    	        path = pathSb.toString();
	        } catch (EncoderException e) {
	            throw new IllegalArgumentException("Couldn't sanitise path; " + path);
	        }
	    }
	    
	    if (StringUtils.hasText(query)) {
	        setQuery(new URLQuery(query));
	    }
	}
}
