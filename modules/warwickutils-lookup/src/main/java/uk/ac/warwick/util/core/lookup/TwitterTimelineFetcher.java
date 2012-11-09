package uk.ac.warwick.util.core.lookup;

import uk.ac.warwick.util.cache.CacheEntryUpdateException;

public interface TwitterTimelineFetcher {
    
    TwitterTimelineResponse get(String accountName, int num, boolean includeRetweets) throws CacheEntryUpdateException;

}
