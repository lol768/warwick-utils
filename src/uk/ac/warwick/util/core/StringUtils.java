package uk.ac.warwick.util.core;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Singleton utility class.
 * 
 * @author xusqac
 */
public final class StringUtils {

    public static final String DEFAULT_ENCODING = "ISO-8859-1";

    private static final int HIGH_CHAR = 127;

    private StringUtils() {

    }

    /**
     * Always use this instead of new String(bytes) because of encoding issues.
     * See http://link.csv.warwick.ac.uk:8080/jira/browse/SBTWO-167
     * 
     * @param bytes
     * @return
     */
    public static String create(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        try {
            return new String(bytes, DEFAULT_ENCODING);
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(DEFAULT_ENCODING + " not supported!", e);
        }
    }

    /**
     * Always use this instead of new String(bytes) because of encoding issues.
     * See http://link.csv.warwick.ac.uk:8080/jira/browse/SBTWO-167
     * 
     * @param bytes
     * @return
     */
    public static byte[] create(final String s) {
        String text = s;
        if (s == null || s.length() == 0) {
            text = "";
        }

        try {
            return text.getBytes(DEFAULT_ENCODING);
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(DEFAULT_ENCODING + " not supported!", e);
        }
    }

    /**
     * Surely this has already been implemented? :)
     */
    public static String[] convertToArray(final String s) {
        String[] result = s.split(",");
        return result;
    }

    /**
     * Implementation of String.substring which will never throw a NPE.
     */
    public static String safeSubstring(final String s, final int proposedStart, final int proposedEnd) {
        if (s == null || s.length() == 0) {
            return "";
        }
        String result = "";
        if (proposedStart > proposedEnd) {
            result = "";
        } else if (proposedEnd < 0) {
            result = "";
        } else {
            int start = proposedStart < 0 ? 0 : proposedStart;
            int end = proposedEnd > s.length() ? s.length() : proposedEnd;
            result = s.substring(start, end);
        }
        return result;
    }

    /**
     * Trivial guard to ensure that a string is never null.
     */
    public static String nullGuard(final String s) {
        if (s == null)
            return "";
        return s;
    }

    /**
     * Find the first occurrence of text between startString and endString, OR
     * startString and the end of the input string. eg. given text = "my name is
     * frank, how are you" startString = "is " endString = "," the result would
     * be "frank" A more useful example is finding the what's between "name="
     * and "&" in the string "a=b&name=whatwewant&othername=hello" If a matching
     * string is not found, null is returned.
     * 
     * @param text
     * @param startString
     * @param endString
     * @return
     */
    public static String substringBetween(final String text, final String startString, final String endString) {
        String t = text;
        int start = t.indexOf(startString);
        if (start == -1) {
            return null;
        }
        start += startString.length();
        int end = t.indexOf(endString, start);
        String result;
        if (end == -1) {
            result = t.substring(start);
        } else {
            result = t.substring(start, end);
        }
        return result;
    }

    /**
     * Determine the character that represents the end of common bytes. For
     * example if this is called with "/a/b/c" and "/a/d/e" then it would return
     * 3.
     */
    public static int determineEndOfCommon(final String firstString, final String secondString) {
        byte[] a = StringUtils.create(firstString);
        byte[] b = StringUtils.create(secondString);

        int longestString = a.length > b.length ? a.length : b.length;
        int i = 0;
        for (i = 0; i < longestString; i++) {
            if (i >= a.length || i >= b.length) {
                break;
            } else {
                if (a[i] != b[i]) {
                    break;
                }
            }
        }
        return i;
    }

