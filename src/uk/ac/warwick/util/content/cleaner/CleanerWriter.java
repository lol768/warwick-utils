package uk.ac.warwick.util.content.cleaner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import uk.ac.warwick.util.content.cleaner.HtmlCleaner.ContentType;
import uk.ac.warwick.util.core.StringUtils;

/**
 * A basic handler that strips out a selection of tags and attributes but
 * otherwise passes the content back out as provided by the parser.
 */
public final class CleanerWriter implements ContentHandler, LexicalHandler {
    public static final int INDENT_DEPTH = 2;

    public static final String HEAD_TAG = "head";

    public static final String BODY_TAG = "body";

    public static final String HTML_TAG = "html";
    
    public static final int MAX_STACK_SIZE = 1000;
    
    private static final Logger LOGGER = Logger.getLogger(CleanerWriter.class);

    private final Set<String> inlineTags = toSet(new String[] { "a", "abbr", "acronym", "span", "b", "basefont", "u", "em",
            "strong", "i", "strike", "img", "del", "ins", "sup", "sub", "code", "br", "tt", "cite", "label", "big", "small",
            "q", "s", "samp" });

    // tags which want an extra line break after they close.
    private final Set<String> extraLineBreak = toSet(new String[] { "h1", "h2", "h3", "h4", "h5", "h6", "p", "ol", "ul",
            "script", "table", "hr", "div", "form" });

    // tags in which not to remove newlines and such
    private final Set<String> preformattedTags = toSet(new String[] { "code", "pre", "script", "style" });
    
    //private final Set<String> trimContentTags = toSet(new String[] { "pre" });
    
    private final Map<String, String> tagReplacements;

    private final TagAndAttributeFilter filter;
    
    private HtmlContentWriter contentWriter;
    
    private BodyContentFilter contentFilter = BodyContentFilter.DO_NOTHING_CONTENT_FILTER;

    private TrackingStringBuilder buffer;

    private int tagDepth;

    private int preformattedTagDepth;

    private boolean inHead;

    private boolean inScript;

    private boolean elementJustOpened;

    // As long as pushTag() and popTag() are used,
    // these two stacks should stay the same size.
    private Stack<StackElement> tagStack;
    private Stack<String> tagNameStack;

    private ContentType lastContentType = ContentType.none;

    public CleanerWriter(final TagAndAttributeFilter theFilter) {
        this.buffer = new TrackingStringBuilder();
        this.filter = theFilter;
        
        this.contentWriter = new DefaultHtmlContentWriter(filter, contentFilter);

        this.tagReplacements = new HashMap<String, String>();
        this.tagReplacements.put("b", "strong");
        this.tagReplacements.put("h1", "h2");
        this.tagReplacements.put("i", "em");
    }
    
    public CleanerWriter(final TagAndAttributeFilter theFilter, final BodyContentFilter theContentFilter) {
        this(theFilter);
        this.contentFilter = theContentFilter;
    }

    public void startDocument() throws SAXException {
        buffer = new TrackingStringBuilder();
        tagDepth = 0;
        preformattedTagDepth = 0;
        inHead = false;
        elementJustOpened = false;
        tagStack = new Stack<StackElement>();
        tagNameStack = new Stack<String>();
    }

