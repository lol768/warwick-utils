package uk.ac.warwick.util.content.texttransformers;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer.Callback;

public class AbstractSquareBracketTagTransformerTest extends TestCase {

    private SquareBracketTransformer transformer;
    
    @Override
    public void setUp() throws Exception {
    	this.transformer = new SquareBracketTransformer();
    	this.transformer2 = new MultiTagSquareBracketTransformer();
    	this.transformer3 = new BlockLevelSquareBracketTransformer();
    }

    public void testStandard() {
        String input = "[tag-name]contents[/tag-name]";
        transformer.apply(new MutableContent(null, input));

        assertEquals("contents", transformer.contents);
    }

    public void testAttributes() {
        String input = "[tag-name allowed1=\"one\" allowed2='two' allowed3=three] the contents [/tag-name]";
        transformer.apply(new MutableContent(null, input));

        assertEquals(" the contents ", transformer.contents);

        assertTrue(transformer.parameters.containsKey("allowed1"));
        assertTrue(transformer.parameters.containsKey("allowed2"));
        assertFalse(transformer.parameters.containsKey("allowed3"));

        assertEquals(transformer.parameters.get("allowed1"), "one");
        assertEquals(transformer.parameters.get("allowed2"), "two");
        assertEquals(transformer.parameters.get("allowed3"), null);
    }
    
    public void testNonBreakingSpaces() {
    	String input = "[tag-name allowed1=one&nbsp; allowed2='two']bam[/tag-name]";
        transformer.apply(new MutableContent(null, input));
        
        assertTrue(transformer.parameters.containsKey("allowed1"));
        assertTrue(transformer.parameters.containsKey("allowed2"));
        assertEquals("one", transformer.parameters.get("allowed1"));
        assertEquals("two", transformer.parameters.get("allowed2"));
    }

    public void testEscapedAttributes() {
        String input = "[tag-name regex='^\\[A-Za-z0-9\\]+$']\nSome escaped stuff ^\\[A-Za-z0-9\\]+$\n[/tag-name]";
        transformer.apply(new MutableContent(null, input));

        assertEquals("\nSome escaped stuff ^\\[A-Za-z0-9\\]+$\n", transformer.contents);

        assertTrue(transformer.parameters.containsKey("regex"));

        String regex = (String) transformer.parameters.get("regex");
        regex = regex.replaceAll("\\\\\\[", "[");
        regex = regex.replaceAll("\\\\]", "]");

        assertEquals("^[A-Za-z0-9]+$", regex);
        assertTrue("regex works", Pattern.matches(regex, "Sometextwithoutspaces092"));
    }

    private static class SquareBracketTransformer extends AbstractSquareTagTransformer {

        private String contents;

        private Map<String, Object> parameters;

        public SquareBracketTransformer() {
            super("tag-name", true);
        }

        @Override
        protected String[] getAllowedParameters() {
            return new String[] { "allowed1", "allowed2", "regex" };
        }

        @Override
        protected Callback getCallback() {
            return new TextPatternTransformer.Callback() {

                public String transform(final String input, final MutableContent mc) {
                    Matcher matcher = getTagPattern().matcher(input);
                    if (!matcher.matches()) {
                        fail("Failed to match tag, but shouldn't be here if it didn't");
                    }

                    parameters = getParameters(matcher);
                    contents = getContents(matcher);

                    return input;
                }
            };
        }

    }
    
    private MultiTagSquareBracketTransformer transformer2;
    
    public void testStandardMulti() {
        String input = "[tag-name]contents[/tag-name]";
        transformer2.apply(new MutableContent(null, input));

        assertEquals("contents", transformer2.contents);
        assertEquals("tag-name", transformer2.tagName);
        
        input = "[other-tag-name]contents[/other-tag-name]";
        transformer2.apply(new MutableContent(null, input));

        assertEquals("contents", transformer2.contents);
        assertEquals("other-tag-name", transformer2.tagName);
    }
    
    public void testDoesntMatchMismatchedTags() {
        String input = "[tag-name]contents[/other-tag-name]";
        transformer2.apply(new MutableContent(null, input));

        // hasn't matched
        // contents hasn't been set
        assertNull(transformer2.contents);
    }

    public void testAttributesMulti() {
        String input = "[tag-name allowed1=\"one\" allowed2='two' allowed3=three] the contents [/tag-name]";
        transformer2.apply(new MutableContent(null, input));

        assertEquals(" the contents ", transformer2.contents);

        assertTrue(transformer2.parameters.containsKey("allowed1"));
        assertTrue(transformer2.parameters.containsKey("allowed2"));
        assertFalse(transformer2.parameters.containsKey("allowed3"));

        assertEquals(transformer2.parameters.get("allowed1"), "one");
        assertEquals(transformer2.parameters.get("allowed2"), "two");
        assertEquals(transformer2.parameters.get("allowed3"), null);
        
        input = "[other-tag-name allowed1=\"one\" allowed2='two' allowed3=three] the contents [/other-tag-name]";
        transformer2.apply(new MutableContent(null, input));

        assertEquals(" the contents ", transformer2.contents);

        assertTrue(transformer2.parameters.containsKey("allowed1"));
        assertTrue(transformer2.parameters.containsKey("allowed2"));
        assertFalse(transformer2.parameters.containsKey("allowed3"));

        assertEquals(transformer2.parameters.get("allowed1"), "one");
        assertEquals(transformer2.parameters.get("allowed2"), "two");
        assertEquals(transformer2.parameters.get("allowed3"), null);
    }

