package uk.ac.warwick.util.web;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import org.apache.commons.codec.net.URLCodec;

import uk.ac.warwick.util.core.StringUtils;

public class EscapingUriParser extends DefaultUriParser {
    
    private static final BitSet ALLOWED_QUERYSTRING_CHARACTERS;
    private static final BitSet ALLOWED_PATH_CHARACTERS;
    
    static {
        ALLOWED_QUERYSTRING_CHARACTERS = new BitSet(256);
        
        //standard URL characters
        ALLOWED_QUERYSTRING_CHARACTERS.set(';');
        ALLOWED_QUERYSTRING_CHARACTERS.set('/');
        ALLOWED_QUERYSTRING_CHARACTERS.set('?');
        ALLOWED_QUERYSTRING_CHARACTERS.set(':');
        ALLOWED_QUERYSTRING_CHARACTERS.set('@');
        ALLOWED_QUERYSTRING_CHARACTERS.set('&');
        ALLOWED_QUERYSTRING_CHARACTERS.set('=');
        ALLOWED_QUERYSTRING_CHARACTERS.set('+');
        ALLOWED_QUERYSTRING_CHARACTERS.set('$');
        ALLOWED_QUERYSTRING_CHARACTERS.set(',');
        ALLOWED_QUERYSTRING_CHARACTERS.set('-');
        ALLOWED_QUERYSTRING_CHARACTERS.set('_');
        ALLOWED_QUERYSTRING_CHARACTERS.set('.');
        ALLOWED_QUERYSTRING_CHARACTERS.set('!');
        ALLOWED_QUERYSTRING_CHARACTERS.set('~');
        ALLOWED_QUERYSTRING_CHARACTERS.set('*');
        ALLOWED_QUERYSTRING_CHARACTERS.set('\'');
        ALLOWED_QUERYSTRING_CHARACTERS.set('(');
        ALLOWED_QUERYSTRING_CHARACTERS.set(')');
        
        // ignore already escaped characters
        ALLOWED_QUERYSTRING_CHARACTERS.set('%');
        
        // alphanumeric
        for (int i = 'a'; i <= 'z'; i++) {
            ALLOWED_QUERYSTRING_CHARACTERS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            ALLOWED_QUERYSTRING_CHARACTERS.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            ALLOWED_QUERYSTRING_CHARACTERS.set(i);
        }
        
        ALLOWED_PATH_CHARACTERS = new BitSet(256);
        
        //standard URL characters
        ALLOWED_PATH_CHARACTERS.set(';');
        ALLOWED_PATH_CHARACTERS.set('/');
        ALLOWED_PATH_CHARACTERS.set('?');
        ALLOWED_PATH_CHARACTERS.set(':');
        ALLOWED_PATH_CHARACTERS.set('@');
        ALLOWED_PATH_CHARACTERS.set('&');
        ALLOWED_PATH_CHARACTERS.set('=');
        ALLOWED_PATH_CHARACTERS.set('+');
        ALLOWED_PATH_CHARACTERS.set('$');
        ALLOWED_PATH_CHARACTERS.set(',');
        ALLOWED_PATH_CHARACTERS.set('-');
        ALLOWED_PATH_CHARACTERS.set('_');
        ALLOWED_PATH_CHARACTERS.set('.');
        ALLOWED_PATH_CHARACTERS.set('!');
        ALLOWED_PATH_CHARACTERS.set('~');
        ALLOWED_PATH_CHARACTERS.set('*');
        ALLOWED_PATH_CHARACTERS.set('\'');
        ALLOWED_PATH_CHARACTERS.set('(');
        ALLOWED_PATH_CHARACTERS.set(')');
        
        // ignore already escaped characters
        ALLOWED_PATH_CHARACTERS.set('%');
        
        // alphanumeric
        for (int i = 'a'; i <= 'z'; i++) {
            ALLOWED_PATH_CHARACTERS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            ALLOWED_PATH_CHARACTERS.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            ALLOWED_PATH_CHARACTERS.set(i);
        }
    }

    @Override
    public Uri parse(String text) {
        return super.parse(escape(text));
    }

    @Override
    public boolean isOpaque(String text) {
        return super.isOpaque(escape(text));
    }
    
    private static String escape(String text) {
        try {
            if (StringUtils.hasText(text)) {
                text = text.trim();
                
                String path = text;
                String query = "";
                String fragment = "";
                if (text.indexOf("?") != -1) {
                    path = text.substring(0, text.indexOf("?"));
                    query = text.substring(text.indexOf("?"));
                    
                    if (query.indexOf("#") != -1) {
                        fragment = query.substring(query.indexOf("#"));
                        query = query.substring(0, query.indexOf("#"));
                    }
                } else if (text.indexOf("#") != -1) {
                    path = text.substring(0, text.indexOf("#"));
                    fragment = text.substring(text.indexOf("#"));
                }
                
                text = new String(URLCodec.encodeUrl(ALLOWED_PATH_CHARACTERS, path.getBytes("UTF-8")), "UTF-8");
                
                if (StringUtils.hasText(query)) {
                    query = new String(URLCodec.encodeUrl(ALLOWED_QUERYSTRING_CHARACTERS, query.getBytes("UTF-8")), "UTF-8");
                    query = scanEscapes(query);
                    
                    text += query;
                }
                
                if (StringUtils.hasText(fragment)) {
                    fragment = new String(URLCodec.encodeUrl(ALLOWED_QUERYSTRING_CHARACTERS, fragment.substring(1).getBytes("UTF-8")), "UTF-8");
                    fragment = "#" + scanEscapes(fragment);
                    
                    text += fragment;
                }
            }
            
            return text;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String scanEscapes(String query) {
        if (query.indexOf("%") == -1) {
            return query;
        }
        
        StringBuilder sb = new StringBuilder(query.length());
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            if (c == '%') {
                // scan the escape sequence
                if (i + 3 <= query.length() 
                && match(query.charAt(i + 1), L_HEX, H_HEX)
                && match(query.charAt(i + 2), L_HEX, H_HEX)) {
                    sb.append(c);
                } else {
                    // Append an escaped %
                    sb.append("%25");
                }
            } else {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
    
    // digit    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
    //            "8" | "9"
    private static final long L_DIGIT = lowMask('0', '9');
    
    // hex           = digit | "A" | "B" | "C" | "D" | "E" | "F" |
    //                         "a" | "b" | "c" | "d" | "e" | "f"
    private static final long L_HEX = L_DIGIT;
    private static final long H_HEX = highMask('A', 'F') | highMask('a', 'f');
    
    // Compute a low-order mask for the characters
    // between first and last, inclusive
    private static long lowMask(char first, char last) {
        long m = 0;
        int f = Math.max(Math.min(first, 63), 0);
        int l = Math.max(Math.min(last, 63), 0);
        for (int i = f; i <= l; i++)
            m |= 1L << i;
        return m;
    }

    // Compute a high-order mask for the characters
    // between first and last, inclusive
    private static long highMask(char first, char last) {
        long m = 0;
        int f = Math.max(Math.min(first, 127), 64) - 64;
        int l = Math.max(Math.min(last, 127), 64) - 64;
        for (int i = f; i <= l; i++)
            m |= 1L << i;
        return m;
    }
    
    // Tell whether the given character is permitted by the given mask pair
    private static boolean match(char c, long lowMask, long highMask) {
        if (c < 64)
            return ((1L << c) & lowMask) != 0;
        if (c < 128)
            return ((1L << (c - 64)) & highMask) != 0;
        return false;
    }

}
