package uk.ac.warwick.util.content.texttransformers;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer.Callback;

public class AbstractSquareBracketTagTransformerTest extends TestCase {

    private final SquareBracketTransformer transformer = new SquareBracketTransformer();

    public void testStandard() {
        String input = "[tag-name]contents[/tag-name]";
        transformer.transform(input);

        assertEquals("contents", transformer.contents);
    }

    public void testAttributes() {
        String input = "[tag-name allowed1=\"one\" allowed2='two' allowed3=three] the contents [/tag-name]";
        transformer.transform(input);

        assertEquals(" the contents ", transformer.contents);

        assertTrue(transformer.parameters.containsKey("allowed1"));
        assertTrue(transformer.parameters.containsKey("allowed2"));
        assertFalse(transformer.parameters.containsKey("allowed3"));

        assertEquals(transformer.parameters.get("allowed1"), "one");
        assertEquals(transformer.parameters.get("allowed2"), "two");
        assertEquals(transformer.parameters.get("allowed3"), null);
    }

    public void testEscapedAttributes() {
        String input = "[tag-name regex='^\\[A-Za-z0-9\\]+$']\nSome escaped stuff ^\\[A-Za-z0-9\\]+$\n[/tag-name]";
        transformer.transform(input);

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

                public String transform(final String input) {
                    Matcher matcher = getTagPattern().matcher(input);
                    if (!matcher.matches()) {
                        fail("Failed to match schedule tag, but shouldn't be here if it didn't");
                    }

                    parameters = getParameters(matcher);
                    contents = getContents(matcher);

                    return input;
                }
            };
        }

    }

}