    public static String convertToString(final List<String> keywords, final String seperator) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < keywords.size(); i++) {
            if (keywords.get(i) != null) {
                if (i > 0) {
                    sb.append(seperator);
                }
                sb.append(keywords.get(i).trim());
            }
        }
        return sb.toString();
    }

    public static String join(Collection<String> s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator<String> iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

    public static List<String> convertCommaOrSpaceDelimitedStringToList(final String theKeywords) {
        ArrayList<String> results = new ArrayList<String>();
        if (!org.springframework.util.StringUtils.hasLength(theKeywords)) {
            return results;
        }

        String keywords = theKeywords.trim();

        String[] commaSeperatedElements = keywords.split(",");
        if (commaSeperatedElements[0] == keywords) { // it couldn't find any
            String[] spaceSeperatedElements = keywords.split(" ");
            addToListButIgnoreEmpty(results, spaceSeperatedElements);
        } else {
            addToListButIgnoreEmpty(results, commaSeperatedElements);
        }

        return results;
    }

    public static List<String> convertCommaDelimitedStringToList(final String theKeywords) {
        ArrayList<String> results = new ArrayList<String>();
        if (!org.springframework.util.StringUtils.hasLength(theKeywords)) {
            return results;
        }

        String keywords = theKeywords.trim();

        String[] commaSeperatedElements = keywords.split(",");
        addToListButIgnoreEmpty(results, commaSeperatedElements);

        return results;
    }

    private static void addToListButIgnoreEmpty(final List<String> list, final String[] elements) {
        for (String s: elements) {
            if (s != null && org.springframework.util.StringUtils.hasLength(s.trim())) {
                list.add(s.trim());
            }
        }
    }

    /**
     * Compacts extraneous whitespace in HTML into single spaces. Multiple
     * spaces and newlines in a row are structurally equivalent to a single
     * space character.
     */
    public static String compactWhitespace(final String text) {
        return text.replaceAll("\\s+", " ");
    }

    /**
     * Returns whether the entire given String is made of whitespace characters
     */
    public static boolean isWhitespace(final String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(final String s) {
        return s == null || s.trim().length() == 0;
    }

    /**
     * Escapes all characters >127, into their respective HTML entities (&#222;
     * etc).
     * 
     * @param input
     * @return
     */
    public static String htmlEscapeHighCharacters(final String input) {
        StringReader reader = new StringReader(input);
        StringBuilder sb = new StringBuilder(input.length());
        int c = 0;
        try {
            do {
                c = reader.read();
                if (c == -1) {
                    break;
                }
                if (c > HIGH_CHAR) {
                    sb.append("&#" + c + ";");
                } else {
                    sb.append((char) c);
                }
            } while (c != -1);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return sb.toString();

    }

    /* remove leading whitespace */
    public static String ltrim(final String source) {
        return Pattern.compile("^\\s+", Pattern.DOTALL).matcher(source).replaceAll("");
    }

    /* remove trailing whitespace */
    public static String rtrim(final String source) {
        return Pattern.compile("\\s+$", Pattern.DOTALL).matcher(source).replaceAll("");
    }

    /**
     * Replace characters having special meaning in regular expressions with
     * their escaped equivalents.
     * <P>
     * The escaped characters include :
     * <ul>
     * <li>.
     * <li>\
     * <li>?, * , and +
     * <li>&
     * <li>:
     * <li>{ and }
     * <li>[ and ]
     * <li>( and )
     * <li>^ and $
     * </ul>
     */
    public static String escapeForRegex(String aRegexFragment) {
        final StringBuilder result = new StringBuilder();

        final StringCharacterIterator iterator = new StringCharacterIterator(aRegexFragment);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            /*
             * All literals need to have backslashes doubled.
             */
            if (character == '.') {
                result.append("\\.");
            } else if (character == '\\') {
                result.append("\\\\");
            } else if (character == '?') {
                result.append("\\?");
            } else if (character == '*') {
                result.append("\\*");
            } else if (character == '+') {
                result.append("\\+");
            } else if (character == '&') {
                result.append("\\&");
            } else if (character == ':') {
                result.append("\\:");
            } else if (character == '{') {
                result.append("\\{");
            } else if (character == '}') {
                result.append("\\}");
            } else if (character == '[') {
                result.append("\\[");
            } else if (character == ']') {
                result.append("\\]");
            } else if (character == '(') {
                result.append("\\(");
            } else if (character == ')') {
                result.append("\\)");
            } else if (character == '^') {
                result.append("\\^");
            } else if (character == '$') {
                result.append("\\$");
            } else if (character == '|') {
                result.append("\\|");
            } else {
                // the char is not a special one
                // add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

}