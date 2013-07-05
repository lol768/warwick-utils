package uk.ac.warwick.util.content.texttransformers.embed;

import java.util.List;

import uk.ac.warwick.util.httpclient.httpclient4.DefaultHttpRequestDecorator;
import uk.ac.warwick.util.httpclient.httpclient4.HttpRequestDecorator;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.UriBuilder;

import com.google.common.collect.ImmutableList;

public class AutodiscoveredOEmbedProvider implements OEmbedProvider {

    /** The autodiscovered provider supports only one scheme... */
    private final List<String> urlSchemes;

    private final Uri apiUrl;

    private final String name;

    private final Format format;

    private final HttpRequestDecorator httpRequestDecorator = new DefaultHttpRequestDecorator();

    public AutodiscoveredOEmbedProvider(final Uri originalUrl, final Uri apiUrl, final Format format) {
        this.urlSchemes = ImmutableList.of(originalUrl.toString());
        this.apiUrl = apiUrl;
        this.name = this.apiUrl.getAuthority();
        this.format = format;
    }

    public String getName() {
        return this.name;
    }

    public Format getFormat() {
        return this.format;
    }

    public List<String> getUrlSchemes() {
        return this.urlSchemes;
    }

    public UriBuilder toApiUrl(Uri url) {
        return new UriBuilder(this.apiUrl);
    }

    public HttpRequestDecorator getHttpRequestDecorator() {
        return httpRequestDecorator;
    }

}