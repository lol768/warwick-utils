package uk.ac.warwick.util.web.view;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.io.WireFeedOutput;

public abstract class AbstractXMLAggregationView<T extends WireFeed> implements View {
    
    private static final int OUTPUT_BYTE_ARRAY_INITIAL_SIZE = 4096;
    
    private static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(OUTPUT_BYTE_ARRAY_INITIAL_SIZE);
        OutputStreamWriter writer = new OutputStreamWriter(baos, getCharacterEncoding());
        
        try {
            WireFeedOutput output = new WireFeedOutput();
            output.output(getFeed(model), writer);
            
            writer.flush();
            
            // Write content type and also length (determined via byte array).
            response.setContentType(getContentType());
            response.setContentLength(baos.size());
            response.setCharacterEncoding(getCharacterEncoding());

            // Flush byte array to servlet output stream.
            ServletOutputStream out = response.getOutputStream();
            baos.writeTo(out);
            out.flush();
        } finally {
            writer.close();
        }
    }
    
    public abstract T getFeed(Map<String, Object> model) throws Exception;
    
    public abstract String getCharacterEncoding();
    
    public static final <T extends WireFeed> AbstractXMLAggregationView<T> of(T feed, String contentType) {
        return of(feed, contentType, DEFAULT_CHARACTER_ENCODING);
    }
    
    public static final <T extends WireFeed> AbstractXMLAggregationView<T> of(final T feed, final String contentType, final String characterEncoding) {
        return new AbstractXMLAggregationView<T>() {
            @Override
            public T getFeed(Map<String, Object> model) throws Exception {
                return feed;
            }

            @Override
            public String getCharacterEncoding() {
                return characterEncoding;
            }
            
            public String getContentType() {
                return contentType;
            }
        };
    }

}