    public void testEscapedAttributesMulti() {
        String input = "[tag-name regex='^\\[A-Za-z0-9\\]+$']\nSome escaped stuff ^\\[A-Za-z0-9\\]+$\n[/tag-name]";
        transformer2.apply(new MutableContent(null, input));

        assertEquals("\nSome escaped stuff ^\\[A-Za-z0-9\\]+$\n", transformer2.contents);

        assertTrue(transformer2.parameters.containsKey("regex"));

        String regex = (String) transformer2.parameters.get("regex");
        regex = regex.replaceAll("\\\\\\[", "[");
        regex = regex.replaceAll("\\\\]", "]");

        assertEquals("^[A-Za-z0-9]+$", regex);
        assertTrue("regex works", Pattern.matches(regex, "Sometextwithoutspaces092"));
        
        input = "[other-tag-name regex='^\\[A-Za-z0-9\\]+$']\nSome escaped stuff ^\\[A-Za-z0-9\\]+$\n[/other-tag-name]";
        transformer2.apply(new MutableContent(null, input));

        assertEquals("\nSome escaped stuff ^\\[A-Za-z0-9\\]+$\n", transformer2.contents);

        assertTrue(transformer2.parameters.containsKey("regex"));

        regex = (String) transformer2.parameters.get("regex");
        regex = regex.replaceAll("\\\\\\[", "[");
        regex = regex.replaceAll("\\\\]", "]");

        assertEquals("^[A-Za-z0-9]+$", regex);
        assertTrue("regex works", Pattern.matches(regex, "Sometextwithoutspaces092"));
    }
    
    private static class MultiTagSquareBracketTransformer extends AbstractSquareTagTransformer {

        private String contents;

        private Map<String, Object> parameters;
        
        private String tagName;

        public MultiTagSquareBracketTransformer() {
            super(new String[] {"tag-name", "other-tag-name"}, true);
        }

        @Override
        protected String[] getAllowedParameters() {
            return new String[] { "allowed1", "allowed2", "regex" };
        }

        @Override
        protected Callback getCallback() {
            return new TextPatternTransformer.Callback() {

                public String transform(final String input, final MutableContent mc) {
                    Matcher matcher = getTagPattern().matcher(input);
                    if (!matcher.matches()) {
                        fail("Failed to match tag, but shouldn't be here if it didn't");
                    }

                    parameters = getParameters(matcher);
                    contents = getContents(matcher);
                    tagName = getTagName(matcher);

                    return input;
                }
            };
        }

    }
    
    private BlockLevelSquareBracketTransformer transformer3;
    
    public void testBlockStandard() {
        String input = "[tag-name]contents[/tag-name]";
        transformer3.apply(new MutableContent(null, input));

        assertEquals("contents", transformer3.contents);
    }
    
    public void testBlockWrapped() {
        String input = "<p>[tag-name]contents[/tag-name]</p>";
        transformer3.apply(new MutableContent(null, input));

        assertEquals("<p>contents</p>", transformer3.contents);
    }

    public void testBlockAttributes() {
        String input = "[tag-name allowed1=\"one\" allowed2='two' allowed3=three] the contents [/tag-name]";
        transformer3.apply(new MutableContent(null, input));

        assertEquals(" the contents ", transformer3.contents);

        assertTrue(transformer3.parameters.containsKey("allowed1"));
        assertTrue(transformer3.parameters.containsKey("allowed2"));
        assertFalse(transformer3.parameters.containsKey("allowed3"));

        assertEquals(transformer3.parameters.get("allowed1"), "one");
        assertEquals(transformer3.parameters.get("allowed2"), "two");
        assertEquals(transformer3.parameters.get("allowed3"), null);
    }

    public void testAttributeEscaping() {
    	String input = "[tag-name allowed2=\"The \\\"Magic\\\" tag\" allowed1='It\\'s escaped!' ]the contents[/tag-name]";
    	transformer3.apply(new MutableContent(null, input));
    	assertEquals("It's escaped!", transformer3.parameters.get("allowed1"));
    	assertEquals("The \"Magic\" tag", transformer3.parameters.get("allowed2"));
    }
    
    private static class BlockLevelSquareBracketTransformer extends AbstractSquareTagTransformer {

        private String contents;

        private Map<String, Object> parameters;

        public BlockLevelSquareBracketTransformer() {
            super("tag-name", false, false, true);
        }

        @Override
        protected String[] getAllowedParameters() {
            return new String[] { "allowed1", "allowed2", "regex" };
        }

        @Override
        protected Callback getCallback() {
            return new TextPatternTransformer.Callback() {
                public String transform(final String input, final MutableContent mc) {
                    Matcher matcher = getTagPattern().matcher(input);
                    if (!matcher.matches()) {
                        fail("Failed to match tag, but shouldn't be here if it didn't");
                    }

                    parameters = getParameters(matcher);
                    contents = getContents(matcher);

                    return input;
                }
            };
        }

    }

}