    public String getOutput() {
        return buffer.toString();
    }

    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        String characters = String.copyValueOf(ch, start, length);
        // javascript tags are cleverly unconverted, so
        // we shouldn't try to process it
        if (!inScript) {
            characters = contentWriter.htmlEscapeAll(characters);
            characters = contentFilter.handleCharacters(characters);
        }
        if (isInPreformattedArea()) {
            buffer.append(characters);
            lastContentType = ContentType.characters;
        } else if (StringUtils.hasText(characters)) {
            characters = StringUtils.compactWhitespace(characters);
            buffer.append(characters);
            lastContentType = ContentType.characters;
        } else if (!buffer.isStartOfLine()) {
            buffer.append(' ');
            lastContentType = ContentType.whitespace;
        }
    }

    /**
     * Renders the opening tag, including a self-closing slash if it's a
     * self-closing tag (img, br);
     */
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
    	String tagName = localName;
        // Slight hack to ignore <html>, <body>, and everything inside
        // <head>
        if (handleUnwantedSurroundingsStart(localName, atts)) {
        	pushTag(new StackElement(tagName, tagName, atts, false));
            return;
        }

        if (localName.equals("script")) {
            inScript = true;
        }

        if (preformattedTagDepth == 0) {
            tagName = handleReplacements(localName);
        }
        
        Map<String, String> attributes = new HashMap<String, String>();
        for (int i = 0; i < atts.getLength(); i++) {
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            attributes.put(name, value);
        }
        
        AttributesImpl attsImpl = new AttributesImpl(atts);
        
        /**
         * [UTL-64]
         * When Chrome/Safari use spans for formatting, TinyMCE
         * will put the alternate element name in mce_name.
         */
        String mceName = attsImpl.getValue("mce_name");
        if (mceName != null) {
        	tagName = mceName;
        	// remove all attributes.
        	attsImpl.clear();
        }

        /**
         * If we have changed tagName, it will go on the
         * stack and get remembered for the close element. 
         */
        pushTag(new StackElement(localName, tagName, attsImpl, true));

        beforeElementStart(tagName);
        
        String renderedTag = contentWriter.renderStartTag(tagName,attsImpl);
        
        buffer.append(contentFilter.handleTagString(renderedTag, tagName, attributes));

        if (tagName.equals("br") && !isInPreformattedArea()) {
            buffer.append("\n");
            appendIndentSpaces();
        }

        elementJustOpened = true;
        lastContentType = ContentType.elementStart;
    }

    private void beforeElementStart(final String localName) {
        if (isPreformatted(localName)) {
            preformattedTagDepth++;
        }
        if (!isInlineTag(localName)) {
            if (elementJustOpened && !isInPreformattedArea()) {
                buffer.append("\n");
            }
            appendIndentSpaces();
            if (!contentWriter.isSelfCloser(localName)) {
                tagDepth++;
            }
        }
    }

    /**
     * Closes a tag, if it's not self closing. Adds line breaks after it if
     * it's not an inline tag.
     */
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
    	String tagName = localName;
        // Slight hack to ignore <html>, <body>, and everything inside
        // <head>
        if (handleUnwantedSurroundingsEnd(tagName)) {
        	popTag(tagName);
            return;
        }

        if (localName.equals("script")) {
            this.inScript = false;
        }

        if (this.preformattedTagDepth == 0) {
            tagName = handleReplacements(localName);
        }
        
        StackElement poppedTag = popTag(tagName);
        if (poppedTag == null || !poppedTag.isPrint() || contentWriter.isSelfCloser(poppedTag.getTagName())) {
            return;
        }

        doEndElement(poppedTag.getTagName());
    }

    private void doEndElement(final String tagName) {
        if (isPreformatted(tagName)) {
            this.preformattedTagDepth--;
        }
        if (isInlineTag(tagName)) {
            renderClosingTag(tagName);
        } else {
            tagDepth--;
            if (this.lastContentType == ContentType.elementEnd) {
                appendIndentSpaces();
            }
            renderClosingTag(tagName);
            renderLineBreaksAfterTagClose(tagName);
        }

        this.elementJustOpened = false;
        this.lastContentType = ContentType.elementEnd;
    }

    public void comment(final char[] data, final int start, final int length) throws SAXException {
    	if (start < 0 || length < 0) {
    		return;
    	}
        String characters = String.copyValueOf(data, start, length);
        buffer.append("<!--");
        buffer.append(characters);
        buffer.append("-->");
    }

    private void appendIndentSpaces() {
        if (isInPreformattedArea()) {
            return;
        }
        for (int i = 0; i < tagDepth * INDENT_DEPTH; i++) {
            buffer.append(" ");
        }
    }

    

    private boolean handleUnwantedSurroundingsStart(final String localName, final Attributes atts) {
        boolean skipTag = false;
        if (localName.equals(HTML_TAG) || localName.equals(BODY_TAG) || localName.equals(HEAD_TAG)
                || !filter.isTagAllowed(localName, tagNameStack, false, atts)) {
            skipTag = true;
        }
        if (localName.equals(HEAD_TAG)) {
            inHead = true;
        }
        if (inHead) {
            skipTag = true;
        }
        return skipTag;
    }

    private boolean handleUnwantedSurroundingsEnd(final String localName) {
        boolean skipTag = false;
        if (localName.equals(HTML_TAG) || localName.equals(BODY_TAG) || localName.equals(HEAD_TAG)
                || !filter.isTagAllowed(localName, tagNameStack, true, peekTag(localName).getAttributes())) {
            skipTag = true;
        }
        if (localName.equals(HEAD_TAG)) {
            inHead = false;
        }
        if (inHead) {
            skipTag = true;
        }
        return skipTag;
    }

    private void renderLineBreaksAfterTagClose(final String localName) {
        if (isInPreformattedArea()) {
            // do nothing
            return;
        } else if (requiresExtraLineBreak(localName)) {
            buffer.append("\n\n");
        } else {
            buffer.append("\n");
        }
    }

    private void renderClosingTag(final String localName) {
        buffer.append(contentWriter.renderEndTag(localName));
    }

    private boolean isInlineTag(final String tagName) {
        return inlineTags.contains(tagName);
    }

    private boolean isPreformatted(final String tagName) {
        return preformattedTags.contains(tagName);
    }

    private boolean isInPreformattedArea() {
        return preformattedTagDepth > 0;
    }

    private boolean requiresExtraLineBreak(final String tagName) {
        return extraLineBreak.contains(tagName);
    }

    private void pushTag(final StackElement element) {
        tagStack.push(element);
        tagNameStack.push(element.getTagName());
    }


    /**
     * Pop a tag off the stack, and return it.
     * 
     * We used to check that the element matched the stack
     * element and just skip it if it didn't match - but 
     * we want to be able to replace one element with another
     * so by returning the element from the stack, we can
     * correctly close the replaced element.
     */
    private StackElement popTag(final String localName) {
        if (tagStack.empty()) {
            //Strange... but we press on
            return null;
        }
        tagNameStack.pop();
        return tagStack.pop();
    }
    
    private StackElement peekTag(final String localName) {
        if (tagStack.empty()) {
            //Strange... but we press on
            return null;
        }
        return tagStack.peek();
    }

    private String handleReplacements(final String tagName) {
        if (tagReplacements.containsKey(tagName)) {
            return tagReplacements.get(tagName);
        }
        return tagName;
    }

    public void endDocument() throws SAXException {
    }

    public void endPrefixMapping(final String prefix) throws SAXException {
    }

    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
    }

    public void processingInstruction(final String target, final String data) throws SAXException {
    }

    public void setDocumentLocator(final Locator locator) {
    }

    public void skippedEntity(final String name) throws SAXException {
    }

    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
    }

    public static Set<String> toSet(final String[] array) {
        return new TreeSet<String>(Arrays.asList(array));
    }

    public void endCDATA() throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void endEntity(final String arg0) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void startDTD(final String arg0, final String arg1, final String arg2) throws SAXException {
    }

    public void startEntity(final String arg0) throws SAXException {
    }
    
    public HtmlContentWriter getContentWriter() {
        return contentWriter;
    }

    public void setContentWriter(HtmlContentWriter contentWriter) {
        this.contentWriter = contentWriter;
    }
    
    static class StackElement {
    	private final String originalTagName;
    	private final String tagName;
    	private final Attributes attributes;
    	private final boolean print;

		public StackElement(String originalTagName, String tagName, Attributes atts,
				boolean print) {
			super();
			this.originalTagName = originalTagName;
			this.tagName = tagName;
			this.attributes = atts;
			this.print = print;
		}
		public final String getOriginalTagName() {
			return originalTagName;
		}
		public final String getTagName() {
			return tagName;
		}
		public final boolean isPrint() {
			return print;
		}
		public final Attributes getAttributes() {
		    return attributes;
		}
    }
}
