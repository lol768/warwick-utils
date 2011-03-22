package uk.ac.warwick.util.web;

import java.net.URI;

import org.springframework.util.Assert;

public class OpaqueUri extends Uri {
    
    private static final long serialVersionUID = 615885427758771071L;
    
    private final URI uri;
    
    OpaqueUri(URI uri) {
        super(new UriBuilder(Uri.parse("#")));
        
        Assert.isTrue(uri.isOpaque());
        
        this.uri = uri;
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public URI toJavaUri() {
        return uri;
    }

    @Override
    public Uri resolve(Uri relative) {
        return relative;
    }

    @Override
    public boolean isAbsolute() {
        return true;
    }

    @Override
    public String getScheme() {
        return uri.getScheme();
    }

}
