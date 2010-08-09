package uk.ac.warwick.util.atom;

import java.util.List;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.springframework.core.io.ClassPathResource;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.WireFeedInput;

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
    }
}
