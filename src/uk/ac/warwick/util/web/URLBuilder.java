package uk.ac.warwick.util.web;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * URL is immutable, but sometimes you want to just change
 * one part of a URL. This simplifies the task.
 * 
 * Setters return the builder for method chaining.
 */
public class URLBuilder {
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
	}

	public URL toURL() throws MalformedURLException {
		String file = path;
		if (query != null && !query.isEmpty()) {
			file += "?" + query;
		}
		return new URL(protocol,host,port,file);
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
}
