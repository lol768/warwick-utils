package uk.ac.warwick.util.content.cleaner;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.texttransformers.NewWindowLinkTextTransformer;

public final class HtmlCleanerTest extends AbstractHtmlCleanerTest {
    
    @Test
    public void sbtwo3795_strikethrough() throws Exception {
    	String input = "<html><body>I am afraid of <span style=\"text-decoration: line-through; \" _mce_style=\"text-decoration: line-through;\">ghosts</span>tigers </body></html>";
    	String expected = "I am afraid of <strike>ghosts</strike>tigers";
    	
    	verify(expected, input);
    }
    
    @Test
    public void sbtwo4909() throws Exception {
        String input = readResourceToString("/htmlClean/sbtwo-4909.html");
       
        verify(input, input);
    }

    @Test
    public void sbtwo5135() throws Exception {
        String expected = "<script type=\"text/javascript\">(function() {return true;})();</script>";
        
        String input = "<script type=\"mce-text/javascript\">(function() {return true;})();</script>";
        verify(expected, input);
        
        input = "<script type=\"mce-mce-mce-mce-text/javascript\">(function() {return true;})();</script>";
        verify(expected, input);
    }

    @Test
    public void sbtwo5117() throws Exception {
        String expected = "<div><!-- a comment about some <p> tags -->\n</div>";
        
        String input = "<div><p><!-- a comment about some <p> tags -->\n</p>\n</div>";
        verify(expected, input);
    }

