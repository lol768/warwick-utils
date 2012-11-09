/**
 * Created by Michael Simons, michael-simons.eu and released under The BSD
 * License http://www.opensource.org/licenses/bsd-license.php Copyright (c)
 * 2010, Michael Simons All rights reserved. Redistribution and use in source
 * and binary forms, with or without modification, are permitted provided that
 * the following conditions are met: * Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following
 * disclaimer. * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. *
 * Neither the name of michael-simons.eu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.warwick.util.content.oembed;

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
import uk.ac.warwick.util.cache.CacheEntryUpdateException;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.SingularCacheEntryFactory;
import uk.ac.warwick.util.core.ExceptionUtils;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;
import uk.ac.warwick.util.httpclient.httpclient4.SimpleHttpMethodExecutor;
import uk.ac.warwick.util.web.Uri;

import com.google.common.collect.Maps;

/**
 * @author Michael J. Simons
 */
public class OEmbed extends SingularCacheEntryFactory<String, OEmbedResponse> {
    
    public static final String CACHE_NAME = "OEmbedResponseCache";
    
    private static final long DEFAULT_CACHE_TIMEOUT = 60 * 60; // Cache for one hour unless there's a specified cache age

    /** The logger */
    private final Logger LOGGER = Logger.getLogger(OEmbed.class);

    /** The map of known providers */
    private Map<String, OEmbedProvider> provider = Maps.newHashMap();

    /** Optional handlers for providers registered in {@link #provider} */
    private Map<String, OEmbedResponseHandler> handler = Maps.newHashMap();

    /** The map of all known parsers. For now, only json and xml exists */
    private Map<String, OEmbedParser> parser;

    /** Optional ehcache client for caching valid oembed responses */
    private final Cache<String, OEmbedResponse> cache;

    /**
     * Flag, if autodiscovery is enabled when there is no provider for a
     * specific url. Defaults to false
     */
    private boolean autodiscovery = false;

//    private String baseUri = "";

    /** The default user agent */
    private String userAgent = String.format("WarwickUtils OEmbed, University of Warwick (http://go.warwick.ac.uk/webteam); email webteam@warwick.ac.uk or call 024765 74000");

    /** An optional string that is appended to the user agent */
    private String consumer;

    /**
     * Constructs the Oembed Api with the default parsers (json and xml) and an
     * empty map of provider
     */
    public OEmbed() {
        this.cache = Caches.newCache(CACHE_NAME, this, DEFAULT_CACHE_TIMEOUT);
        
        this.parser = Maps.newHashMap();
        this.parser.put("json", new OEmbedJsonParser());
        this.parser.put("xml", new OEmbedXmlParser());
    }

    public Map<String, OEmbedProvider> getProvider() {
        return provider;
    }

    public void setProvider(Map<String, OEmbedProvider> provider) {
        this.provider = provider;
    }

    public OEmbedParser getParser(final String format) {
        return this.parser.get(format);
    }
    
    public OEmbedResponse transformUrl(final String url) throws OEmbedException {
        try {
            return cache.get(url);
        } catch (CacheEntryUpdateException e) {
            OEmbedException ex = ExceptionUtils.retrieveException(e, OEmbedException.class);
            if (ex != null) {
                throw ex;
            } else {
                throw new IllegalStateException(e);
            }
        }
    }

    public OEmbedResponse create(final String url) throws CacheEntryUpdateException {
        OEmbedResponse response = null;
        
        try {
            if (url == null || url.length() == 0) {
                LOGGER.warn("Can't embed an empty url!");
            } else {
                OEmbedProvider p = this.findProvider(url);
                if (p == null && (!this.isAutodiscovery() || (p = autodiscoverOembedURIForUrl(url)) == null)) {
                    LOGGER.info(String
                            .format("No oembed provider for url %s and autodiscovery is disabled or found no result", url));
                } else {
                    try {
                        final OEmbedProvider provider = p;
                        final Uri api = provider.toApiUrl(url);
                        LOGGER.debug(String.format("Calling url %s", api.toString()));
                        
                        final HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get, api.toString());
    
                        if (this.userAgent != null)
                            ex.addHeader("User-Agent",
                                    String.format("%s%s", this.userAgent, this.consumer == null ? "" : "; " + this.consumer));
                        
                        response = ex.execute(new ResponseHandler<OEmbedResponse>() {
                            public OEmbedResponse handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                                    LOGGER.warn(String.format("Server returned error %d: %s", httpResponse.getStatusLine().getStatusCode(),
                                            EntityUtils.toString(httpResponse.getEntity())));
                                    
                                    return new EmptyOEmbedResponse();
                                } else {
                                    try {
                                        OEmbedResponse response = getParser(provider.getFormat().toLowerCase()).unmarshal(
                                                httpResponse.getEntity().getContent());
                                        response.setSource(provider.getName());
                                        response.setOriginalUrl(url);
                                        
                                        return response;
                                    } catch (OEmbedException e) {
                                        throw new IOException(e);
                                    }
                                }
                            }
                        }).getRight();
                    } catch (IOException e) {
                        if (e.getCause() != null && e.getCause() instanceof OEmbedException) {
                            throw (OEmbedException) e.getCause();
                        } else {
                            throw new OEmbedException(e);
                        }
                    } catch (NullPointerException e) {
                        throw new OEmbedException(String.format("NPE, probably invalid format :%s", p.getFormat()));
                    } catch (Exception e) {
                        throw new OEmbedException(e);
                    }
                }
            }
        } catch (OEmbedException e) {
            throw new CacheEntryUpdateException(e);
        }

        return response;
    }

    public boolean shouldBeCached(OEmbedResponse val) {
        return val != null;
    }
