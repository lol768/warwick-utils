package uk.ac.warwick.util.web.filter.stack;

import uk.ac.warwick.util.core.spring.FileUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attempts to match request paths to URL patterns according to the
 * Servlet spec (SRV.11)
 */
public final class FilterMappingParserImpl implements FilterMappingParser {

    private final Pattern compiledPattern = Pattern.compile("(?<!\\.)\\*(?!\\.)");

    public boolean matches(String requestPath, String mapping) {
        return (matchesPrefix(requestPath, mapping)
                || matchesExtension(requestPath, mapping))
                || matchesExact(requestPath, mapping)
                || matchesNonExtensionSpanningWildcardPrefix(requestPath, mapping);
    }

    /**
     * Allows /foo* to match paths spanning multiple segments, e.g. /foo/bar/x.htm
     * Note that a pattern of /foo/bar.* will not match against
     * /foo/bar.txt/other/directories/
     *
     * @param requestPath The request path
     * @param mapping Mapping string
     * @return Whether there was a match
     */
    private boolean matchesNonExtensionSpanningWildcardPrefix(String requestPath, String mapping) {
        char lookBehind = 0;

        int indexMapping = 0;
        int indexRequestPath = 0;

        int backtrackMappingIndex = -1;
        int backtrackCharacterIndex = -1;

        while (true) {
            char requestPathChar = requestPath.charAt(indexRequestPath++);
            char mappingChar = mapping.charAt(indexMapping++);
            if (indexMapping > 1) {
                lookBehind = mapping.charAt(indexMapping - 2);
            }
            switch (mappingChar) {
                case '?':
                    if (indexMapping == mapping.length()) {
                        return true;
                    }
                    break;
                case '*':
                    if (lookBehind != '.') {
                        if (indexMapping == mapping.length()) {
                            return true;
                        }
                        backtrackMappingIndex = indexMapping;
                        backtrackCharacterIndex = --indexRequestPath;
                        continue;
                    }
                default:
                    if (mappingChar != requestPathChar) {
                        if (backtrackMappingIndex == -1 || indexRequestPath == requestPath.length()) {
                            return false;
                        } else {
                            indexMapping = backtrackMappingIndex;
                            // advance cursor one, try again
                            indexRequestPath = ++backtrackCharacterIndex;
                        }
                    } else if (indexMapping == mapping.length() && indexRequestPath == requestPath.length()) {
                        return true;
                    } else if (indexMapping == mapping.length() || indexRequestPath == requestPath.length()) {
                        return false;
                    }
            }
        }
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
