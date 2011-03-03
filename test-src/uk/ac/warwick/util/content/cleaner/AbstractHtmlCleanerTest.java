package uk.ac.warwick.util.content.cleaner;

import org.junit.Before;

import uk.ac.warwick.util.content.cleaner.html5.HTML5Schema;

public abstract class AbstractHtmlCleanerTest {
    
    protected final HtmlCleaner cleaner = new HtmlCleaner();
    
    @Before public void setupSchema() {
        if (!HTML5Schema.IS_POPULATED) {
            System.err.println("Using cached HTML5Schema - this should not be thrown in the build!!!");
            
            // FIXME We should really build a new one of these from the TSSL, rather than using pre-built ones from the build
            cleaner.setSchema(new GeneratedTestHTML5Schema());
        }
    }

}
