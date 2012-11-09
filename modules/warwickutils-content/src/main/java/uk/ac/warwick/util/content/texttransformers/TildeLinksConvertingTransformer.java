package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.content.MutableContent;

public class TildeLinksConvertingTransformer implements TextTransformer {
    
    private static final Pattern PATTERN = Pattern.compile("\\s(?:href|src)=([\"'])(.*?~.*?)\\1", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public MutableContent apply(MutableContent mc) {
        String text = mc.getContent();
        
        StringBuffer sb = new StringBuffer();
        Matcher m = PATTERN.matcher(text);

        while (m.find()) {
            m.appendReplacement(sb, m.group().replace("~", "%7E"));
        }

        m.appendTail(sb);

        mc.setContent(sb.toString());
        return mc;
    }
}
