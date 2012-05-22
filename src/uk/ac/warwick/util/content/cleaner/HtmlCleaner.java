package uk.ac.warwick.util.content.cleaner;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.Parser;
import org.ccil.cowan.tagsoup.Schema;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.ac.warwick.html5.HTML5Schema;
import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.collections.Triple;
import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.texttransformers.NewWindowLinkTextTransformer;
import uk.ac.warwick.util.core.ObjectProvider;

import com.google.common.collect.Lists;

/**
 * Substitute for the JScript cleanup routine. 
 */
public final class HtmlCleaner implements Cleaner {
    
    public static final Logger LOGGER = Logger.getLogger(HtmlCleaner.class); 

    private final List<Pair<String,String>> straightReplacements;
    private final List<Pair<String,String>> postParseStraightReplacements;
    private final List<Triple<Pattern,String,String>> regexReplacements;
    private final List<Triple<Pattern,String,String>> postParseRegexReplacements;
    
    private final HtmlContentWriter contentWriter;
    
    private ObjectProvider<TagAndAttributeFilter> filterProvider = new ObjectProvider<TagAndAttributeFilter>() {
        public TagAndAttributeFilter newInstance() {
            return new TagAndAttributeFilterImpl();
        }
    };
    
    private boolean allowJavascriptHandlers = true;
    
    private Schema schema = new HTML5Schema();
    
    public HtmlCleaner() {
    	this(null);
    }
    
