package uk.ac.warwick.util.content.cleaner;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.ac.warwick.util.content.texttransformers.NewWindowLinkTextTransformer;

/**
 * Substitute for the JScript cleanup routine. TODO make this much more
 * configurable and less hardcoded
 * 
 * @author cusebr
 */
public final class HtmlCleaner {
    
    public static final Logger LOGGER = Logger.getLogger(HtmlCleaner.class); 

    private final Map<String,String> straightReplacements;
    private final Map<Pattern,String> regexReplacements;
    
    private final HtmlContentWriter contentWriter;
    
    private boolean allowJavascriptHandlers = true;
    
    public HtmlCleaner() {
    	this(null);
    }
    
    public HtmlCleaner(HtmlContentWriter theContentWriter) {
    	this.contentWriter = theContentWriter;
    	
        this.straightReplacements = new HashMap<String,String>();
        this.straightReplacements.put("mce_thref=", "href=");
        this.straightReplacements.put("mce_tsrc=", "src=");
        this.straightReplacements.put(NewWindowLinkTextTransformer.HTML_IMAGE, "");
        
        this.regexReplacements = new HashMap<Pattern, String>();
        this.regexReplacements.put(Pattern.compile("<br mce_bogus=\"?1\"?\\s*/?>",Pattern.CASE_INSENSITIVE), "");
        this.regexReplacements.put(Pattern.compile("(<t[dh][^>]*)\\salign=[\"']?middle[\"']?",Pattern.CASE_INSENSITIVE), "$1 align=\"center\"");
    }
    
    public String clean(final String input) {
        String text = doPreParsingCleanup(input);
        
        Parser parser = new Parser();
        
        TagAndAttributeFilter filter = new TagAndAttributeFilterImpl();
        filter.setAllowJavascriptHandlers(isAllowJavascriptHandlers());
        
        CleanerWriter handler = new CleanerWriter(filter);
        
        if (contentWriter != null) {
        	contentWriter.setDelegate(handler.getContentWriter());
        	handler.setContentWriter(contentWriter);
        }
        try {
            InputSource is = new InputSource(new StringReader(text));
            parser.setFeature(Parser.defaultAttributesFeature, false);
            parser.setContentHandler(handler);

            parser.setProperty(Parser.lexicalHandlerProperty, handler);
            parser.parse(is);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException("HTML cleanup error", e);
        }
        return handler.getOutput();
    }

    /**
     * Do simple find-and-replaces that should be done before
     * SAX parsing.
     */
    private String doPreParsingCleanup(final String input) {
        String text = encodeLoneTags(input);
        for (String key : straightReplacements.keySet()) {
            text = text.replace(key, straightReplacements.get(key));
        }
        for (Pattern pattern : regexReplacements.keySet()) {
            text = pattern.matcher(text).replaceAll(regexReplacements.get(pattern));
        }
        
        text = text.replaceAll("<!--\\[(.+?)]-->", "");
        
        return text;
    }

    /*
     * Idea: put (<script.*>)?PATTERN(</script>)? and then if
     * the first and last matching groups are not empty, we matched
     * within a script tag and should skip it.
     */
    String encodeLoneTags(final String input) {
        
        Pattern noScriptTags = Pattern.compile("<script[^>]*>(.*?)</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        
        Matcher matcher = noScriptTags.matcher(input);
        StringBuilder sb = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;    
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            sb.append(doEscaping(input.substring(lastMatch, startIndex)));
            sb.append(input.substring(startIndex, endIndex));
            lastMatch = endIndex;
        }
        
        sb.append(doEscaping(input.substring(endIndex)));
        
        return sb.toString();
    }
    
    String doEscaping(final String input) {
        String result = input;
        Pattern p = Pattern.compile("<([^a-zA-Z?!/])");
        Matcher m = p.matcher(result);
        result = m.replaceAll("&lt;$1");
        return result;
    }

    enum ContentType {
        none, elementStart, elementEnd, characters, whitespace
    }

	public boolean isAllowJavascriptHandlers() {
		return allowJavascriptHandlers;
	}

	public void setAllowJavascriptHandlers(boolean allowJavascriptHandlers) {
		this.allowJavascriptHandlers = allowJavascriptHandlers;
	};
}
