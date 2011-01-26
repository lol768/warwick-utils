package uk.ac.warwick.util.web.filter.stack;

import uk.ac.warwick.util.core.spring.FileUtils;

/**
 * Attempts to match request paths to URL patterns according to the
 * Servlet spec (SRV.11)
 */
public final class FilterMappingParserImpl implements FilterMappingParser {

    public boolean matches(String requestPath, String mapping) {
        return (matchesPrefix(requestPath, mapping)
                || matchesExtension(requestPath, mapping))
                || matchesExact(requestPath, mapping);
    }

    private static boolean matchesExtension(String requestPath, String mapping) {
        return (mapping.startsWith("*.") && FileUtils.extensionMatches(requestPath, mapping.substring(2)));
    }

    /**
     * Match patterns starting / and ending /*. The specification says that it
     * should match each part of the path with the pattern, which implies that
     * it shouldn't matter if there is a trailing slash in the pattern but not
     * in the request. Otherwise SBTWO-3349 happens.
     */
    private static boolean matchesPrefix(String requestPath, String mapping) {
        return (mapping.startsWith("/") && mapping.endsWith("/*") 
                && (
                        requestPath.startsWith(mapping.substring(0,mapping.length()-1))
                        || (requestPath+"/").startsWith(mapping.substring(0,mapping.length()-1))
                        ));
    }
    
    private static boolean matchesExact(String requestPath, String mapping) {
        return (mapping.equals(requestPath));
    }

    // it turns out that this is only used for the default servlet mapping -
    // it isn't interpreted as "always run" for filters - for that use "/*"
//    private static boolean isRoot(String mapping) {
//        return mapping.equals("/");
//    }
}
