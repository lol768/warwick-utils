package uk.ac.warwick.util.content.texttransformers;

import java.util.Arrays;

import junit.framework.TestCase;

public class CompositeTextTransformerTest extends TestCase {
    public void testItWorks() {
        TextTransformer transformer = new CompositeTextTransformer(Arrays.asList(new TextTransformer[]{
                new TextTransformer() {
                    public String transform(String text) {
                        return text.toUpperCase();
                    }      
                },
                new TextTransformer() {
                    public String transform(String text) {
                        return text + "append";
                    }      
                }
        }));
        
        String input = "test string";
        String expected = "TEST STRINGappend";
        
        assertEquals(expected, transformer.transform(input));
    }
}