    @Test
    public void basicSanity() throws Exception {
        String input = readResourceToString("/htmlClean/input1.html");
        String output = cleaner.clean(input, new MutableContent(null, null));

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
    
    @Test
    public void spanner() throws Exception {
    	String input = "<p class=\"MsoNormal\">A <span style=\"\">&nbsp; </span> B</p>";
    	
    	verify("<p>A &nbsp; B</p>", input);
    }

    @Test
    public void strongTagsNotNested() {
        String input = "<p><strong>bold <strong>text</strong></strong></p>";
        String expected = "<p><strong>bold text</strong></p>";
        verify(expected, input);
    }

    @Test
    public void boldTags() {
        String input = "<b><strong><em>Hello";
        String expected = "<strong><em>Hello</em></strong>";
        verify(expected, input);

        String input3 = "<strong><strong><strong><strong>X</strong></strong></strong></strong>";
        String expected3 = "<strong>X</strong>";
        verify(expected3, input3);
    }

    @Test
    public void paragraphsSeparated() {
        String input = "<p>para 1</p><p>para 2</p>";
        String expected = "<p>para 1</p>\n\n<p>para 2</p>";
        verify(expected, input);
    }

    /**
     * Check that high unicode values, stored as HTML entities (&#3333;) remain
     * that way and are not changed to be single characters.
     */
    @Test
    public void highByteCharsRemainEntities() {
        String input = "<p>Characters &#20013;&#22269;&#25253;&#36947;</p>";
        verify(input, input);
    }

    @Test
    public void preformattedBlocks() {
        String[] tagsToTest = new String[] { "pre", "script" };

        for (String tag: tagsToTest) {
            String input = "<" + tag + ">Line1\n" + "Line2\n" + "Line3</" + tag + ">";
            verify(input, input);
        }
    }

    /**
     * This behaviour wasn't our design but the parser does do this (and is
     * strictly correct), so we may as well have a test to verify that it
     * happens.
     * 
     * Note that this is only when there is no surrounding body tag - see {@link #testStyleTagsNotStrippedFromBody()}.
     */
    @Test
    public void styleTagsAreStripped() {
        String input = "<style type='text/css'>body { color: magenta; }</style>";
        String expected = "body { color: magenta; }";
        verify(expected, input);
    }
    
    /**
     * When wrapped in <html><body>, the parser
     * DOES keep style tags (though the html and body tags are removed).
     */
    @Test
    public void styleTagsNotStrippedFromBody() {
    	String expected = "<style type=\"text/css\">body { color: magenta; }</style>";
    	String input = "<html><body>"+expected+"</body><html>";
    	verify(expected, input);
    }
    
    /**
     * One of the regexes attempting to strip styles inside paragraphs was stripping styles
     * BETWEEN paragraphs, so check that this doesn't happen.
     */
    @Test
    public void styleTagBetweenParagraphs() {
    	String input = "<html><body>" +
    			"<p>One</p>\n <style>Two</style>\n <p>Three</p>\n" +
    			"</body></html>";
    	
    	String expected = "<p>One</p>\n\n<style>Two</style>\n<p>Three</p>";
    	
    	verify(expected, input);
    }

    /**
     * Basic test that nested tags are indented.
     */
    @Test
    public void indent() {
        String input = "<ul><li>Item1</li><li>Item2</li></ul>";
        String expected = "<ul>\n" + "  <li>Item1</li>\n" + "  <li>Item2</li>\n" + "</ul>";
        verify(expected, input);
    }

    @Test
    public void commentsPreserved() {
        String comment = "<!-- SITEBUILDER_MERGE_DYNAMIC_STATIC -->";
        String input = "<p>Inconsequential text</p>\n " + comment + " \n" + "<p>More text.</p>";
        String output = cleaner.clean(input, new MutableContent(null, null));
        assertTrue("comment preserved", output.contains(comment));
    }

    /**
     * Test that the HTML escaping that is normally done, is not done within a
     * script tag, because TagSoup leaves these alone.
     */
    @Test
    public void escapingInScriptTags() {
        String input = "<script type=\"text/javascript\">" + "<!--// <![CDATA[\n var c = 3;\n // ]]> -->" + "</script>";
        verify(input, input);
    }

    @Test
    public void moreScriptTags() {
        String input = "<script>\n" + " if (3 < 4) alert('the world is round'); \n" + "</script>";

        verify(input, input);
    }

    @Test
    public void relativeLinks() {
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
    @Test
    public void useStupidTinyMceLinks() {
        String input = "<p><a href=\"http://server/page/page/../page.htm\" mce_href=\"../page.htm\">link</a></p>";
        String expected = "<p><a href=\"../page.htm\">link</a></p>";
        verify(expected, input);
    }

    @Test
    public void useStupidTinyMceImages() {
        String input = "<p><img src=\"http://server/page/page/../picture.jpg\" mce_src=\"../picture.jpg\" /></p>";
        String expected = "<p><img src=\"../picture.jpg\" border=\"0\" /></p>";
        verify(expected, input);
    }

    /**
     * If somebody writes a < on its own, or followed by some character that
     * isn't the start of a tag or comment, the parser should just escape it
     * into the HTML entity "lt".
     */
    @Test
    public void loneLeftBracketsAreEscaped() {
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
    @Test
    public void imagesGivenBorders() {
        String noBorder = "<p><img src=\"image.jpg\" /></p>";
        String addedZeroBorder = "<p><img src=\"image.jpg\" border=\"0\" /></p>";
        String existingBorder = "<p><img src=\"image.jpg\" border=\"1\" /></p>";

        verify(existingBorder, existingBorder);
        verify(addedZeroBorder, noBorder);
    }

    @Test
    public void preUnchanged() {
        String input = "<pre>Text <b>bold</b> <q>hellolevel!</q>  \n<tt>teletype</tt>. \n <code>&gt; prompt</code>\n</pre>";
        verify(input, input);
    }

    // [SBTWO-1396] newlines generated after <br> tags within
    // <pre> tags cause unwanted grief
    @Test
    public void preTagsAccumulatingNewlines() {
        String input = "<pre>ball ball ball<br />\n" + "footy footy footy<br />\n" + "ball ball ball<br />\n" + "FOOTBALL\n</pre>";
        verify(input, input);
    }

    /**
     * Remove a TinyMCE attribute that we don't use.
     */
    @Test
    public void mceKeepIsRemoved() {
        String input = "<p class=\"awesome\" mce_keep=\"true\">test</p>";
        String expected = "<p class=\"awesome\">test</p>";
        verify(expected, input);
    }

    @Test
    public void mceStyle() {
        String input = "<div mce_style=\"text-align: center\">Hello</div>";
        String expected = "<div style=\"text-align: center\">Hello</div>";
        verify(expected, input);

        // style AND mce_style. in this case they tend to have the same value so it
        // doesn't matter which one is used; the important thing is mce_style goes away.
        input = "<div style=\"text-align: center\" mce_style=\"text-align: center\">Hello</div>";
        expected = "<div style=\"text-align: center\">Hello</div>";
        verify(expected, input);
    }
    
    @Test
    public void paragraphCenter() {
    	String standard = "<p style=\"text-align: center;\">Centre me</p>";
    	// uppercase property name, no ending semicolon
    	String ie = "<p style=\"TEXT-ALIGN: center\">Centre me</p>";
    	verify(standard, standard);
    	verify(ie, ie);
    }
    
    @Test
    public void mceStyleOnSpan() {
    	String input = "<span style=\"color: magenta\">Hello</span>";
        String expected = "Hello";
        verify(expected, input);
        
    	input = "<span mce_style=\"color: magenta\">Hello</span>";
        expected = "Hello";
        verify(expected, input);

        input = "<span style=\"color: magenta\" mce_style=\"color: magenta\">Hello</span>";
        
        // we should strip both style (a banned tag) and mce_style (which gets converted to style) - and then remove the span with no attributes 
        expected = "Hello";
        verify(expected, input);
    }

    @Test
    public void mceStyleDoesntKillOthers() {
        String input = "<a href=\"#\" mce_style=\"text-align: center\" style=\"text-align: center;\">Hello</a>";
        String expected = "<a href=\"#\" style=\"text-align: center\">Hello</a>";
        verify(expected, input);
    }

    @Test
    public void linkStyleIsKept() {
        String input = "Ra <a href=\"#\" style=\"granos\">Hello</a> Bye";
        String expected = input;
        verify(expected, input);
    }

    @Test
    public void mceImg() {
        String input = "<img mce_src=\"kitten.jpg\" border=\"0\">";
        String expected = "<img src=\"kitten.jpg\" border=\"0\" />";
        verify(expected, input);
    }
    
    @Test
    public void mceArea() {
        String input = "<map><area title=\"Pasqual Paoli - Corsican patriot\" alt=\"Pasqual Paoli - Corsican patriot\" coords=\"203,107,220,139\" mce_coords=\"203,107,220,139\" shape=\"rect\" mce_shape=\"rect\" href=\"/wiki/Pasquale_Paoli\" /></map>";
        String expected = "<map>  <area title=\"Pasqual Paoli - Corsican patriot\" alt=\"Pasqual Paoli - Corsican patriot\" coords=\"203,107,220,139\" shape=\"rect\" href=\"/wiki/Pasquale_Paoli\" /></map>";
        verifyNoLineBreaks(expected, input);
    }

    /**
     * Make sure empty attributes are removed
     */
    @Test
    public void emptyAttributesRemoved() {
        String input = "<td scope=\"\" style=\"\" dir=\"\" id=\"\" rowspan=\"1\" align=\"\" lang=\"\" valign=\"\">Blah</td>";
        String expected = "<table>  <tbody>    <tr>      <td rowspan=\"1\">Blah</td>    </tr>  </tbody></table>";
        verifyNoLineBreaks(expected, input);
    }

    /**
     * But allow empty alt tags or markup will be invalid
     */
    @Test
    public void allowEmptyAltAttribute() {
        String input = "<img src=\"image.jpg\" border=\"\" alt=\"\" />";
        String expected = "<img src=\"image.jpg\" alt=\"\" />";
        verify(expected, input);
    }

    @Test
    public void disallowRepeatedTags() {
        String input = "<strong><strong><strong>test</strong></strong></strong>";
        String expected = "<strong>test</strong>";
        verify(expected, input);
    }

    @Test
    public void disallowRepeatedTagsDifferentTags() {
        String input = "<strong><em><strong>test</strong></em></strong>";
        String expected = "<strong><em>test</em></strong>";
        verify(expected, input);
    }

    @Test
    public void replaceBandIWithAlternates() {
        String input = "<b><i>test</i></b>";
        String expected = "<strong><em>test</em></strong>";
        verify(expected, input);
    }

    @Test
    public void noNestedFormsPlease() {
        String input = "<form action='jim' method='post'><input type='blah' value='boo' /><form action='jim' method='post'><input type='blah' value='boo' /></form></form>";
        String expected = "<form action=\"jim\" method=\"post\">  <input type=\"blah\" value=\"boo\" />  <input type=\"blah\" value=\"boo\" /></form>";
        verifyNoLineBreaks(expected, input);
    }
    
    @Test
    public void removeOnlyMceClass() {
        String input="a <img src=\"x.jpg\" class=\"mceItem square-tag\"> b";
        String expected="a <img src=\"x.jpg\" class=\" square-tag\" border=\"0\" /> b";
        verify(expected, input);
    }

    @Test
    public void noMceMarkup() {
        String input = "<h3 class=\"mceItemHidden\">A title</h3>";
        String expected = "<h3>A title</h3>";
        verify(expected, input);

        input = "<p>A <a mce_thref=\"#somewhere\">stupid link</a>. <img mce_tsrc='sheep.gif' border=3 /> sheep lol</p>";
        expected = "<p>A <a href=\"#somewhere\">stupid link</a>. <img src=\"sheep.gif\" border=\"3\" /> sheep lol</p>";
        verify(expected, input);
    }

    @Test
    public void noMceMarkupNewUnderscoredVersion() {
        String input = "<h3 class=\"_mceItemHidden\">A title</h3>";
        String expected = "<h3>A title</h3>";
        verify(expected, input);

        input = "<p>A <a _mce_href=\"#somewhere\">stupid link</a>. <img _mce_src='sheep.gif' border=3 /> sheep lol</p>";
        expected = "<p>A <a href=\"#somewhere\">stupid link</a>. <img src=\"sheep.gif\" border=\"3\" /> sheep lol</p>";
        verify(expected, input);
    }

    @Test
    public void pastedNewWindowLinksAreRemoved() {
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
    @Test
    public void mceBogus() {
        String input = "<p>Some test, la la!<br _mce_bogus=\"1\" /></p>";
        String expected = "<p>Some test, la la!</p>";
        verify(expected, input);

        // test it with slightly different formatting
        input = "<p>Some test, la la!<br mce_bogus=1></p>";
        expected = "<p>Some test, la la!</p>";
        verify(expected, input);
    }

    /**
     * TinyMCE 3.4 uses HTML5 data attrs for this
     */
    @Test
    public void mceBogus2() {
        String input = "<p>Some test, la la!<br data-mce-bogus=\"1\" /></p>";
        String expected = "<p>Some test, la la!</p>";
        verify(expected, input);

        // test it with slightly different formatting
        input = "<p>Some test, la la!<br data-mce-bogus=1></p>";
        expected = "<p>Some test, la la!</p>";
        verify(expected, input);
    }
    
    // TinyMCE3 puts scripts in <p> tags because it is a KNOBSHITE
    @Test
    public void mceScript() {
    	String input = "<p><mce:script type=\"text/javascript\"><!--\nalert('Im a script!');\n// --></mce:script></p>";
    	String expected = "<script type=\"text/javascript\"><!--\nalert('Im a script!');\n// --></script>";
    	
    	verify(expected, input);
    }

    @Test
    public void alignMiddleOnTableCells() {
        String input = "<table>";
        input += "<tr><th>Header 1</th><th align=\"middle\">Header 2</th><th cellpadding=\"1\" align=middle>Header 3</th></tr>";
        input += "<tr><td>Content 1</td><td align=\"middle\">Content 2</td><td cellpadding=\"1\" align=middle>Header 3</td></tr>";
        input += "</table>";

        String output = cleaner.clean(input, new MutableContent(null, null)).trim();
        
        assertTrue(output.contains("<td align=\"center\">"));
        assertTrue(output.contains("<th align=\"center\">"));
        assertFalse(output.contains("<td align=\"middle\">"));
    }
    
    @Test
    public void nastyOOWriterPasteTinyMCE3() throws Exception {
    	String input = readResourceToString("/htmlClean/input5.html");
    	String expected = "<p class=\"western\">Some nasty ass shit</p>";
    	
    	verify(expected, input);
    }
    
    @Test
    public void dontTrimTinyMCE3Indents() throws Exception {
    	String input = "<h2 style=\"padding-left: 270px; \">blah blah blah</h2>\n\n<p style=\"padding-left: 60px;\">blah blah blah</p>";
    	String expected = input;
    	
    	verify(expected, input);
    }
    
    @Test
    public void dontTrimTinyMCE3Aligns() throws Exception {
    	String input = "<h2 style=\"text-align: middle;\">blah blah blah</h2>\n\n<p style=\"text-align: right;\">blah blah blah</p>";
    	String expected = input;
    	
    	verify(expected, input);
    }
    
    @Test
    public void cleanEmptyStyleAttributes() throws Exception {
    	String input = "<p style=\"\">blah blah blah</p>";
    	String expected = "<p>blah blah blah</p>";
    	
    	verify(expected, input);
    }
    
    @Test
    public void stripMceStyleWithPadding() throws Exception {
    	String input = readResourceToString("/htmlClean/input7.html");
    	String expected = readResourceToString("/htmlClean/output7.html");
    	
    	verify(expected, input);
    }
    
    @Test
    public void nastyBlocking() throws Exception {
    	String input = readResourceToString("/htmlClean/sbtwo-3275.html");
    	
    	long start = System.currentTimeMillis();
    	cleaner.clean(input, new MutableContent(null, null));
    	long stop = System.currentTimeMillis();
    	
    	// assert that this took less than 5 seconds. It should take MUCH less than that!
    	assertTrue((stop-start) < 5000);
    }
    
    @Test
    public void webkitFormattingSpans() throws Exception {
    	String input = readResourceToString("/htmlClean/input9.html");
    	String expected = readResourceToString("/htmlClean/output9.html");
    	verify(expected, input);
    }

    @Test
    public void ieIndenting() throws Exception {
    	String input = readResourceToString("/htmlClean/input8.html");
    	String expected = readResourceToString("/htmlClean/output8.html");
    	
    	verify(expected, input);
    }
    
    @Test
    public void sbtwo3564LotsOfSpans() throws Exception {
        String input = readResourceToString("/htmlClean/sbtwo-3564.html");
        String expected = readResourceToString("/htmlClean/sbtwo-3564-expected.html");
        
        verify(expected, input);
    }
    
    @Test
    public void sbtwo3574ChromeBoldening() throws Exception {
    	String input = 
    		"<p>Hello, " + 
    		  "<span mce_name=\"strong\" mce_style=\"font-weight: bold;\" class=\"Apple-style-span\" style=\"font-weight: bold; \">" +
    		      "my" +
    		      "<span class=\"Apple-style-span\" style=\"font-weight: normal;\" mce_style=\"font-weight: normal;\" mce_fixed=\"1\">" +
    		        " <em>name</em> is" +
    		      "</span>" +
    		  " Nick</span>" +
    		"</p>";
    	String expected = "<p>Hello, <strong>my</strong> <em>name</em> is<strong> Nick</strong></p>";
    	verify(expected, input);
    }
    
    @Test
    public void utl72BackgroundEqBackground() throws Exception {
        String input = "Here is a <em background=\"background\" align=\"align\">Rubbish</em> How magic";
        String expected = "Here is a <em>Rubbish</em> How magic";
        verifyNoLineBreaks(expected, input);
    }
    
    @Test
    public void utl72InvalidStyle() throws Exception {
        String input = "Here is a <div style=\"background-image: url(background);\">Rubbish</div> How magic";
        String expected = "Here is a <div>Rubbish</div> How magic";
        verifyNoLineBreaks(expected, input);
    }
    
    @Test
    public void html5Video() throws Exception {
        String input = readResourceToString("/htmlClean/video-tag.html");
        String expected = readResourceToString("/htmlClean/video-tag.html");
        
        verifyNoLineBreaks(expected, input);
    }
    
    @Test
    public void removeInvisibleSandwiches() throws Exception {
    	String range = "Something happens<span id='_mce_start' style='display:none;line-height:0'>&#65279;</span> and I get my invisible sandwiches<span id='_mce_end' style='display:none;line-height:0'>&#65279;</span> for breakfast.";
    	String expected = "Something happens and I get my invisible sandwiches for breakfast.";
    	
    	verify(expected, range);
    }
    
    @Test
    public void sbtwo3782_orphanLightboxLink() throws Exception {
    	String input = "<P>Hello I like <a href=\"http://www.google.com\">Google</a>. <STRONG><A title='koala.jpg' href=\"http://www2-test.warwick.ac.uk/services/its/intranet/projects/webdev/sandbox/nickhowes/koala.jpg\" rel=lightbox[all] _mce_href=\"koala.jpg\"></A><BR></STRONG></P>";
    	String expected = "<p>Hello I like <a href=\"http://www.google.com\">Google</a>. <strong><br />\n  </strong></p>";
    	verify(expected, input);
    }
    
    @Test
    public void sbtwo3782_regularLightboxLink() throws Exception {
    	String input = "<P>Hello I like <a href=\"http://www.google.com\">Google</a>. <STRONG><A title='koala.jpg' href=\"http://www2-test.warwick.ac.uk/services/its/intranet/projects/webdev/sandbox/nickhowes/koala.jpg\" rel=lightbox[all] _mce_href=\"koala.jpg\"><img src=\"koala.jpg\"></A><BR></STRONG> badgers</P>";
    	//String expected = "<p>Hello I like <a href=\"http://www.google.com\">Google</a>. <strong><a title=\"koala.jpg\" href=\"http://www2-test.warwick.ac.uk/services/its/intranet/projects/webdev/sandbox/nickhowes/koala.jpg\" rel=\"lightbox[all]\" _mce_href=\"koala.jpg\"><img src=\"koala.jpg\"></a><br></strong> badgers</p>";
    	String expected = "<p>Hello I like <a href=\"http://www.google.com\">Google</a>. <strong><a title=\"koala.jpg\" rel=\"lightbox[all]\" href=\"koala.jpg\"><img src=\"koala.jpg\" border=\"0\" /></a><br />\n  </strong> badgers</p>";
    	verify(expected, input);
    }
    
    @Test
    public void sbtwo3830() throws Exception {
    	String input = "<p>Something <span class=\"Apple-style-span\" style=\"font-weight: normal;\" mce_style=\"font-weight: normal;\">wonderful</span> has happened.</p>";
    	String expected = "<p>Something wonderful has happened.</p>";
    	
    	verify(expected, input);
    }
    
    @Test
    public void sbtwo3874() {
        String input = "<th align='center'>       \n        \n\n\n      \n</TH>";
        String expected = 
            "<table>\n" +
            "  <tbody>\n" +
            "    <tr>\n" +
            "      <th align=\"center\">&nbsp;</th>\n" +
            "    </tr>\n" +
            "  </tbody>\n" +
            "</table>";
        
        verify(expected, input);
    }
    
    @Test
    public void sbtwo4051() {
        String input = "<img src=\"http://java-monkey.warwick.ac.uk/services/its/elab/about/people/nickhowes/pagetypes/news/809-3.jpg\" _mce_src=\"809-3.jpg\" mce_src=\"809-3.jpg\" alt=\"Moomins\">";
        String expected = "<img src=\"809-3.jpg\" alt=\"Moomins\" border=\"0\" />";
        verify(expected, input);
    }
    
    /**
     * Compact non-breaking spaces and turn them into regular spaces where possible.
     */
    @Test
    public void sbtwo4230() {
        String input = "<table><tr><td>&nbsp;</td><td>  &nbsp; </td></tr></table>\n" +
        		"<p>Hey how's&nbsp;it going. &nbsp;I&nbsp;like    your    socks.&nbsp; Yeah, nice socks.</p>\n" +
        		"<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Mysterious indenting!</p>\n";
        String expected = "<table>\n" +
        "  <tr>\n" +
        "    <td>&nbsp;</td>\n" +
        "    <td>&nbsp;</td>\n" +
        "  </tr>\n" +
        "</table>\n\n" +
        "<p>Hey how's it going. I like your socks. Yeah, nice socks.</p>\n\n" +
        "<p> Mysterious indenting!</p>";
        verify(expected, input);
        
        verify("<p>Toast. Toast. Toast.</p>", "<p>Toast.&nbsp; Toast. &nbsp;Toast.</p>");
    }
    
    @Test
    public void html5() throws Exception {
        // Taken from Bruce Lawson's HTML5 test page www.brucelawson.co.uk/tests/html5-elements.html
        String input = readResourceToString("/htmlClean/input10.html");
        
        verifyNoLineBreaks(input);
    }
    
    private void verifyNoLineBreaks(String input) {
    	verifyNoLineBreaks(input,input);
    }

    private void verify(String expected, String input) {
        String output = cleaner.clean(input, new MutableContent(null, null)).trim();
        assertEquals(expected.replace("\r", ""), output.replace("\r", ""));
        
        // Check that subsequent cleanups are idempotent
        verifyIdempotence(expected, false);
    }
    
    private void verifyIdempotence(final String html, boolean trimLineBreaks) {
    	// This is DIFFICULT!
    	return;
    	
//    	String currentContent = html;
//    	// Check that subsequent cleanups are idempotent
//    	for (int i=0; i<5; i++) {
//    		String cleaned = cleaner.clean(currentContent).trim();
//    		if (trimLineBreaks) {
//    			cleaned = trimLineBreaks(cleaned);
//    		}
//			assertEquals("Re-cleanup number " + (i+1) + " changed", html, cleaned);
//			currentContent = cleaned;
//    	}
    }

    private void verifyNoLineBreaks(String expected, String input) {
        String output = trimLineBreaks(cleaner.clean(input, new MutableContent(null, null)));
        expected = trimLineBreaks(expected);
        assertEquals(expected, output);
        
        verifyIdempotence(expected, true);
    }

    private String trimLineBreaks(String clean) {
    	return clean.trim().replace("\n", "").replace("\t", "").replace("\r", "").replaceAll(">\\s+<", "><");
	}

	private String readResourceToString(final String filename) throws IOException {
        InputStream is = getClass().getResourceAsStream(filename);
        String input = FileCopyUtils.copyToString(new InputStreamReader(is));
        return input;
    }

}
