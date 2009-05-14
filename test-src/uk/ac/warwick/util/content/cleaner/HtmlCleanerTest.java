package uk.ac.warwick.util.content.cleaner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jmock.MockObjectTestCase;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.content.texttransformers.NewWindowLinkTextTransformer;

public class HtmlCleanerTest extends MockObjectTestCase {

    private HtmlCleaner cleaner;

    public void setUp() {
        cleaner = new HtmlCleaner();
    }

    public void testBasicSanity() throws Exception {
        String input = readResourceToString("/htmlClean/input1.html");
        String output = cleaner.clean(input);

        assertTrue("keep onclick", output.contains("onclick=\"alert('hello, I\\'m an alert')\""));
        assertTrue("keep align", output.contains("align="));

        assertFalse("remove u", output.contains("<u>"));
        assertFalse("remove font", output.contains("<font"));
        assertFalse("remove font", output.contains("</font>"));
        assertFalse("remove span", output.contains("<span>"));

        assertTrue("Not screwed up script tag contents", output.contains("assert(3 < 7);"));

        assertTrue("don't remove non-empty span", output.contains("<span class=\"dontdeleteme\">"));
        assertTrue("correctly don't delete nested good span", output.contains("<span class=\"dontdeleteme\">sub</span>"));

        assertTrue("map tag tidied", output.contains("<map name=\"mymap\">"));
    }

    public void testHorribleMsWordPaste() throws Exception {
        String input = readResourceToString("/htmlClean/input2.html");
        String output = cleaner.clean(input);

        assertFalse("remove all those stupid spans!", output.contains("<span"));
        assertFalse("remove all those stupid spans!", output.contains("</span>"));
        assertFalse("remove all those stupid classes!", output.contains("MsoNormal"));
        assertFalse("remove MS word's stupid tags", output.contains("<time"));
        assertFalse("remove MS word's stupid tags", output.contains("<date"));
    }

    public void testHorribleMsWord2007Paste() throws Exception {
        String input = readResourceToString("/htmlClean/input3.html");
        String output = cleaner.clean(input);

        assertFalse("remove all those stupid spans!", output.contains("<span"));
        assertFalse("remove all those stupid spans!", output.contains("</span>"));
        assertFalse("remove all those stupid classes!", output.contains("MsoNormal"));
        assertFalse("remove MS word's stupid tags", output.contains("<time"));
        assertFalse("remove MS word's stupid tags", output.contains("<date"));

        // we need to allow td style :(
    }
    
    public void testHorribleMsWord2007PasteTinyMCE3Firefox() throws Exception {
    	String input = readResourceToString("/htmlClean/input6-firefox.html");
    	String expected = readResourceToString("/htmlClean/output6-firefox.html");
    	
    	verify(expected, input);
    }
    
    public void testHorribleMsWord2007PasteTinyMCE3IE() throws Exception {
    	String input = readResourceToString("/htmlClean/input6-ie.html");
    	String expected = readResourceToString("/htmlClean/output6-ie.html");
    	
    	verify(expected, input);
    }

    public void testStrongTagsNotNested() {
        String input = "<p><strong>bold <strong>text</strong></strong></p>";
        String expected = "<p><strong>bold text</strong></p>";
        verify(expected, input);
    }

    public void testBoldTags() {
        String input = "<b><strong><em>Hello";
        String expected = "<strong><em>Hello</em></strong>";
        verify(expected, input);

        String input3 = "<strong><strong><strong><strong>X</strong></strong></strong></strong>";
        String expected3 = "<strong>X</strong>";
        verify(expected3, input3);
    }

    public void paragraphsSeparated() {
        String input = "<p>para 1</p><p>para 2</p>";
        String expected = "<p>para 1</p>\n\n<p>para 2</p>\n\n";
        verify(expected, input);
    }

