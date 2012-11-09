package uk.ac.warwick.util.web.tags;

import java.util.regex.Pattern;

import junit.framework.TestCase;

public class WrappingUrlTagTest extends TestCase {

    public void testUrlStartingWithSlash() throws Exception {
        String input = "/services/its/elab/about/people/mmannion";
        String expected = "/<wbr>services/<wbr>its/<wbr>elab/<wbr>about/<wbr>people/<wbr>mmannion";

        assertItWorks(input, expected);
    }

    public void testUrlStartingWithHttp() throws Exception {
        String input = "http://www.warwick.ac.uk/services/its/elab/about/people/mmannion";
        String expected = "http://<wbr>www.warwick.ac.uk/<wbr>services/<wbr>its/<wbr>elab/<wbr>about/<wbr>people/<wbr>mmannion";

        assertItWorks(input, expected);
    }

    public void testUrlWithoutLeadingSlash() throws Exception {
        String input = "services/its/elab/about/people/mmannion/";
        String expected = "services/<wbr>its/<wbr>elab/<wbr>about/<wbr>people/<wbr>mmannion/";

        assertItWorks(input, expected);
    }

    private void assertItWorks(String input, String expected) {
        WrappingUrlTag tag = new WrappingUrlTag();
        tag.setUrl(input);

        String glueRegex = Pattern.quote(WrappingUrlTag.GLUE);

        // assert that with the glue removed we get the initial string
        assertEquals("Removing glue should yield initial string", input, tag.getWrappableUrl().replaceAll(glueRegex, ""));
        assertEquals("Should equal expected string", expected, tag.getWrappableUrl());
    }

}
