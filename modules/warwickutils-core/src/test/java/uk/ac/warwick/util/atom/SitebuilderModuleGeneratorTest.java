package uk.ac.warwick.util.atom;

import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.WireFeedInput;
import com.rometools.rome.io.WireFeedOutput;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;
import uk.ac.warwick.util.atom.spring.SitebuilderModuleGenerator;
import uk.ac.warwick.util.atom.spring.SitebuilderModuleImpl;

import java.io.StringReader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class SitebuilderModuleGeneratorTest {
    
    private final SitebuilderModuleGenerator generator = new SitebuilderModuleGenerator();
    
    @Test
    public void itWorks() throws Exception {
        Document document = new SAXBuilder().build(
            new StringReader(
                "<entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:sitebuilder=\"http://go.warwick.ac.uk/elab-schemas/atom\"></entry>"
            )
        );
        
        SitebuilderModuleImpl module = new SitebuilderModuleImpl();
        module.setPageOrder(100);
        module.setAllowSearchEngines(true);
        module.setDescription("my description");
        module.setLastUpdateComment("an edit comment, shouldn't be in the output");
        
        generator.generate(module, document.getRootElement());
        
        Feed feed = (Feed) new WireFeedInput().build(document);
        String outputAsString = new WireFeedOutput().outputString(feed, true); 
        
        assertTrue(outputAsString.contains("<sitebuilder:searchable>true</sitebuilder:searchable>"));
        assertTrue(outputAsString.contains("<sitebuilder:description>my description</sitebuilder:description>"));
        assertTrue(outputAsString.contains("<sitebuilder:page-order>100</sitebuilder:page-order>"));
        assertFalse(outputAsString.contains("<sitebuilder:edit-comment>"));
    }

}
