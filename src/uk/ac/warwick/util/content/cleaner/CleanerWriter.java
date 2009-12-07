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

    private Stack<String> tagStack;

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
        tagStack = new Stack<String>();
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
        // Slight hack to ignore <html>, <body>, and everything inside
        // <head>
        if (handleUnwantedSurroundingsStart(localName, atts))
            return;

        if (localName.equals("script")) {
            inScript = true;
        }

        String tagName = localName;

        if (preformattedTagDepth == 0) {
            tagName = handleReplacements(localName);
        }

        pushTag(tagName);

        beforeElementStart(tagName);
        
        String renderedTag = contentWriter.renderStartTag(tagName,atts);
        
        Map<String, String> attributes = new HashMap<String, String>();
        
        for (int i = 0; i < atts.getLength(); i++) {
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            
            attributes.put(name, value);
        }
        
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
        // Slight hack to ignore <html>, <body>, and everything inside
        // <head>
        if (handleUnwantedSurroundingsEnd(localName))
            return;

        if (localName.equals("script")) {
            this.inScript = false;
        }

        String tagName = localName;

        if (this.preformattedTagDepth == 0) {
            tagName = handleReplacements(localName);
        }
        
        boolean tagWeShouldIgnore = false;
        if (!popTag(tagName)) {
            tagWeShouldIgnore = true;
        }

        if (tagWeShouldIgnore || contentWriter.isSelfCloser(tagName)) {
            return;
        }

        doEndElement(tagName);
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
        buffer.append("<!--" + characters + "-->");
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
                || !filter.isTagAllowed(localName, tagStack, false, atts)) {
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
                || !filter.isTagAllowed(localName, tagStack, true, null)) {
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

    private void pushTag(final String localName) {
        tagStack.push(localName);
    }

    /**
     * Pop a tag off the stack, checking that it's the same tag that was
     * pushed on. If not, then there has been a mismatch of opening and
     * closing tags which should never ever happen in XML.
     * 
     * Returns whether the tag matched in the stack. If false, it's
     * most likely one of those situations, so the caller should ignore
     * the closing tag.
     */
    private boolean popTag(final String localName) {
        if (tagStack.empty()) {
            //Strange... but we press on
            return false;
        }
        
        boolean found;
        
        String top = tagStack.peek();
        if (top.equals(localName)) {
            tagStack.pop();
            found = true;
        } else {
            /*
            It may get here as part of what seems like a bug in TagSoup, where eg
            <b><strong> may only result in one startElement, but two endElements.
            So if we just don't do anything, things will be fine.
           */
           //throw new IllegalStateException("Closing tag '" + localName + "' doesn't match top of stack '" + top + "'");
           found = false;
           LOGGER.warn("HTML cleanup encountered confusing combination of tags; working around it");
        }
        
        return found;
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
}
