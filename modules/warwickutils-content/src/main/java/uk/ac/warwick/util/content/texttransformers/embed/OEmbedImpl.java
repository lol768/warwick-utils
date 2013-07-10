package uk.ac.warwick.util.content.texttransformers.embed;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import uk.ac.warwick.util.cache.Cache;
import uk.ac.warwick.util.cache.CacheEntry;
import uk.ac.warwick.util.cache.CacheEntryUpdateException;
import uk.ac.warwick.util.cache.CacheExpiryStrategy;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.cache.SingularCacheEntryFactory;
import uk.ac.warwick.util.content.texttransformers.embed.OEmbedProvider.Format;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;
import uk.ac.warwick.util.httpclient.httpclient4.SimpleHttpMethodExecutor;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.UriBuilder;

import com.google.common.collect.Maps;

public class OEmbedImpl extends SingularCacheEntryFactory<OEmbedRequest, OEmbedResponse> implements OEmbed {   
    private static final Logger LOGGER = Logger.getLogger(OEmbedImpl.class);
    
    private static final int DEFAULT_CACHE_TIME_IN_SECONDS = 60 * 60; // 1 hour, but can be overridden by the response
    private static final int DEFAULT_CACHE_TIME_FAILURES_IN_SECONDS = 24 * 60 * 60; // 24 hours
    private static final long MS_IN_A_SECOND = 1000;
    
	/** The map of known providers */
	private Map<String, OEmbedProvider> provider = Maps.newHashMap();

	/** The map of all known parsers. For now, only json and xml exists */
	private Map<Format, OEmbedParser> parser;

	/** Flag, if autodiscovery is enabled when there is no provider for a specific url. Defaults to false */
	private boolean autodiscovery = false;
	
	private final Cache<OEmbedRequest, OEmbedResponse> cache;

