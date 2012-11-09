package uk.ac.warwick.util.web.view;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.ProcessingInstruction;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.View;

import com.google.common.collect.Maps;
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
            WireFeed feed = getFeed(model, request);
            
            Document doc = output.outputJDom(feed);
            postProcess(doc);
            
            StringWriter sw = new StringWriter(OUTPUT_BYTE_ARRAY_INITIAL_SIZE);         
            
            Format format = Format.getPrettyFormat();
            format.setEncoding(getCharacterEncoding());
            
            XMLOutputter outputter = new XMLOutputter(format);
            outputter.output(doc, sw);
            sw.flush();
            
            String encoded = postProcess(sw.toString());
            FileCopyUtils.copy(encoded, writer);
            
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
    
    public void postProcess(Document doc) {
        // Override to (e.g.) add XSL
    }
    
    public String postProcess(String input) {
        // Override to (e.g.) escape high characters for ISO-8859-1 output
        return input;
    }
    
    public abstract T getFeed(Map<String, Object> model, HttpServletRequest request) throws Exception;
    
    public abstract String getCharacterEncoding();
    
    public static final <T extends WireFeed> AbstractXMLAggregationView<T> of(T feed, String contentType) {
        return of(feed, contentType, DEFAULT_CHARACTER_ENCODING);
    }
    
    public static final <T extends WireFeed> AbstractXMLAggregationView<T> of(final T feed, final String contentType, final String characterEncoding) {
        return new AbstractXMLAggregationView<T>() {
            @Override
            public T getFeed(Map<String, Object> model, HttpServletRequest request) throws Exception {
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
    
    protected final void addXslToDocument(Document doc, String path) {
        Map<String,String> xsl = Maps.newHashMap();
        xsl.put("href", path);
        xsl.put("type", "text/xsl");
        xsl.put("media", "screen");
        ProcessingInstruction pXsl = new ProcessingInstruction("xml-stylesheet", xsl);
        doc.addContent(0, pXsl);
    }

}
