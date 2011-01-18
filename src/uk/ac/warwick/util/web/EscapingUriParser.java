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
        try {
            if (StringUtils.hasText(text)) {
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
                    
                    text += query;
                }
                
                if (StringUtils.hasText(fragment)) {
                    text += fragment;
                }
            }
            
            return super.parse(text);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