	/**
	 * Constructs the OEmbed Api with the default parsers (json and xml) and 
	 * an empty map of providers
	 */
	public OEmbedImpl() {	    
		this.parser = Maps.newHashMap();
		this.parser.put(Format.json, new OEmbedJsonParser());
		this.parser.put(Format.xml, new OEmbedXmlParser());

        this.cache = Caches.newCache(CACHE_NAME, this, DEFAULT_CACHE_TIME_IN_SECONDS, CacheStrategy.EhCacheRequired);
        this.cache.setExpiryStrategy(new CacheExpiryStrategy<OEmbedRequest, OEmbedResponse>() {
            public boolean isExpired(CacheEntry<OEmbedRequest, OEmbedResponse> entry) {
                final long expires;
                if (entry.getValue() != null && entry.getValue().getCacheAge() != null) {
                    expires = entry.getTimestamp() + (entry.getValue().getCacheAge() * MS_IN_A_SECOND);
                } else if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    expires = entry.getTimestamp() + (DEFAULT_CACHE_TIME_IN_SECONDS * MS_IN_A_SECOND);
                } else {
                    expires = entry.getTimestamp() + (DEFAULT_CACHE_TIME_FAILURES_IN_SECONDS * MS_IN_A_SECOND);
                }
                
                final long now = System.currentTimeMillis();
                return expires <= now;
            }
        });
	}

	public Map<String, OEmbedProvider> getProvider() {
		return provider;
	}

	public void setProvider(Map<String, OEmbedProvider> provider) {
		this.provider = provider;
	}

	public OEmbedParser getParser(final Format format) {
		return this.parser.get(format);
	}

	public OEmbedResponse transformUrl(final Uri uri) throws OEmbedException {
	    try {
	        return cache.get(new OEmbedRequest(uri));
	    } catch (CacheEntryUpdateException e) {
	        throw new OEmbedException(e);
	    }
	}

	public OEmbedResponse create(final OEmbedRequest request) throws CacheEntryUpdateException {
        if (request == null || request.getUri() == null) {
            LOGGER.warn("Can't embed an empty url!");
        } else {
            OEmbedProvider autoProvider = this.findProvider(request.getUri());
            if (autoProvider == null && (!this.isAutodiscovery() || (autoProvider = autodiscoverOEmbedUri(request.getUri())) == null))
                LOGGER.info(String
                        .format("No OEmbed provider for url %s and autodiscovery is disabled or found no result", request.getUri()));
            else {
                try {
                    final OEmbedProvider provider = autoProvider;
                    final UriBuilder api = provider.toApiUrl(request.getUri());
                    LOGGER.debug(String.format("Calling url %s", api.toString()));
                    
                    if (request.getMaxWidth() != null) api.putQueryParameter("maxwidth", request.getMaxWidth().toString());
                    if (request.getMaxHeight() != null) api.putQueryParameter("maxheight", request.getMaxHeight().toString());
                    
                    HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get);
                    ex.setUrl(api.toUri());
                    ex.setHttpRequestDecorator(provider.getHttpRequestDecorator());
                    
                    return ex.execute(new ResponseHandler<OEmbedResponse>() {
                        public OEmbedResponse handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                            try {
                                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                                    LOGGER.warn(String.format("Server returned error %d for '%s': %s", httpResponse.getStatusLine()
                                            .getStatusCode(), request.getUri(), EntityUtils.toString(httpResponse.getEntity())));
                                    final OEmbedResponse emptyResponse = new OEmbedResponse(request);
                                    emptyResponse.setEmpty(true);
                                    emptyResponse.setCacheAge((long) DEFAULT_CACHE_TIME_FAILURES_IN_SECONDS);
                                    
                                    return emptyResponse;
                                } else {
                                    final OEmbedResponse response = 
                                        getParser(provider.getFormat())
                                        .unmarshal(httpResponse.getEntity().getContent());
                                    
                                    response.setSource(provider.getName());
                                    response.setRequest(request);
                                    
                                    return response;
                                }
                            } catch (OEmbedException e) {
                                throw new IOException(e);
                            }
                        }
                    }).getRight();
                } catch (Exception e) {
                    throw new CacheEntryUpdateException(e);
                }
            }
        }
        
        return null;
    }

    public boolean shouldBeCached(OEmbedResponse response) {
        return true;
    }

	/**
	 * Finds a provider for the given url
	 * @param url
	 * @return
	 */
	private OEmbedProvider findProvider(final Uri url) {
		for(OEmbedProvider provider : this.provider.values()) {
			for (String urlScheme : provider.getUrlSchemes()) {
				if (url.toString().matches(urlScheme)) {
					return provider;
				}	
			}
		}		
		return null;
	}

	private OEmbedProvider autodiscoverOEmbedUri(final Uri uri) {
	    HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get);
	    ex.setUrl(uri);
	    
	    try {
            return ex.execute(new ResponseHandler<OEmbedProvider>() {
                public OEmbedProvider handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        LOGGER.warn(String.format("Autodiscovery for %s failed, server returned error %d: %s", uri, httpResponse
                                .getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity())));
                    } else {
                        final Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity(), "UTF-8"),
                                String.format("%s://%s", uri.getScheme(), uri.getAuthority()));
                        for (Element alternate: document.getElementsByAttributeValue("rel", "alternate")) {
                            if (alternate.attr("type").equalsIgnoreCase("application/json+oembed"))
                                return new AutodiscoveredOEmbedProvider(uri, Uri.parse(alternate.absUrl("href")), Format.json);
                            else if (alternate.attr("type").equalsIgnoreCase("text/xml+oembed"))
                                return new AutodiscoveredOEmbedProvider(uri, Uri.parse(alternate.absUrl("href")), Format.xml);
                        }
                    }
    
                    return null;
                }
            }).getRight();
	    } catch (IOException e) {
	        LOGGER.warn(String.format("Exception trying to autodiscover OEmbed uri for %s", uri.toString()), e);
	        
	        return null;
	    }
	}

	public Map<Format, OEmbedParser> getParser() {
		return parser;
	}

	public void setParser(Map<Format, OEmbedParser> parser) {
		this.parser = parser;
	}

	public boolean isAutodiscovery() {
		return autodiscovery;
	}

	public void setAutodiscovery(boolean autodiscovery) {
		this.autodiscovery = autodiscovery;
	}

}