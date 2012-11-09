package uk.ac.warwick.util.web;

public abstract class UriUtils {
    
    private UriUtils() {}
    
    /**
     * Sanitise the URI to ensure that when users type www.google.com the Uri is http://www.google.com/
     */
    public static Uri sanitise(Uri url) {
        if (url.getScheme() == null) {
            UriBuilder uriBuilder = new UriBuilder(url).setScheme("http");
            if (uriBuilder.getAuthority() == null && uriBuilder.getPath() != null) {
                String path = uriBuilder.getPath();
                if (path.indexOf("/") == -1) {
                    uriBuilder.setAuthority(path).setPath(null);
                } else {
                    uriBuilder.setAuthority(path.substring(0, path.indexOf("/"))).setPath(path.substring(path.indexOf("/")+1));
                }
            }
            
            return uriBuilder.toUri();
        } else {
            return url;
        }
    }

}
