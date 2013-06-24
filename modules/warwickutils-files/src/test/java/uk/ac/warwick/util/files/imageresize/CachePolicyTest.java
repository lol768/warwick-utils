package uk.ac.warwick.util.files.imageresize;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.warwick.util.files.imageresize.CachePolicy;

public final class CachePolicyTest {

    @Test
    public void stringRepsentation() {
        assertEquals("", CachePolicy.NO_POLICY.toString());
        assertEquals("private, max-age=0", CachePolicy.PRIVATE.toString());
        assertEquals("public, max-age=0", CachePolicy.PUBLIC.toString());
        assertEquals("public, max-age=7200, stale-while-revalidate=60", CachePolicy.PUBLIC_IMAGES.toString());
        assertEquals("public, max-age=31536000, stale-while-revalidate=60", CachePolicy.SITE_ASSET.toString());
    }

}
