package uk.ac.warwick.util.content.texttransformers.embed;

import org.springframework.util.Assert;

import uk.ac.warwick.util.content.texttransformers.embed.OEmbedProvider.Format;
import uk.ac.warwick.util.httpclient.httpclient4.HttpRequestDecorator;
import uk.ac.warwick.util.web.Uri;

import com.google.common.collect.Lists;

public class OEmbedProviderBuilder {
    private final DefaultOEmbedProvider oembedProvider = new DefaultOEmbedProvider();

    public OEmbedProviderBuilder withName(final String name) {
        this.oembedProvider.setName(name);
        return this;
    }

    public OEmbedProviderBuilder withFormat(final Format format) {
        this.oembedProvider.setFormat(format);
        return this;
    }

    public OEmbedProviderBuilder withEndpoint(final Uri endpoint) {
        this.oembedProvider.setEndpoint(endpoint);
        return this;
    }

    public OEmbedProviderBuilder withUrlSchemes(final String... urlSchemes) {
        if (urlSchemes != null) {
            this.oembedProvider.getUrlSchemes().addAll(Lists.newArrayList(urlSchemes));
        }

        return this;
    }

    public OEmbedProviderBuilder withMaxHeight(final int maxHeight) {
        this.oembedProvider.setMaxHeight(maxHeight);
        return this;
    }

    public OEmbedProviderBuilder withMaxWidth(final int maxWidth) {
        this.oembedProvider.setMaxWidth(maxWidth);
        return this;
    }

    public OEmbedProviderBuilder withHttpRequestDecorator(HttpRequestDecorator httpRequestDecorator) {
        Assert.notNull(httpRequestDecorator, "HttpRequestDecorator may not be null!");
        this.oembedProvider.setHttpRequestDecorator(httpRequestDecorator);
        return this;
    }

    public OEmbedProvider build() {
        return oembedProvider;
    }

}