    /**
     * Check that high unicode values, stored as HTML entities (&#3333;) remain
     * that way and are not changed to be single characters.
     */
    public void testHighByteCharsRemainEntities() {
        String input = "<p>Characters &#20013;&#22269;&#25253;&#36947;</p>";
        verify(input, input);
    }

    public void testPreformattedBlocks() {
        String[] tagsToTest = new String[] { "pre", "script" };

        for (String tag: tagsToTest) {
            String input = "<" + tag + ">Line1\n" + "Line2\n" + "Line3</" + tag + ">";
            verify(input, input);
        }
    }

    /**
     * This behaviour wasn't our design but the parser does do this (and is
     * strictly correct), so we may as well have a test to verify that it
     * happens. It's not a problem because we only clean the contents of body,
     * and even if we were doing it on the whole of html, it wouldn't strip
     * style tags from the head because they're allowed there.
     */
    public void testStyleTagsAreStrippedFromBody() {
        String input = "<style type='text/css'>body { color: magenta; }</style>";
        String expected = "body { color: magenta; }";
        verify(expected, input);
    }

    /**
     * Basic test that nested tags are indented.
     */
    public void testIndent() {
        String input = "<ul><li>Item1</li><li>Item2</li></ul>";
        String expected = "<ul>\n" + "  <li>Item1</li>\n" + "  <li>Item2</li>\n" + "</ul>";
        verify(expected, input);
    }

    public void testCommentsPreserved() {
        String comment = "<!-- SITEBUILDER_MERGE_DYNAMIC_STATIC -->";
        String input = "<p>Inconsequential text</p>\n " + comment + " \n" + "<p>More text.</p>";
        String output = cleaner.clean(input);
        assertTrue("comment preserved", output.contains(comment));
    }

    /**
     * Test that the HTML escaping that is normally done, is not done within a
     * script tag, because TagSoup leaves these alone.
     */
    public void testEscapingInScriptTags() {
        String input = "<script type=\"text/javascript\">" + "<!--// <![CDATA[\n var c = 3;\n // ]]> -->" + "</script>";
        verify(input, input);
    }

    public void testMoreScriptTags() {
        String input = "<script>\n" + " if (3 < 4) alert('the world is round'); \n" + "</script>";

        verify(input, input);
    }

    public void testRelativeLinks() {
        String input = "<p>A <a href=\"../page.htm\">relative link</a>.</p>";
        verify(input, input);
    }

    /**
     * TinyMCE uses href and src attributes to store an absolute version of
     * relative links which it usually then swaps in at cleanup time. It keeps
     * the original links in mce_href and mce_src respectively. These tests make
     * sure that the HtmlCleaner gets the values of the mce_ attributes and uses
     * them.
     */
    public void testUseStupidTinyMceLinks() {
        String input = "<p><a href=\"http://server/page/page/../page.htm\" mce_href=\"../page.htm\">link</a></p>";
        String expected = "<p><a href=\"../page.htm\">link</a></p>";
        verify(expected, input);
    }

    public void testUseStupidTinyMceImages() {
        String input = "<p><img src=\"http://server/page/page/../picture.jpg\" mce_src=\"../picture.jpg\" /></p>";
        String expected = "<p><img src=\"../picture.jpg\" border=\"0\" /></p>";
        verify(expected, input);
    }

    /**
     * If somebody writes a < on its own, or followed by some character that
     * isn't the start of a tag or comment, the parser should just escape it
     * into the HTML entity "lt".
     */
    public void testLoneLeftBracketsAreEscaped() {
        String input = "<p>CrabSet <- Crab.</p>";
        String expected = "<p>CrabSet &lt;- Crab.</p>";
        verify(expected, input);

        input = "<p>Crab <3 Crab.</p>";
        expected = "<p>Crab &lt;3 Crab.</p>";
        verify(expected, input);
    }

