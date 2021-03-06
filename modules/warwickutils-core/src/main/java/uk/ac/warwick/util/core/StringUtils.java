package uk.ac.warwick.util.core;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
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

	public static final String DEFAULT_ENCODING = "UTF-8";
	public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);

    private static final int HIGH_CHAR = 127;

    // Pattern to match an HTML entity where the initial & has been escaped
    private static final String ESCAPED_AMP_ENTITIES = "&amp;" +
    		"(" +
    		"#x[0-9A-Fa-f]+" + // hex entities
    		"|" +
    		"#[0-9]+" + // decimal entities
    		"|" +
    		"[A-Za-z0-9]+" + // named entities
    		");";

    private StringUtils() {

    }

    /**
     * Never use <code>new String(bytes)</code> - use this if you
     * KNOW the bytes to be in {@value #DEFAULT_ENCODING}, otherwise
     * use the two-argument String constructor to specify the charset
     * after finding out what it is. Never assume a charset. 
     */
    public static String create(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return new String(bytes, DEFAULT_CHARSET);
    }

    /**
     * Never use <code>s.getBytes()</code> - use this if you
     * KNOW that you want to get an array of {@value #DEFAULT_ENCODING}
     * bytes. Otherwise use the two-argument method to specify the
     * charset after finding out what it is.
     * 
     * Never assume a charset!
     *
     * @deprecated Use {@link String#getBytes(Charset)}
     */
    @Deprecated
    public static byte[] create(final String s) {
        return nullGuard(s).getBytes(DEFAULT_CHARSET);
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
     * Same as above but ignores escaped commas and removes escape backslashes
     * Split using commas no immediately preceded by a backslash
     */
    public static List<String> convertUnescapedCommaDelimitedStringToList(final String keywords) {
        ArrayList<String> results = new ArrayList<String>();
        if (!hasText(keywords)) {
            return results;
        }
        String[] commaSeperatedElements = keywords.split("(?<!\\\\),");
        for (String s: commaSeperatedElements) {
            if (hasText(s)) {
                results.add(s.replace("\\,", ",").trim());
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
     * Analogue of String.contains(), ignoring whitespace differences.
     * Operates on Strings only, rather than other CharSequence types
     */
    public static boolean containsSpaceInsensitive(String srcString, String searchString) {
        return compactWhitespace(srcString.trim()).contains(compactWhitespace(searchString.trim()));
    }

    /**
     * Analogue of String.equals(), ignoring whitespace differences.
     * Operates on Strings only, rather than other CharSequence types
     */
    public static boolean equalsSpaceInsensitive(String aString, String bString) {
        return compactWhitespace(aString.trim()).equals(compactWhitespace(bString.trim()));
    }

    /**
     * Escapes all non-ASCII characters into HTML entities, so you
     * can output the result into any HTML page. Existing HTML entities and
     * special HTML characters like angle brackets are not changed.
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
                    if (Character.isHighSurrogate((char)c)) {
                        // Character on astral plane is encoded as 2 chars, so grab
                        // the next one and read them together as a single code point
                        int point = Character.toCodePoint((char)c, (char)reader.read());
                        sb.append("&#" + point + ";");
                    } else {
                        sb.append("&#" + c + ";");
                    }
                } else {
                    sb.append((char) c);
                }
            } while (c != -1);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return sb.toString();

    }

    /**
     * Converts a small set of characters into HTML entities:
     * 
     * - <
     * - >
     * - & when not part of an HTML entity (eg A&E, Penn & Teller)
     * 
     * This basically disables any tags, while still allowing the use of
     * HTML entities for non-ASCII characters.
     * 
     * DOESN'T escape any non-ASCII characters. See {@link #htmlEscapeHighCharacters(String)}.
     */
    public static String htmlEscapeSpecialCharacters(final String input) {
        /*
         * Ampersand encoding works by encoding ALL ampersands first, then
         * reversing this for any that starts an HTML entity.     
         */
        return input
            .replaceAll("&", "&amp;")
            .replaceAll(ESCAPED_AMP_ENTITIES, "&$1;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
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
     * Copy the contents of the given Reader into a String.
     * Closes the reader when done.
     * @param in the reader to copy from
     * @return the String that has been copied to
     * @throws IOException in case of I/O errors
     */
    public static String copyToString(Reader in) throws IOException {
        StringWriter out = new StringWriter();

        try {
            int byteCount = 0;
            char[] buffer = new char[8192];
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
        } finally {
            try {
                in.close();
            } catch (IOException ex) {}
            try {
                out.close();
            } catch (IOException ex) {}
        }

        return out.toString();
    }

}
