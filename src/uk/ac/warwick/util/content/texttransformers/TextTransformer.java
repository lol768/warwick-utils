package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Pattern;

import com.google.common.base.Function;

import uk.ac.warwick.util.content.MutableContent;

/**
 * Trivial interface which modifies the specified text in some way.
 * 
 * Stateless as this is a single op.
 * 
 * @author xusqac
 */
public interface TextTransformer extends Function<MutableContent, MutableContent> {
    Pattern HEADEND = Pattern.compile("(</head>)",Pattern.CASE_INSENSITIVE);
    Pattern HTMLSTART = Pattern.compile("(<html[^>]*?>)",Pattern.CASE_INSENSITIVE);
    Pattern HEAD_MATCHER = Pattern.compile("<head[^>]*?>(.*?)</head>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
}
