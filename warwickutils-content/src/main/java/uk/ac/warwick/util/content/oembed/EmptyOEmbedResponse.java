package uk.ac.warwick.util.content.oembed;

import uk.ac.warwick.util.cache.CustomCacheExpiry;

@CustomCacheExpiry(24 * 60 * 60)
public final class EmptyOEmbedResponse extends OEmbedResponse {
   
    private static final long serialVersionUID = -2761254062065592608L;

    public EmptyOEmbedResponse() {
        super();
        setEmpty(true);
    }
    
}
