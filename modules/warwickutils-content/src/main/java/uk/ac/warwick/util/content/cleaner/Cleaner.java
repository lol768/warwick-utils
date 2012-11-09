package uk.ac.warwick.util.content.cleaner;

import uk.ac.warwick.util.content.MutableContent;

public interface Cleaner {
    
    String clean(final String input, final MutableContent mc);

}