    /**
     * Check that images without a border attribute are given a default of zero,
     * and also that images with a border already specified are unaffected.
     */
    public void testImagesGivenBorders() {
        String noBorder = "<p><img src=\"image.jpg\" /></p>";
        String addedZeroBorder = "<p><img src=\"image.jpg\" border=\"0\" /></p>";
        String existingBorder = "<p><img src=\"image.jpg\" border=\"1\" /></p>";

        verify(existingBorder, existingBorder);
        verify(addedZeroBorder, noBorder);
    }

    public void testPreUnchanged() {
        String input = "<pre>Text <b>bold</b> <q>hellolevel!</q>  \n<tt>teletype</tt>. \n <code>&gt; prompt</code>\n</pre>";
        verify(input, input);
    }

    // [SBTWO-1396] newlines generated after <br> tags within
    // <pre> tags cause unwanted grief
    public void testPreTagsAccumulatingNewlines() {
        String input = "<pre>ball ball ball<br />\n" + "footy footy footy<br />\n" + "ball ball ball<br />\n" + "FOOTBALL\n</pre>";
        verify(input, input);
    }

    /**
     * Remove a TinyMCE attribute that we don't use.
     */
    public void testMceKeepIsRemoved() {
        String input = "<p class=\"awesome\" mce_keep=\"true\">test</p>";
        String expected = "<p class=\"awesome\">test</p>";
        verify(expected, input);
    }

    public void testMceStyle() {
        String input = "<div mce_style=\"text-align: center\">Hello</div>";
        String expected = "<div style=\"text-align: center\">Hello</div>";
        verify(expected, input);

        // style AND mce_style. in this case they tend to have the same value so
        // it
        // doesn't matter which one is used; the important thing is mce_style
        // goes away.
        input = "<div style=\"text-align: center\" mce_style=\"text-align: center\">Hello</div>";
        expected = "<div style=\"text-align: center\">Hello</div>";
        verify(expected, input);
    }
    
    public void testMceStyleOnSpan() {
    	String input = "<span style=\"text-align: center\">Hello</span>";
        String expected = "Hello";
        verify(expected, input);
        
    	input = "<span mce_style=\"text-align: center\">Hello</span>";
        expected = "Hello";
        verify(expected, input);

        input = "<span style=\"text-align: center\" mce_style=\"text-align: center\">Hello</span>";
        
        // we should strip both style (a banned tag) and mce_style (which gets converted to style) - and then remove the span with no attributes 
        expected = "Hello";
        verify(expected, input);
    }

    public void testMceStyleDoesntKillOthers() {
        String input = "<a href=\"#\" mce_style=\"text-align: center\" style=\"text-align:center\">Hello</a>";
        String expected = "<a href=\"#\" style=\"text-align: center\">Hello</a>";
        verify(expected, input);
    }

    public void testLinkStyleIsKept() {
        String input = "Ra <a href=\"#\" style=\"granos\">Hello</a> Bye";
        String expected = input;
        verify(expected, input);
    }

    public void testMceImg() {
        String input = "<img mce_src=\"kitten.jpg\" border=\"0\">";
        String expected = "<img src=\"kitten.jpg\" border=\"0\" />";
        verify(expected, input);
    }

    /**
     * Make sure empty attributes are removed
     */
    public void testEmptyAttributesRemoved() {
        String input = "<td scope=\"\" style=\"\" dir=\"\" id=\"\" rowspan=\"1\" align=\"\" lang=\"\" valign=\"\">Blah</td>";
        String expected = "<table>  <tbody>    <tr>      <td rowspan=\"1\">Blah</td>    </tr>  </tbody></table>";
        verifyNoLineBreaks(expected, input);
    }

    /**
     * But allow empty alt tags or markup will be invalid
     */
    public void testAllowEmptyAltAttribute() {
        String input = "<img src=\"image.jpg\" border=\"\" alt=\"\" />";
        String expected = "<img src=\"image.jpg\" alt=\"\" />";
        verify(expected, input);
    }

