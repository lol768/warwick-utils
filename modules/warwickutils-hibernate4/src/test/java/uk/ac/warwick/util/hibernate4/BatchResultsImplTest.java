package uk.ac.warwick.util.hibernate4;

import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.junit.Test;

import com.google.common.base.Function;

public class BatchResultsImplTest  {

	interface Page {
		String getId();
	}
	interface HtmlPage extends Page {
		
	}
	
	Function<Page, String> ID_FUNCTION = new Function<Page, String>() { public String apply(Page from) {
        return from.getId();
    }};
	
    /**
     * You should be able to have an HtmlPage results that uses a Page idFunction, since an HtmlPage is
     * always a Page. At the time of writing this test the type restrictions prevent this from being compiled.
     */
	@Test
	public void superclassIdFunction() {
		ScrollableResults results = null;
		Session session = null;
		new BatchResultsImpl<HtmlPage>(results, 10, ID_FUNCTION, session);
	}

}
