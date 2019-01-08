package uk.ac.warwick.util.atom;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.WireFeedInput;
import junit.framework.TestCase;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.springframework.core.io.ClassPathResource;
import uk.ac.warwick.util.atom.spring.SitebuilderModule;

import java.util.List;

public class SitebuilderModuleParserTest extends TestCase {
    @SuppressWarnings("unchecked")
    public void testModuleParsing() throws Exception {
        Document document = new SAXBuilder().build(getClass().getResourceAsStream("testfeed.xml"));
        Feed feed = (Feed) new WireFeedInput().build(document);
        
        ClassPathResource romeProps = new ClassPathResource("rome.properties");
        assertTrue("rome.properties not found", romeProps.exists());

        List<Entry> entries = feed.getEntries();
        assertEquals(1, entries.size());  
        Entry entry = (Entry) entries.get(0);
        
        SitebuilderModule module = (SitebuilderModule) entry.getModule(SitebuilderModule.MODULE_URI);
        
        assertEquals("nickles", module.getPageName());
        assertTrue(module.getAllowSearchEngines());
        assertFalse(module.getShowInLocalNavigation());
        assertTrue(module.getSpanRhs());
        assertEquals("Here is the description I'm using for this page", module.getDescription());
        assertEquals("horses, play things, lovely times", module.getKeywords());
    }
}
