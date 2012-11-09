package uk.ac.warwick.util.content.texttransformers;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public final class SquareTagTransformerTest {
    
    @Test
    public void extractHeadsNoHeads() throws Exception {
        List<String> heads = Lists.newArrayList();
        String output = AbstractSquareTagTransformer.extractHeads("Here is some random text", heads);
        
        assertEquals("Here is some random text", output);
        assertTrue(heads.isEmpty());
    }
    
    @Test
    public void extractHeadsOneHead() throws Exception {
        List<String> heads = Lists.newArrayList();
        String output = AbstractSquareTagTransformer.extractHeads("<p>Here is some random text.</p>\n\n<head><script></script></head>", heads);
        
        assertEquals("<p>Here is some random text.</p>", output);
        assertEquals(Lists.newArrayList("<script></script>"), heads);
    }
    
    @Test
    public void extractHeadsTwoHeads() throws Exception {
        List<String> heads = Lists.newArrayList();
        String output = AbstractSquareTagTransformer.extractHeads("<head><style>css</style></head>\n\n<p>Here is some random text.</p>\n\n<head><script></script></head>", heads);
        
        assertEquals("<p>Here is some random text.</p>", output);
        assertEquals(Lists.newArrayList("<style>css</style>", "<script></script>"), heads);
    }

}
