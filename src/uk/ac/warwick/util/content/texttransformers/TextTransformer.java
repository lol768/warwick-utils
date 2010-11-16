package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Pattern;

/**
 * Trivial interface which modifies the specified text in some way.
 * 
 * Stateless as this is a single op.
 * 
 * @author xusqac
 */
public interface TextTransformer {

    Pattern HEADEND = Pattern.compile("(</head>)",Pattern.CASE_INSENSITIVE);
    Pattern HTMLSTART = Pattern.compile("(<html[^>]*?>)",Pattern.CASE_INSENSITIVE);
    Pattern HEAD_MATCHER = Pattern.compile("<head[^>]*?>(.*?)</head>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    String transform(final String text);
}
