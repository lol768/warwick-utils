package uk.ac.warwick.util.web.filter.stack;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attempts to match request paths to URL patterns according to the
 * Servlet spec (SRV.11)
 */
public final class FilterMappingParserImpl implements FilterMappingParser {

    private static final Pattern SLASH_PREFIXED_WILDCARDS_AT_END = Pattern.compile("(?<!\\.)\\/\\*$");
    private static final Pattern ONE_CHARACTER_WILDCARD = Pattern.compile("\\?");
    private static final Pattern MATCH_ONE_CHARACTER = Pattern.compile(".");
    private static final Pattern EXTENSION_SUFFIXED_WILDCARD = Pattern.compile("\\.\\*(?!\\.)");
    private static final Pattern MATCH_WITHOUT_SLASHES = Pattern.compile("\\.[^\\/]+");
    private static final Pattern EXTENSION_PREFIXED_WILDCARD = Pattern.compile("(?<!\\.)\\*\\.");
    private static final Pattern MATCH_DOT_PREFIXED_ANYTHING = Pattern.compile(".+\\.");
    private static final Pattern MATCH_WILDCARD_WITH_OPTIONAL_SLASH = Pattern.compile("(?:\\/.*)?");
    private static final Pattern NON_EXTENSION_WILDCARD = Pattern.compile("(?<!\\.)\\*(?!\\.)");
    private static final Pattern MATCH_ANYTHING = Pattern.compile(".*");

    public boolean matches(String requestPath, String mapping) {
        return requestPath.matches(handleSlashPrefixedWildcards(mapping));
    }

    private static String handleSlashPrefixedWildcards(String str) {
        // we allow /path/* at the end of the mapping to match /path but not /pathsabc
        // this should match behaviour in the old matchesPrefix
        return "^" + genericHandle(str, FilterMappingParserImpl::handleGeneralWildcards, SLASH_PREFIXED_WILDCARDS_AT_END, MATCH_WILDCARD_WITH_OPTIONAL_SLASH, 1) + "$";
    }

    private static String handleGeneralWildcards(String str) {
        return genericHandle(str, FilterMappingParserImpl::handleOneCharacterWildcards, NON_EXTENSION_WILDCARD, MATCH_ANYTHING, 1);
    }

    private static String handleOneCharacterWildcards(String str) {
        return genericHandle(str, FilterMappingParserImpl::handleStartingExtensions, ONE_CHARACTER_WILDCARD, MATCH_ONE_CHARACTER, 1);
    }

    private static String handleStartingExtensions(String str) {
        return genericHandle(str, FilterMappingParserImpl::handleEndingExtensions, EXTENSION_SUFFIXED_WILDCARD, MATCH_WITHOUT_SLASHES, 2);
    }

    private static String handleEndingExtensions(String str) {
        // note we allow *.css to match /foo/bar/styles.css
        return genericHandle(str, Pattern::quote, EXTENSION_PREFIXED_WILDCARD, MATCH_DOT_PREFIXED_ANYTHING, 2);
    }

    private static String genericHandle(String str, Function<String, String> callback, Pattern pattern, Pattern patternToAppend, int offset) {
        StringBuilder intermediateRegex = new StringBuilder();
        Matcher matcher = pattern.matcher(str);
        int beginning = 0;
        while (matcher.find()) {
            int start = matcher.start();
            intermediateRegex.append(callback.apply(str.substring(beginning, start))).append(patternToAppend);
            beginning = matcher.end();
        }
        if (!str.substring(beginning).isEmpty()) {
            intermediateRegex.append(callback.apply((str.substring(beginning))));
        }
        return intermediateRegex.toString();
    }
}
