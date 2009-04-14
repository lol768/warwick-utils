package uk.ac.warwick.util.core;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
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

    private static final Pattern TRAILING_SPACE_PATTERN = Pattern.compile("\\s+$", Pattern.DOTALL);

	private static final Pattern LEADING_SPACE_PATTERN = Pattern.compile("^\\s+", Pattern.DOTALL);

	public static final String DEFAULT_ENCODING = "ISO-8859-1";

    private static final int HIGH_CHAR = 127;

    private StringUtils() {

    }

    /**
     * Always use this instead of new String(bytes) because of encoding issues.
     * See SBTWO-167
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
     * See SBTWO-167
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
     * Join an array of String objects into a single string, joined
     * by the given delimiter.
     */
    public static String join(String[] strings, String delimiter) {
    	StringBuilder buffer = new StringBuilder();
        int len = strings.length;
		for (int i=0;i<len;i++) {
            buffer.append(strings[i]);
            if (i+1 < len) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
       
    }
    
    /**
     * Joins a collection of strings into one string using the specified delimiter.
     * 
     * This version doesn't trim the strings as they go in.
     */
    public static String join(Collection<String> s, String delimiter) {
    	return join(s, delimiter, false);
    }
 
    /**
     * @param trim If true, each element is trim()ed before it is added.
     */
    public static String join(Collection<String> s, String delimiter, boolean trim) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = s.iterator();
        while (iter.hasNext()) {
        	String string = iter.next();
        	if (trim) { string = string.trim(); }
            builder.append(string);
            if (iter.hasNext()) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }
    
    /**
     *  Basically join(keywords, separator) but it trims each value as well.
     */
    public static String convertToString(final Collection<String> keywords, final String seperator) {    	
    	return join(keywords, seperator, true);
    }
    
    /**
     * Basically keywords.split(",") but it also trims each value.
     */
    public static List<String> convertCommaDelimitedStringToList(final String keywords) {
        ArrayList<String> results = new ArrayList<String>();
        if (!hasText(keywords)) {
            return results;
        }
        String[] commaSeperatedElements = keywords.split(",");
        for (String s: commaSeperatedElements) {
		    if (hasText(s)) {
		        results.add(s.trim());
		    }
		}
        return results;
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
            start = start > s.length() ? s.length() : start;
            int end = proposedEnd > s.length() ? s.length() : proposedEnd;
            result = s.substring(start, end);
        }
        return result;
    }
    
    public static String safeSubstring(final String s, final int proposedStart) {
    	if (s == null || s.length() == 0) {
            return "";
        }
        String result;
        int l = s.length();
        if (proposedStart > l) {
            result = "";
        } else {
        	int start = proposedStart < 0 ? 0 : proposedStart;
            start = start > l ? l : start;
            result = s.substring(start);
        }
        return result;
    }

    /**
     * Trivial guard to ensure that a string is never null.
     */
    public static String nullGuard(final String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    /**
     * Find the first occurrence of text between startString and endString, OR
     * startString and the end of the input string. eg. given text = "my name is
     * frank, how are you" startString = "is " endString = "," the result would
     * be "frank" A more useful example is finding the what's between "name="
     * and "&" in the string "a=b&name=whatwewant&othername=hello" If a matching
     * string is not found, null is returned.
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
        char[] a = firstString.toCharArray();
        char[] b = secondString.toCharArray();

        int longestString = Math.max(a.length, b.length);
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


    /**
     * Compacts extraneous whitespace in HTML into single spaces. Multiple
     * spaces and newlines in a row are structurally equivalent to a single
     * space character.
     */
    public static String compactWhitespace(final String text) {
        return text.replaceAll("\\s+", " ");
    }

    /**
     * Escapes all non-ASCII characters into HTML entities, so you
     * can output the result into any HTML page.
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
        return LEADING_SPACE_PATTERN.matcher(source).replaceAll("");
    }

    /* remove trailing whitespace */
    public static String rtrim(final String source) {
        return TRAILING_SPACE_PATTERN.matcher(source).replaceAll("");
    }

	
	/**
	 * Returns if the string is non-null and has any characters.
	 * A string containing just whitespace will return true.
	 * 
	 * @see #hasText(String)
	 */
	public static boolean hasLength(String s) {
		return (s != null && s.length() > 0);
	}

	/**
	 * Returns if the string is non-null and has any non-whitespace
	 * characters. This is generally best for validating whether a user
	 * has inputted anything.
	 * A string containing just whitespace will return false.
	 */
	public static boolean hasText(String s) {
		return (s != null && s.trim().length() > 0);
	}

	/**
     * Returns whether the entire given String is made of whitespace characters,
     * including when the string has no characters.
     * The logical opposite of {@link #hasText(String)}.
     * 
     * @deprecated Please use {@link #hasText(String)} instead.
     */
    public static boolean isWhitespace(final String text) {
        return !hasText(text);
    }

    /**
     * Identical to {@link #isWhitespace(String)}.
     * 
     * @deprecated Please use {@link #hasText(String)} instead.
     */
    public static boolean isEmpty(final String s) {
        return isWhitespace(s);
    }
 

}