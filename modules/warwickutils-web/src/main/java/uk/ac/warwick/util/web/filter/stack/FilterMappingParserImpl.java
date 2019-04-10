package uk.ac.warwick.util.web.filter.stack;

import org.springframework.util.AntPathMatcher;
import uk.ac.warwick.util.core.spring.FileUtils;

/**
 * Attempts to match request paths to URL patterns according to the
 * Servlet spec (SRV.11)
 */
public final class FilterMappingParserImpl implements FilterMappingParser {

    public boolean matches(String requestPath, String mapping) {
        return (matchesPrefix(requestPath, mapping)
                || matchesExtension(requestPath, mapping))
                || matchesExact(requestPath, mapping)
                || matchesNonExtensionSpanningWildcardPrefix(requestPath, mapping);
    }

    /**
     * Allows /foo* to match paths spanning multiple segments,
     * e.g. /foo/bar/x.htm
     *
     * Note that a pattern of /foo/bar.* will not match against
     * /foo/bar.txt/other/directories/
     *
     * @param requestPath The request path
     * @param mapping Mapping string
     * @return Whether there was a match
     */
    private boolean matchesNonExtensionSpanningWildcardPrefix(String requestPath, String mapping) {
        AntPathMatcher apm = new AntPathMatcher();
        if (apm.isPattern(mapping)) {
            return apm.match(mapping, requestPath);
        }
        return false;
    }

    private static boolean matchesExtension(String requestPath, String mapping) {
        return (mapping.startsWith("*.") && FileUtils.extensionMatches(requestPath, mapping.substring(2))) ||
               (mapping.endsWith(".*") && mapping.substring(0, mapping.length() - 2).equals(FileUtils.getFileNameWithoutExtension(requestPath)));
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
