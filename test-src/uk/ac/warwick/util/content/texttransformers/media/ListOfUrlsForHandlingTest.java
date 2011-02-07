package uk.ac.warwick.util.content.texttransformers.media;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import uk.ac.warwick.util.content.MutableContent;

public class ListOfUrlsForHandlingTest extends TestCase {
    public void testNothingDies() throws IOException {      
        Map<String, MediaUrlHandler> handlers = new HashMap<String, MediaUrlHandler>();
        handlers.put("audio", new AudioMediaUrlHandler("",""));
        handlers.put("google", new GoogleMediaUrlHandler());
        handlers.put("youtube", new YouTubeMediaUrlHandler());
        handlers.put("quicktime", new QuickTimeMediaUrlHandler(""));
        handlers.put("avi", new AviMediaUrlHandler(""));
        handlers.put("flv", new FlvMediaUrlHandler("", ""));
        handlers.put("flash", new StandardFlashMediaUrlHandler());
        handlers.put("revver", new RevverMediaUrlHandler());
        handlers.put("metacafe", new MetacafeMediaUrlHandler());
        handlers.put("jumpcut", new JumpcutMediaUrlHandler());
        handlers.put("guba", new GubaMediaUrlHandler());
        handlers.put("ifilm", new IFilmMediaUrlHandler());
        handlers.put("selfcasttv", new SelfcastTVMediaUrlHandler());
        handlers.put("grouper", new GrouperMediaUrlHandler());
        handlers.put("eyespot", new EyespotMediaUrlHandler());
        handlers.put("vimeo", new VimeoMediaUrlHandler());
        handlers.put("myspace", new MySpaceMediaUrlHandler());
                
        MediaUrlTransformer transformer = new MediaUrlTransformer(handlers, "");
        
        for (String tag : readResourceToLines("/mediaTagList.txt")) {
            /*
             * Not actually doing anything with the result at the
             * moment, just checking that an exception isn't thrown.
             */
            transformer.apply(new MutableContent(null, tag));
        }
    }
    
    private List<String> readResourceToLines(final String file) throws IOException {
        InputStream is = getClass().getResourceAsStream(file);
        List<String> result = new ArrayList<String>();
        StringBuffer buf = new StringBuffer();
        
        if (is == null) {
            throw new FileNotFoundException("resource not found: " + file);
        }
        
        InputStreamReader reader = new InputStreamReader(is);
        int c = (char)reader.read();
        do {
            if (c == '\n') {
                result.add(buf.toString());
                buf = new StringBuffer();
            } else {
                buf.append((char)c);
            }
            c = reader.read();
        } while (c != -1);
        
        return result;
    }
}
