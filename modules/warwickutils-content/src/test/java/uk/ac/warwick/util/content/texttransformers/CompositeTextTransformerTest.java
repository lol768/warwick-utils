package uk.ac.warwick.util.content.texttransformers;

import java.util.Arrays;

import junit.framework.TestCase;
import uk.ac.warwick.util.content.MutableContent;

public class CompositeTextTransformerTest extends TestCase {
    public void testItWorks() {
        TextTransformer transformer = new CompositeTextTransformer(Arrays.asList(new TextTransformer[]{
            new TextTransformer() {
                public MutableContent apply(MutableContent mc) {
                    mc.setContent(mc.getContent().toUpperCase());
                    
                    return mc;
                }      
            },
            new TextTransformer() {
                public MutableContent apply(MutableContent mc) {
                    mc.setContent(mc.getContent() + "append");
                    
                    return mc;
                }      
            }
        }));
        
        String input = "test string";
        String expected = "TEST STRINGappend";
        
        assertEquals(expected, transformer.apply(new MutableContent(null, input)).getContent());
    }
}
