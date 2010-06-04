package uk.ac.warwick.util.content.cleaner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jmock.MockObjectTestCase;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

public class HtmlCleanerOfficeTest extends MockObjectTestCase {

    private HtmlCleaner cleaner;

    public void setUp() {
        cleaner = new HtmlCleaner();
    }
    
    public void testAwfulBogusStyles() throws Exception {
    	String input = readResourceToString("/htmlClean/sbtwo-3828.html");
        String expected = readResourceToString("/htmlClean/sbtwo-3828-expected.html");
        
        verify(expected, input);
    }

    public void testSBTWO3709() throws Exception {
        String input = readResourceToString("/htmlClean/sbtwo-3709.html");
        String expected = readResourceToString("/htmlClean/sbtwo-3709-expected.html");
        
        verify(expected, input);
    }
    
    public void testSBTWO3564MsoStyles() throws Exception {
        String input = readResourceToString("/htmlClean/sbtwo-3564-2.html");
        String expected = readResourceToString("/htmlClean/sbtwo-3564-2-expected.html");
        
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
    
    public void testHorribleMsWord2007PasteTinyMCE3Chrome() throws Exception {
    	String input = readResourceToString("/htmlClean/input6-chrome.html");
    	String expected = readResourceToString("/htmlClean/output6-chrome.html");
    	verify(expected, input);
    }
    
    public void testWordConditionalCommentEngulsion() throws Exception {
    	String input = readResourceToString("/htmlClean/sbtwo-3781-word.txt");
    	String expected = readResourceToString("/htmlClean/sbtwo-3781-word-expected.txt");
    	
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
    
    private void verify(String expected, String input) {
        String output = cleaner.clean(input).trim();
        assertEquals(expected, output);
    }

    private void verifyNoLineBreaks(String expected, String input) {
        String output = cleaner.clean(input).trim().replaceAll("\n", "").replaceAll("\t", "").replaceAll(">\\s+<", "><");
        expected = expected.trim().replaceAll("\n", "").replaceAll("\t", "").replaceAll(">\\s+<", "><");
        assertEquals(expected, output);
    }

    private String readResourceToString(final String filename) throws IOException {
        InputStream is = getClass().getResourceAsStream(filename);
        Assert.notNull(is, "Couldn't get an inputstream for " + filename);
        String input = FileCopyUtils.copyToString(new InputStreamReader(is));
        return input;
    }

}