    /**
     * Word 2003 and Word 2007 are horrible at pasting stuff. We should strip
     * all their crap out. see [SBTWO-1044]
     */
    public void testRemoveDodgyWordContent() {
        String input = "<p class=\"MsoNormal\">As members of both institutions, the students of the \n<place st=\"on\"></place>\n  <placetype st=\"on\"></placetype>School of   <placename st=\"on\"></placename>Engineering";
        String expected = "<p>As members of both institutions, the students of the  School of Engineering</p>";
        verify(expected, input);
    }

    public void testDisallowRepeatedTags() {
        String input = "<strong><strong><strong>test</strong></strong></strong>";
        String expected = "<strong>test</strong>";
        verify(expected, input);
    }

    public void testDisallowRepeatedTagsDifferentTags() {
        String input = "<strong><em><strong>test</strong></em></strong>";
        String expected = "<strong><em>test</em></strong>";
        verify(expected, input);
    }

    public void testReplaceBandIWithAlternates() {
        String input = "<b><i>test</i></b>";
        String expected = "<strong><em>test</em></strong>";
        verify(expected, input);
    }

    public void testNoNestedFormsPlease() {
        String input = "<form action='jim' method='post'><input type='blah' value='boo' /><form action='jim' method='post'><input type='blah' value='boo' /></form></form>";
        String expected = "<form action=\"jim\" method=\"post\">  <input type=\"blah\" value=\"boo\" />  <input type=\"blah\" value=\"boo\" /></form>";
        verifyNoLineBreaks(expected, input);
    }

    public void testNoMceMarkup() {
        String input = "<h3 class=\"mceItemHidden\">A title</h3>";
        String expected = "<h3>A title</h3>";
        verify(expected, input);

        input = "<p>A <a mce_thref=\"#somewhere\">stupid link</a>. <img mce_tsrc='sheep.gif' border=3 /> sheep lol</p>";
        expected = "<p>A <a href=\"#somewhere\">stupid link</a>. <img src=\"sheep.gif\" border=\"3\" /> sheep lol</p>";
        verify(expected, input);
    }

    public void testNoCrappyMsWordEmptyTags() {
        String input = "<span> </span><span>Also, the loss of confidence internally in the current email system requires us to set challenging objectives to accelerate progress with the email replacement project.<span>&nbsp; </span>Work has started on plans to enable all staff to send and receive email via Exchange as soon as possible.<span>&nbsp; </span>By reprioritising work within IT Services, and obtaining extra help, this process will be well underway by Christmas, retaining Groupwise only as a temporary archive.<span>&nbsp; </span>Full migration from Groupwise to Outlook will then take place over the following few months. After consultation with the Email Project Board I will provide a revised timetable for migration as soon as it is available. In moving forward I recognise that there remains an outstanding issue regarding the proposed restriction of attachment size to a maximum of 10 MB.<span>&nbsp; </span>I will ensure that we provide an appropriate method for the distribution of larger attachments which addresses this issue without unduly impacting on the efficiency of the system.</span><p><span></span></p>\n\n<span> </span><span> </span><p><span></span></p>";
        String expected = "Also, the loss of confidence internally in the current email system requires us to set challenging objectives to accelerate progress with the email replacement project.&nbsp; Work has started on plans to enable all staff to send and receive email via Exchange as soon as possible.&nbsp; By reprioritising work within IT Services, and obtaining extra help, this process will be well underway by Christmas, retaining Groupwise only as a temporary archive.&nbsp; Full migration from Groupwise to Outlook will then take place over the following few months. After consultation with the Email Project Board I will provide a revised timetable for migration as soon as it is available. In moving forward I recognise that there remains an outstanding issue regarding the proposed restriction of attachment size to a maximum of 10 MB.&nbsp; I will ensure that we provide an appropriate method for the distribution of larger attachments which addresses this issue without unduly impacting on the efficiency of the system.";
        verify(expected, input);
    }

