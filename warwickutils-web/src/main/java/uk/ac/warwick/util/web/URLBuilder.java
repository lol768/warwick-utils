package uk.ac.warwick.util.web;

import java.net.URI;
import java.net.URL;

import uk.ac.warwick.util.web.Uri.UriException;

/**
 * URL is immutable, but sometimes you want to just change
 * one part of a URL. This simplifies the task.
 * 
 * Setters return the builder for method chaining.
 * 
 * @deprecated Use {@link UriBuilder}
 */
public class URLBuilder {
    private final UriBuilder builder;
	
	public URLBuilder(URL existingUrl) {
		this.builder = new UriBuilder(Uri.fromJavaUrl(existingUrl));
	}
	
	public URLBuilder(String urlString) {
		this.builder = new UriBuilder(Uri.parse(urlString));
	}

	public URL toURL() {
		return builder.toUri().toJavaUrl();
	}
	
	public URI toURI() {
	    return builder.toUri().toJavaUri();
	}
	
	@Override
	public String toString() {
	    try {
	        return toURL().toExternalForm();
	    } catch (UriException e) {
	        return "**MALFORMED** " + builder.toString();
	    }
	}

	public String getProtocol() {
		return builder.getScheme();
	}

	public URLBuilder setProtocol(String protocol) {
		builder.setScheme(protocol);
		return this;
	}

	public String getHost() {
		return getServerName(builder.getAuthority());
	}

	public URLBuilder setHost(String host) {
	    int port = getServerPort(builder.getAuthority());
	    if (port > 0) { host += ":" + port; }
	    
		builder.setAuthority(host);
		return this;
	}

	public int getPort() {
		return getServerPort(builder.getAuthority());
	}

	public URLBuilder setPort(int port) {
		builder.setAuthority(getServerName(builder.getAuthority()) + ":" + port);
		return this;
	}
    
    private String getServerName(String authority) {
        if (authority.indexOf(":") == -1) {
            return authority;
        }
        
        return authority.substring(0, authority.lastIndexOf(":"));
    }
    
    private int getServerPort(String authority) {
        if (authority.indexOf(":") == -1) {
            return 0;
        }
        
        String[] parts = authority.split(":");
        return Integer.parseInt(parts[parts.length-1]);
    }

	public String getPath() {
		return builder.getPath();
	}

	public URLBuilder setPath(String path) {
		builder.setPath(path);
		return this;
	}

	public String getQuery() {
		return builder.getQuery();
	}

	public URLBuilder setQuery(String query) {
		builder.setQuery(query);
		return this;
	}
	
	public URLBuilder setQuery(URLQuery query) {
		return setQuery(query.toQueryString());
	}
	
	public void sanitise() {
	    // no-op
	}
}