//
//    public String transformDocumentString(final String documentHtml) {
//        final Document rv = transformDocument(documentHtml);
//        rv.outputSettings().prettyPrint(false).escapeMode(EscapeMode.xhtml);
//        return rv.body().html();
//    }
//
//    public Document transformDocument(final String documentHtml) {
//        return transformDocument(Jsoup.parseBodyFragment(documentHtml, baseUri));
//    }
//
//    /**
//     * Parses the given html document into a document and processes all anchor
//     * elements. If a valid anchor is found, it tries to get an oembed response
//     * for it's url and than render the result into the document replacing the
//     * given anchor.<br>
//     * It returns the html representation of the new document.<br>
//     * If there's an error or no oembed result for an url, the anchor tag will
//     * be left as it was.
//     * 
//     * @param documentHtml
//     * @return
//     */
//    public Document transformDocument(final Document document) {
//        boolean changedBaseUri = false;
//        if (document.baseUri() == null && this.getBaseUri() != null) {
//            document.setBaseUri(this.getBaseUri());
//            changedBaseUri = true;
//        }
//        
//        for (Element a: document.getElementsByTag("a")) {
//            final String href = a.absUrl("href");
//            try {
//                String renderedRespose = null;
//                final OEmbedResponse oembedResponse = this.transformUrl(href);
//                // There was no response or an exception happened
//                if (oembedResponse == null)
//                    continue;
//                // There is a handler for this response
//                else if (this.getHandler().containsKey(oembedResponse.getSource()))
//                    this.getHandler().get(oembedResponse.getSource()).handle(document, a, oembedResponse);
//                // Try to render the response itself and replace the current
//                // anchor
//                else if ((renderedRespose = oembedResponse.render()) != null) {
//                    a.before(renderedRespose);
//                    a.remove();
//                }
//            } catch (OEmbedException e) {
//            }
//        }
//        if (changedBaseUri)
//            document.setBaseUri(null);
//        return document;
//    }

    /**
     * Finds a provider for the given url
     * 
     * @param url
     * @return
     */
    private OEmbedProvider findProvider(final String url) {
        OEmbedProvider rv = null;
        providerLoop: for (OEmbedProvider provider: this.provider.values()) {
            for (String urlScheme: provider.getUrlSchemes()) {
                if (url.matches(urlScheme)) {
                    rv = provider;
                    break providerLoop;
                }
            }
        }
        return rv;
    }

    private OEmbedProvider autodiscoverOembedURIForUrl(final String url) {
        OEmbedProvider rv = null;

        try {
            final HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get, url);
            rv = ex.execute(new ResponseHandler<OEmbedProvider>() {
                public OEmbedProvider handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        LOGGER.warn(String.format("Autodiscovery for %s failed, server returned error %d: %s", url, httpResponse
                                .getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity())));
                    } else {
                        Uri uri = ex.getUri();
                        
                        final Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity(), "UTF-8"),
                                String.format("%s://%s", uri.getScheme(), uri.getAuthority()));
                        for (Element alternate: document.getElementsByAttributeValue("rel", "alternate")) {
                            if (alternate.attr("type").equalsIgnoreCase("application/json+oembed")) {
                                return new AutodiscoveredOEmbedProvider(url, Uri.parse(alternate.absUrl("href")), "json");
                            } else if (alternate.attr("type").equalsIgnoreCase("text/xml+oembed")) {
                                return new AutodiscoveredOEmbedProvider(url, Uri.parse(alternate.absUrl("href")), "xml");
                            }
                        }
                    }
                    
                    return null;
                }
            }).getRight();
        } catch (Exception e) {
            LOGGER.warn(String.format("Autodiscovery for %s failed: %s", url, e.getMessage()), e);
        }

        return rv;
    }

    public Map<String, OEmbedResponseHandler> getHandler() {
        return handler;
    }

    public void setHandler(Map<String, OEmbedResponseHandler> handler) {
        this.handler = handler;
    }

    public Map<String, OEmbedParser> getParser() {
        return parser;
    }

    public void setParser(Map<String, OEmbedParser> parser) {
        this.parser = parser;
    }

    public boolean isAutodiscovery() {
        return autodiscovery;
    }

    public void setAutodiscovery(boolean autodiscovery) {
        this.autodiscovery = autodiscovery;
    }

//    public String getBaseUri() {
//        return baseUri;
//    }
//
//    public void setBaseUri(String baseUri) {
//        this.baseUri = baseUri;
//    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }
}