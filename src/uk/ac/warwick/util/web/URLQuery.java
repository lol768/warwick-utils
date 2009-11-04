package uk.ac.warwick.util.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.core.StringUtils;

/**
 * Class representing a URL query string, which can also have values added to it.
 * <p>
 * <i>Not thread-safe.</i>
 * 
 * @author cusebr
 */
public class URLQuery {
	private List<Pair<String,String>> query = new ArrayList<Pair<String,String>>();
	
	private Charset charset = Charset.forName("UTF-8");
	
	public URLQuery() {}
	
	public URLQuery (final String query) throws UnsupportedEncodingException {
		this();
		String q = StringUtils.nullGuard(query);
		if (q.startsWith("?")) {
			q = q.substring(1);
		}
		for (String pair : q.split("&")) {
			int i = pair.indexOf("=");
			if (i > -1) {
				add(
						decode(pair.substring(0,i)), 
						decode(pair.substring(i+1))
				);
			}
		}
	}
	
	/**
	 * @param key Parameter name to find.
	 * @return The first value with this name, or null if it is not found.
	 */
	public String getFirst(String key) {
		for (Pair<String,String> pair : query) {
			if (pair.getLeft().equals(key)) {
				return pair.getRight();
			}
		}
		return null;
	}
	
	/**
	 * Adds another parameter to the end.
	 * @return this, for chaining.
	 */
	public URLQuery add(String key, String value) {
		query.add(new Pair<String,String>(key,value));
		return this;
	}
	
	/**
	 * Adds all the values of the provided {@link URLQuery} to this one.
	 * Doesn't check for duplicates or anything - they are all just added
	 * to the end.
	 * @param other The other URLQuery to take parameters from. It is not modified.
	 * @return This.
	 */
	public URLQuery add(URLQuery other) {
		query.addAll(other.getValues());
		return this;
	}
	
	/**
	 * Similar to {@link #add(URLQuery)}, but this will remove any existing
	 * parameters with the same name as ones found in other.
	 */
	public URLQuery override(URLQuery other) {
		removeKeys(other.getKeys());
		return add(other);
	}
	
	/**
	 * Returns the query string. NO question mark as that is not
	 * part of the query.
	 */
	public String toQueryString() throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Pair<String,String> pair : query) {
			if (first) {
				first = false;
			} else {
				sb.append("&");
			}
			sb.append(encode(pair.getLeft()));
			sb.append("=");
			sb.append(encode(pair.getRight()));
		}
		return sb.toString();
	}
	
	public Set<String> getKeys() {
		Set<String> keys = new HashSet<String>();
		for (Pair<String,String> pair : query){
			keys.add(pair.getLeft());
		}
		return keys;
	}
	
	/**
	 * Same as {@link #toQueryString()} except it is prefixed with a
	 * question mark UNLESS it is empty. Handy for quickly appending to
	 * the end of a URL.
	 */
	public String toQueryStringPrefixed() throws UnsupportedEncodingException {
		String result = toQueryString();
		if (result.isEmpty()) {
			return "";
		} else {
			return "?" + result;
		}
	}
	
	private String encode(String unencoded) throws UnsupportedEncodingException {
		return URLEncoder.encode(unencoded, charset.name());
	}
	
	private String decode(String encoded) throws UnsupportedEncodingException {
		return URLDecoder.decode(encoded, charset.name());
	}

	/**
	 * Remove all parameters from the query string that match the given names.
	 * @param keys Key names to remove
	 */
	public void removeKeys(String... keys) {
		removeKeys(Arrays.asList(keys));
	}
	
	public void removeKeys(Collection<String> keys) {
		for (Iterator<Pair<String, String>> iterator = query.iterator(); iterator.hasNext();) {
			Pair<String,String> pair = iterator.next();
			if (keys.contains(pair.getLeft())) {
				iterator.remove();
			}
		}
	}
	
	/**
	 * For access by another URLQuery instance, used in {@link #add(URLQuery)}.
	 */
	private Collection<Pair<String,String>> getValues() {
		return query;
	}
}