    // Word pastes in conditional comments for lists sometimes.
    public void testConditionalCommentsAreRemoved() {
        String input = "<!--[if !supportLists]-->3)   <!--[endif]-->How many letters in the alphabet there are on labels in the room"
                + "<!--[if !supportLists]-->5)   <!--[endif]-->Ideas for essays";
        String expected = "3) How many letters in the alphabet there are on labels in the room" + "5) Ideas for essays";

        verify(expected, input);
    }

    public void testPastedNewWindowLinksAreRemoved() {
        String string = "<p>Hello I've pasted this <a href=\"blah\" target=\"_blank\">Link";
        String string2 = "</a> in from another page</p>";
        String input = string + NewWindowLinkTextTransformer.HTML_IMAGE + string2;

        String expected = string + string2;
        verify(expected, input);
    }

    /**
     * TinyMCE 3 generates these bogus line breaks to fill up space. They're
     * only needed while editing, so strip them out completely.
     */
    public void testMceBogus() {
        String input = "<p>Some test, la la!<br mce_bogus=\"1\" /></p>";
        String expected = "<p>Some test, la la!</p>";
        verify(expected, input);

        // test it with slightly different formatting
        input = "<p>Some test, la la!<br mce_bogus=1></p>";
        expected = "<p>Some test, la la!</p>";
        verify(expected, input);
    }
    
    // TinyMCE3 puts scripts in <p> tags because it is a KNOBSHITE
    public void testMceScript() {
    	String input = "<p><mce:script type=\"text/javascript\"><!--\nalert('Im a script!');\n// --></mce:script></p>";
    	String expected = "<script type=\"text/javascript\"><!--\nalert('Im a script!');\n// --></script>";
    	
    	verify(expected, input);
    }

    public void testAlignMiddleOnTableCells() {
        String input = "<table>";
        input += "<tr><th>Header 1</th><th align=\"middle\">Header 2</th><th cellpadding=\"1\" align=middle>Header 3</th></tr>";
        input += "<tr><td>Content 1</td><td align=\"middle\">Content 2</td><td cellpadding=\"1\" align=middle>Header 3</td></tr>";
        input += "</table>";

        String output = cleaner.clean(input).trim();
        
        assertTrue(output.contains("<td align=\"center\">"));
        assertTrue(output.contains("<th align=\"center\">"));
        assertFalse(output.contains("<td align=\"middle\">"));
    }
    
    public void testNastyOOWriterPasteTinyMCE3() throws Exception {
    	String input = readResourceToString("/htmlClean/input5.html");
    	String expected = "<p class=\"western\">Some nasty ass shit</p>";
    	
    	verify(expected, input);
    }
    
    public void testDontTrimTinyMCE3Indents() throws Exception {
    	String input = "<h2 style=\"padding-left: 270px;\">blah blah blah</h2>\n\n<p style=\"padding-left: 60px;\">blah blah blah</p>";
    	String expected = input;
    	
    	verify(expected, input);
    }
    
    public void testCleanEmptyStyleAttributes() throws Exception {
    	String input = "<p style=\"\">blah blah blah</p>";
    	String expected = "<p>blah blah blah</p>";
    	
    	verify(expected, input);
    }

    private void verify(String expected, String input) {
        String output = cleaner.clean(input).trim();
        assertEquals(expected, output);
    }

    private void verifyNoLineBreaks(String expected, String input) {
        String output = cleaner.clean(input).trim().replaceAll("\n", "").replaceAll("\t", "");
        assertEquals(expected, output);
    }

    private void verifyNoTrim(String expected, String input) {
        String output = cleaner.clean(input);
        assertEquals(expected, output);
    }

    private String readResourceToString(final String filename) throws IOException {
        InputStream is = getClass().getResourceAsStream(filename);
        String input = FileCopyUtils.copyToString(new InputStreamReader(is));
        return input;
    }

}
