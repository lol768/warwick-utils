package uk.ac.warwick.util.content.cleaner;

import org.junit.Test;
import uk.ac.warwick.util.content.MutableContent;

import static org.junit.Assert.assertEquals;

public class TagAndAttributeFilterTest extends AbstractHtmlCleanerTest {

    @Test
    public void setSmallTag() {
        String input = "<p><small>this is a test</small></p>";
        verify(input, input);
    }

    private void verify(String expected, String input) {
        String output = cleaner.clean(input, new MutableContent(null, null)).trim();
        assertEquals(expected, output);
    }

}