    public HtmlCleaner(HtmlContentWriter theContentWriter) {
    	this.contentWriter = theContentWriter;
    	
        this.straightReplacements = Lists.newArrayList();
        this.straightReplacements.add(Pair.of("_mce_thref=", "href="));
        this.straightReplacements.add(Pair.of("_mce_tsrc=", "src="));
        this.straightReplacements.add(Pair.of("mce_thref=", "href="));
        this.straightReplacements.add(Pair.of("mce_tsrc=", "src="));
        this.straightReplacements.add(Pair.of(NewWindowLinkTextTransformer.HTML_IMAGE, ""));
        this.straightReplacements.add(Pair.of("\u00b7", "&#183;"));
        this.straightReplacements.add(Pair.of("&#65279;", ""));
        
        this.regexReplacements = Lists.newArrayList();
        
        //SBTWO-4230 compact spaces and nbsp
        this.regexReplacements.add(Triple.of(Pattern.compile("&nbsp;(&nbsp;)+"), "&nbsp;", "&nbsp;"));
        this.regexReplacements.add(Triple.of(Pattern.compile(">(&nbsp;| )*&nbsp;(&nbsp;| )*<"), "&nbsp;", ">_NONBREAKINGSPACE_<"));
        this.regexReplacements.add(Triple.of(Pattern.compile("&nbsp;"), "&nbsp;", " "));
        this.regexReplacements.add(Triple.of(Pattern.compile("_NONBREAKINGSPACE_"), "_nonbreakingspace_", "&nbsp;"));
        
        this.regexReplacements.add(Triple.of(Pattern.compile("<!--\\[if [a-z]+ mso \\d*\\]>.*?<!\\-*\\[endif\\].*?-->",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "[endif]", ""));
        this.regexReplacements.add(Triple.of(Pattern.compile("<!--\\[if supportFields\\]>.*?<!\\[endif\\]-->",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "[if supportfields]", ""));// MS Word lists
        this.regexReplacements.add(Triple.of(Pattern.compile("<!--\\[if !mso\\]>.*?<!-*\\[endif\\]-->",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "[if !mso]", ""));// MS Word
        this.regexReplacements.add(Triple.of(Pattern.compile("<!--\\[if gte vml 1\\]>.*?<!\\[endif\\]-->",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "[if gte vml 1]", ""));// MS Word VML
        
        this.regexReplacements.add(Triple.of(Pattern.compile("<br _?mce_bogus=\"?1\"?\\s*/?>",Pattern.CASE_INSENSITIVE), "_bogus", ""));
        this.regexReplacements.add(Triple.of(Pattern.compile("<br data-mce-bogus=\"?1\"?\\s*/?>",Pattern.CASE_INSENSITIVE), "data-mce-bogus", ""));
        this.regexReplacements.add(Triple.of(Pattern.compile("<mce:style([^>]*)>\\<\\!\\-\\-(.*?)\\-\\-\\></mce:style>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "</mce:style>", "<style$1>$2</style>"));
        this.regexReplacements.add(Triple.of(Pattern.compile("<style[^>]* _?mce_bogus=\"?1\"?\\s*>.*?</style>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "</style>", ""));
        this.regexReplacements.add(Triple.of(Pattern.compile("<style[^>]* data-mce-bogus=\"?1\"?\\s*>.*?</style>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "</style>", ""));
        this.regexReplacements.add(Triple.of(Pattern.compile("<mce\\:([a-z]*)([^>]*)>(.*?)<\\/mce\\:\\1>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "<mce:", "<$1$2>$3</$1>"));
        this.regexReplacements.add(Triple.of(Pattern.compile("<p>\\s*(<script.*?<\\/script>)\\s*</p>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "</script>", "$1"));
        this.regexReplacements.add(Triple.of(Pattern.compile("(<t[dh][^>]*)\\salign=[\"']?middle[\"']?",Pattern.CASE_INSENSITIVE), "middle", "$1 align=\"center\""));
        this.regexReplacements.add(Triple.of(Pattern.compile("(<t[dh][^>]*>)\\s*(</t[dh]>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "</t", "$1&nbsp;$2"));
        
        // SBTWO-5117 fix merge placeholder para wrapping (indeed any comment para wrapping)
        this.regexReplacements.add(Triple.of(Pattern.compile("<p>\\s*(<!--.*?-->)\\s*</p>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "<!--", "$1"));
        // SBTWO-5135 fix mce-text/javascript
        this.regexReplacements.add(Triple.of(Pattern.compile("(mce-)+text/javascript", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "<script", "text/javascript"));
        
        // MS Word idiocy
        this.regexReplacements.add(Triple.of(Pattern.compile("<p>(.*?)<meta[^>]+>(.*?)</p>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "<meta", "<p>$1$2</p>"));
        this.regexReplacements.add(Triple.of(Pattern.compile("<p>(.*?)<title>[^<]*</title>(.*?)</p>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "</title>", "<p>$1$2</p>"));
        // Now handled in CleanerWriter.
        //this.regexReplacements.add(Triple.of(Pattern.compile("<p>(.*?)<style[^<]*</style>(.*?)</p>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "</style>", "<p>$1$2</p>"));
        this.regexReplacements.add(Triple.of(Pattern.compile("<p>(.*?)<link[^>]+>(?:</link>)?(.*?)</p>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "<link", "<p>$1$2</p>"));
        
        // MS Word lists
        this.regexReplacements.add(Triple.of(Pattern.compile("<p[^>]*class=\"?Mso(?:[A-Z][a-z]+)+\"?[^>]*>" +
        		"(?:<!--\\[if !supportLists\\]-->)?" +
        		"(?:<\\/?(?:span|font)[^>]*>)*" +
        		"(?:&#183;|\u00b7)" +
        		"(?:<\\/?(?:span|font)[^>]*>)*" +
        		"(?:&nbsp;)*\\s*" +
        		"(?:<\\/?(?:span|font)[^>]*>)*" +
        		"(?:<!--\\[endif\\]-->)?" +
        		"(.*?)" +
        		"(?:<\\/?(?:span|font)[^>]*>)*" +
        		"</p>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "&#183;", "<li>$1</li>"));
        
        this.postParseStraightReplacements = Lists.newArrayList();
        this.postParseStraightReplacements.add(Pair.of("<strong></strong>", ""));
        
        this.postParseRegexReplacements = Lists.newArrayList();
        this.postParseRegexReplacements.add(Triple.of(Pattern.compile("<p>\\s*</p>\n*"), "</p>", ""));
        
        // TinyMCE 3 indents use padding-left - [SBTWO-3017]
        this.regexReplacements.add(Triple.of(Pattern.compile("\\bstyle=(\"padding-left:\\s*\\d{2,}px;?\\s*\")",Pattern.CASE_INSENSITIVE), "padding-left", "tinymce_indent=$1"));
        this.postParseRegexReplacements.add(Triple.of(Pattern.compile("\\btinymce_indent=(\"padding-left:\\s*\\d{2,}px;?\\s*\")(?:\\sstyle=\"[^\"]*\")?",Pattern.CASE_INSENSITIVE), "tinymce_indent", "style=$1"));
        this.postParseRegexReplacements.add(Triple.of(Pattern.compile("<table\\sstyle=\"padding(-left:\\s*\\d{2,}px;?\\s*)\"",Pattern.CASE_INSENSITIVE), "<table", "<table style=\"margin$1\""));
        this.postParseRegexReplacements.add(Triple.of(Pattern.compile("\\s*<p>\\s*(<br\\s*/?>)?\\s*</p>\\s*$",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "</p>", ""));
        
        // SBTWO-3782 - remove contentless lightbox links
        this.postParseRegexReplacements.add(Triple.of(Pattern.compile("<a [^>]+rel=\"lightbox\\[[^>]+></a>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "lightbox[", ""));
    }
    
    public String clean(final String input, final MutableContent mc) {
        String text = doPreParsingCleanup(input);
        
        Parser parser = new Parser();
        
        TagAndAttributeFilter filter = filterProvider.newInstance();
        filter.setAllowJavascriptHandlers(isAllowJavascriptHandlers());
        
        CleanerWriter handler = new CleanerWriter(filter, mc);
        
        if (contentWriter != null) {
        	contentWriter.setDelegate(handler.getContentWriter());
        	handler.setContentWriter(contentWriter);
        }
        try {
            InputSource is = new InputSource(new StringReader(text));
            parser.setFeature(Parser.defaultAttributesFeature, false);
            parser.setContentHandler(handler);

            parser.setProperty(Parser.lexicalHandlerProperty, handler);
            
            // HTML 5 Schema
            parser.setProperty(Parser.schemaProperty, schema);
            
            parser.parse(is);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException("HTML cleanup error", e);
        }
        
        return doPostParsingCleanup(handler.getOutput());
    }

	/**
     * Do simple find-and-replaces that should be done before
     * SAX parsing.
     */
    String doPreParsingCleanup(final String input) {
        String text = encodeLoneTags(input);
        
        for (Pair<String, String> replacement : straightReplacements) {
            text = text.replace(replacement.getLeft(), replacement.getRight());
        }
        
        for (Triple<Pattern,String,String> replacement : regexReplacements) {        	
        	if (text.toLowerCase().indexOf(replacement.getMiddle()) == -1) {
        		continue;
        	}
        	
        	int attempts = 10;
        	while (replacement.getLeft().matcher(text).find() && (attempts-- > 0)) {
       			text = replacement.getLeft().matcher(text).replaceAll(replacement.getRight());
        	}
        }
        
        // Do this regex seperately; it's nasty!
        text = doComplexOfficeTags(text);
        
        text = doOfficeStyles(text);
        
        text = text.replaceAll("<!--\\[(.+?)]-->", "");
        
        return text;
    }

	private String doComplexOfficeTags(String text) {
        if (text.indexOf("Mso") != -1 && text.indexOf("</o:p>") != -1) {
        	// Since this is a nasty regular expression, split it into some sub-expressions.
        	Pattern outerPattern = Pattern.compile("<p[^>]*class=\"?Mso[a-z]+\"?[^>]*>(.*?)</p>", Pattern.CASE_INSENSITIVE);
        	Pattern innerPattern = Pattern.compile("(?:<\\?xml[^>]*>)?(?:<b style[^>]*>)?<o:p>(?:<font[^>]*>)?&nbsp;(?:</font>)?</o:p>(</b>)?", Pattern.CASE_INSENSITIVE);
        	Pattern innerPattern2 = Pattern.compile("<span [^>]*mce_name=\"strong\"[^>]*><o:p>(?:<font[^>]*>)?&nbsp;(?:</font>)?</o:p></span>", Pattern.CASE_INSENSITIVE);
        	
        	Matcher outerMatcher = outerPattern.matcher(text);
        	
        	StringBuilder sb = new StringBuilder();
            
            int lastMatch = 0;
            int startIndex = 0;
            int endIndex = 0;
            
            while (outerMatcher.find()) {
                startIndex = outerMatcher.start();
                endIndex = outerMatcher.end();

                sb.append(text.substring(lastMatch, startIndex));
                
                String inner = text.substring(startIndex, endIndex);
                
                Matcher innerMatcher = innerPattern.matcher(outerMatcher.group(1));
                Matcher innerMatcher2 = innerPattern2.matcher(outerMatcher.group(1));
                
                // only append inner if we don't match the inner pattern
                if (!innerMatcher.matches() && !innerMatcher2.matches()) {              	
                	sb.append(inner);
                }
                
                lastMatch = endIndex;
            }
            
            sb.append(text.substring(endIndex));
            
            text = sb.toString();
        }
		return text;
	}
	
	private String doOfficeStyles(String text) {
	    if ((text.toLowerCase().indexOf("<style") != -1 || text.toLowerCase().indexOf("<mce:style") != -1) && text.indexOf("mso-") != -1) {	        
	        Pattern styles = Pattern.compile("<(?:mce\\:)?style[^>]*>(.*?)</(?:mce\\:)?style>\\s*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	        Pattern officeStyle = Pattern.compile("^\\s*mso-.*$", Pattern.MULTILINE);
	        
	        Matcher matcher = styles.matcher(text);
	        
	        StringBuilder sb = new StringBuilder();
	        
	        int lastMatch = 0;
            int startIndex = 0;
            int endIndex = 0;
            
            while (matcher.find()) {
                startIndex = matcher.start();
                endIndex = matcher.end();

                sb.append(text.substring(lastMatch, startIndex));
                
                String inner = text.substring(startIndex, endIndex);
                
                // only append inner if we don't match the inner pattern
                if (!officeStyle.matcher(matcher.group(1)).find()) {
                    sb.append(inner);
                }
                
                lastMatch = endIndex;
            }
            
            sb.append(text.substring(endIndex));
            
            text = sb.toString();
	    }
	    
	    return text;
	}

    /**
     * More simple find and replaces
     */
    private String doPostParsingCleanup(final String output) {
    	String text = output;
    	
        for (Pair<String, String> replacement : postParseStraightReplacements) {
            text = text.replace(replacement.getLeft(), replacement.getRight());
        }
    	
    	for (Triple<Pattern,String,String> replacement : postParseRegexReplacements) {
    		int attempts = 10;
        	while (text.toLowerCase().indexOf(replacement.getMiddle()) != -1 && replacement.getLeft().matcher(text).find() && (attempts-- > 0)) {
        		text = replacement.getLeft().matcher(text).replaceAll(replacement.getRight());
        	}
        }
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
	}

    public void setFilterProvider(ObjectProvider<TagAndAttributeFilter> filterProvider) {
        this.filterProvider = filterProvider;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
