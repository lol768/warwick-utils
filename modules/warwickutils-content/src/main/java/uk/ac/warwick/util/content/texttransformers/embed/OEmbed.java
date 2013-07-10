package uk.ac.warwick.util.content.texttransformers.embed;

import uk.ac.warwick.util.web.Uri;

public interface OEmbed {
    String CACHE_NAME = "OEmbedCache";

    /**
     * Transforms the given URL into an OEmbedResponse. Returns null if
     * there is no provider configured for this url.
     * @param url
     * @return
     * @throws OEmbedException
     */
    OEmbedResponse transformUrl(Uri uri) throws OEmbedException;
}
