package uk.ac.warwick.util.files.hash;

import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;


public final class CompositeFileHashResolverTest {
    private CompositeFileHashResolver resolver;
    private Mockery m = new Mockery();
    private FileHashResolver defaultResolver;
    private FileHashResolver htmlResolver;
    
    @Before public void setUp() {
        Map<String, FileHashResolver> resolvers = Maps.newHashMap();
        defaultResolver = m.mock(FileHashResolver.class, "defaultResolver");
        htmlResolver = m.mock(FileHashResolver.class, "htmlResolver");
        resolvers.put("default", defaultResolver);
        resolvers.put("html", htmlResolver);
        resolver = new CompositeFileHashResolver(resolvers);
    }
    
    @Test public void useDefault() throws Exception {
        m.checking(new Expectations(){{
            one(defaultResolver).lookupByHash(null, new HashString("abcdefghjijklmn"), true);
        }});
        resolver.lookupByHash(null, new HashString("abcdefghjijklmn"), true);
        m.assertIsSatisfied();
    }
    
    @Test public void useHtmlStore() throws Exception {
        m.checking(new Expectations(){{
            one(htmlResolver).lookupByHash(null, new HashString("html/abcdefghjijklmn"), true);
        }});
        resolver.lookupByHash(null, new HashString("html/abcdefghjijklmn"), true);
        m.assertIsSatisfied();
    }
    
    
    @Test(expected=IllegalArgumentException.class) public void missingStore() throws Exception {
        resolver.lookupByHash(null, new HashString("unknownstore/abcdefghjijklmn"), true);
    }
}
