package uk.ac.warwick.util.content.texttransformers.embed;

import java.util.List;

import uk.ac.warwick.util.httpclient.httpclient4.HttpRequestDecorator;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.UriBuilder;

public interface OEmbedProvider {

    enum Format {
        json, xml
    }

    String getName();

    Format getFormat();

    List<String> getUrlSchemes();

    UriBuilder toApiUrl(final Uri url);
    
    HttpRequestDecorator getHttpRequestDecorator();

}