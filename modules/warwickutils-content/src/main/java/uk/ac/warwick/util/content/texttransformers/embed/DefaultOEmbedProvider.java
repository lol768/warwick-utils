package uk.ac.warwick.util.content.texttransformers.embed;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import uk.ac.warwick.util.httpclient.httpclient4.DefaultHttpRequestDecorator;
import uk.ac.warwick.util.httpclient.httpclient4.HttpRequestDecorator;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.UriBuilder;

import com.google.common.collect.Lists;

public class DefaultOEmbedProvider implements OEmbedProvider, InitializingBean {

    private String name;

    private Uri endpoint;

    private Format format;

    private Integer maxWidth;

    private Integer maxHeight;

    private List<String> urlSchemes = Lists.newArrayList();
    
    /** A decorator that can manipulate / decorate the request before executing */
    private HttpRequestDecorator httpRequestDecorator = new DefaultHttpRequestDecorator();

    public UriBuilder toApiUrl(final Uri url) {
        final UriBuilder apiUrl = new UriBuilder(endpoint).addQueryParameter("url", url.toString());

        // Replace {format} token in path
        if (apiUrl.getPath().contains("%25%7Bformat%7D")) {
            apiUrl.setPath(apiUrl.getPath().replace("%25%7Bformat%7D", format.name()));
        } else if (apiUrl.getPath().contains("%7Bformat%7D")) {
            apiUrl.setPath(apiUrl.getPath().replace("%7Bformat%7D", format.name()));
        } else {
            apiUrl.addQueryParameter("format", format.name());
        }

        if (maxWidth != null)
            apiUrl.addQueryParameter("maxwidth", maxWidth.toString());
        if (maxHeight != null)
            apiUrl.addQueryParameter("maxheight", maxHeight.toString());

        return apiUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Uri apiEndpoint) {
        this.endpoint = apiEndpoint;
    }

    public List<String> getUrlSchemes() {
        return urlSchemes;
    }

    public void setUrlSchemes(List<String> urlSchemes) {
        this.urlSchemes = urlSchemes;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public Integer getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public HttpRequestDecorator getHttpRequestDecorator() {
        return httpRequestDecorator;
    }

    public void setHttpRequestDecorator(HttpRequestDecorator httpRequestDecorator) {
        this.httpRequestDecorator = httpRequestDecorator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(name, "name must not be null");
        Assert.notNull(endpoint, "endpoint must not be null");
        Assert.notNull(format, "format must not be null");
        Assert.notNull(urlSchemes, "urlSchemes must not be null");
        Assert.notNull(httpRequestDecorator, "httpRequestDecorator must not be null");
    }
}