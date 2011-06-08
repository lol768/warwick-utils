package uk.ac.warwick.util.content.texttransformers;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import uk.ac.warwick.util.content.MutableContent;

public final class TextPatternTransformerTest {
    
    @Test
    public void applyWithHeads() throws Exception {
        // This transformer includes the content inside the head tag itself.
        TextPatternTransformer transformer = new TextPatternTransformer() {
            @Override
            protected Pattern getPattern() {
                return Pattern.compile("\\[add-head(\\s+.+?[^\\\\])?" + "\\]" + "(.+?)" + "\\[/add-head\\]", Pattern.CASE_INSENSITIVE);
            }
            
            @Override
            protected Callback getCallback() {
                return new Callback() {
                    public String transform(String input, MutableContent mc) {
                        Matcher matcher = getPattern().matcher(input);
                        matcher.matches();
                        
                        String contents = matcher.group(2);
                        
                        return "<html><head>" + contents + "</head><body>" + contents + "</body></html>";
                    }
                };
            }
        };
        
        MutableContent mc = new MutableContent(null, "<p>[add-head]head1[/add-head]</p>\n\n<p>[add-head]head2[/add-head]</p>");
        mc = transformer.apply(mc);
        
        assertEquals("<html><head>head1head2</head><body><p>head1</p>\n\n<p>head2</p></body></html>", mc.getContent());
    }
    
    @Test
    public void applyWithHeadsWithExistingHtml() throws Exception {
        // This transformer includes the content inside the head tag itself.
        TextPatternTransformer transformer = new TextPatternTransformer() {
            @Override
            protected Pattern getPattern() {
                return Pattern.compile("\\[add-head(\\s+.+?[^\\\\])?" + "\\]" + "(.+?)" + "\\[/add-head\\]", Pattern.CASE_INSENSITIVE);
            }
            
            @Override
            protected Callback getCallback() {
                return new Callback() {
                    public String transform(String input, MutableContent mc) {
                        Matcher matcher = getPattern().matcher(input);
                        matcher.matches();
                        
                        String contents = matcher.group(2);
                        
                        return "<html><head>" + contents + "</head><body>" + contents + "</body></html>";
                    }
                };
            }
        };
        
        MutableContent mc = new MutableContent(null, "<html><body><p>[add-head]head1[/add-head]</p>\n\n<p>[add-head]head2[/add-head]</p></body></html>");
        mc = transformer.apply(mc);
        
        assertEquals("<html><head>head1head2</head><body><p>head1</p>\n\n<p>head2</p></body></html>", mc.getContent());
    }
    
    @Test
    public void applyWithHeadsWithExistingHead() throws Exception {
        // This transformer includes the content inside the head tag itself.
        TextPatternTransformer transformer = new TextPatternTransformer() {
            @Override
            protected Pattern getPattern() {
                return Pattern.compile("\\[add-head(\\s+.+?[^\\\\])?" + "\\]" + "(.+?)" + "\\[/add-head\\]", Pattern.CASE_INSENSITIVE);
            }
            
            @Override
            protected Callback getCallback() {
                return new Callback() {
                    public String transform(String input, MutableContent mc) {
                        Matcher matcher = getPattern().matcher(input);
                        matcher.matches();
                        
                        String contents = matcher.group(2);
                        
                        return "<html><head>" + contents + "</head><body>" + contents + "</body></html>";
                    }
                };
            }
        };
        
        MutableContent mc = new MutableContent(null, "<html><head>something</head><body><p>[add-head]head1[/add-head]</p>\n\n<p>[add-head]head2[/add-head]</p></body></html>");
        mc = transformer.apply(mc);
        
        assertEquals("<html><head>somethinghead1head2</head><body><p>head1</p>\n\n<p>head2</p></body></html>", mc.getContent());
    }

}
