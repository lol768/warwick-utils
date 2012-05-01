package uk.ac.warwick.util.content;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.HTMLEntities;
import org.cyberneko.html.HTMLScanner;
import org.cyberneko.html.filters.NamespaceBinder;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;

public final class NekoHtmlParser implements HtmlParser {
    
    private static final Logger LOGGER = Logger.getLogger(NekoHtmlParser.class);

    private final DOMImplementation documentFactory = DocumentUtils.getDOMImplementation();
    
    public Document parseDOM(String source) throws HtmlParsingException {
        Document document;
        try {
            document = parseDomImpl(source);
        } catch (Exception e) {
            // DOMException is a RuntimeException
            throw new HtmlParsingException("Can't parse DOM", e);
        }
        
        HtmlSerialization.attach(document, new DefaultHtmlSerializer(), source);

        Node html = document.getDocumentElement();

        Node head = null;
        Node body = null;
        LinkedList<Node> beforeHead = Lists.newLinkedList();
        LinkedList<Node> beforeBody = Lists.newLinkedList();

        // Concatenate multiple <body> and <head> tags into a single one of
        // each, and ensure both exist
        while (html.hasChildNodes()) {
            Node child = html.removeChild(html.getFirstChild());
            if (child.getNodeType() == Node.ELEMENT_NODE && "head".equalsIgnoreCase(child.getNodeName())) {
                if (head == null) {
                    head = child;
                } else {
                    // Concatenate <head> elements together.
                    transferChildren(head, child);
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE && "body".equalsIgnoreCase(child.getNodeName())) {
                if (body == null) {
                    body = child;
                } else {
                    // Concatenate <body> elements together.
                    transferChildren(body, child);
                }
            } else if (head == null) {
                beforeHead.add(child);
            } else if (body == null) {
                beforeBody.add(child);
            } else {
                // Both <head> and <body> are present. Append to tail of <body>.
                body.appendChild(child);
            }
        }

        // Ensure head tag exists
        if (head == null) {
            // beforeHead contains all elements that should be prepended to
            // <body>. Switch them.
            LinkedList<Node> temp = beforeBody;
            beforeBody = beforeHead;
            beforeHead = temp;

            // Add as first element
            head = document.createElement("head");
            html.insertBefore(head, html.getFirstChild());
        } else {
            // Re-append head node.
            html.appendChild(head);
        }

        // Ensure body tag exists.
        body = ensureBody(document, html, head, body);

        // Leftovers: nodes before the first <head> node found and the first
        // <body> node found.
        // Prepend beforeHead to the front of <head>, and beforeBody to
        // beginning of <body>,
        // in the order they were found in the document.
        prependToNode(head, beforeHead);
        prependToNode(body, beforeBody);

        return document;
    }

    private Node ensureBody(Document document, Node html, Node head, Node theBody) {
        Node body = theBody;
        if (body == null) {
            // Add immediately after head.
            body = document.createElement("body");
            html.insertBefore(body, head.getNextSibling());
        } else {
            // Re-append body node.
            html.appendChild(body);
        }
        return body;
    }

    private void transferChildren(Node to, Node from) {
        while (from.hasChildNodes()) {
            to.appendChild(from.removeChild(from.getFirstChild()));
        }
    }

    private void prependToNode(Node to, LinkedList<Node> from) {
        while (from.size() > 0) {
            to.insertBefore(from.removeLast(), to.getFirstChild());
        }
    }

    private Document parseDomImpl(String source) throws Exception {
        DocumentHandler handler;

        HTMLConfiguration config = newConfiguration();
        try {
            handler = parseHtmlImpl(source, config);
        } catch (IOException ioe) {
            return null;
        }

        Document document = handler.getDocument();
        Node htmlElement = DocumentUtils.getFirstNamedChildNode(handler.getFragment(), "html");
        if (htmlElement == null) {
            htmlElement = document.createElement("html");
            
            if (DocumentUtils.getFirstNamedChildNode(handler.getFragment(), "body") != null) {
                // Copy all children from the fragment into the html element
                transferChildren(htmlElement, handler.getFragment());
            } else {
                Element bodyElement = document.createElement("body");
                transferChildren(bodyElement, handler.getFragment());
                
                htmlElement.appendChild(bodyElement);
            }
        }
        
        document.appendChild(htmlElement);
        return document;
    }

    private DocumentHandler parseHtmlImpl(String source, HTMLConfiguration config)
            throws IOException {
        HTMLScanner htmlScanner = new HTMLScanner();

        DocumentHandler handler = newDocumentHandler(source);

        NamespaceBinder namespaceBinder = new NamespaceBinder();
        namespaceBinder.setDocumentHandler(handler);
        namespaceBinder.setDocumentSource(htmlScanner);
        namespaceBinder.reset(config);

        // Order of filter is Scanner -> OSMLFilter -> Tag Balancer
        handler.setDocumentSource(htmlScanner);
        htmlScanner.setDocumentHandler(handler);
        
        htmlScanner.reset(config);

        XMLInputSource inputSource = new XMLInputSource(null, null, null);
        inputSource.setEncoding("ISO-8859-1");
        inputSource.setCharacterStream(new StringReader(source));
        htmlScanner.setInputSource(inputSource);
        htmlScanner.scanDocument(true);
        return handler;
    }

    private DocumentHandler newDocumentHandler(String source) {
        return new DocumentHandler(source);
    }

    private HTMLConfiguration newConfiguration() {
        HTMLConfiguration config = new HTMLConfiguration();
        // Maintain original case for elements and attributes
        config.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
        config.setProperty("http://cyberneko.org/html/properties/names/attrs", "no-change");
        // Get notified of entity and character references
        config.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
        config.setFeature("http://cyberneko.org/html/features/scanner/notify-builtin-refs", true);
        config.setFeature("http://xml.org/sax/features/namespaces", true);
        return config;
    }

    /** Handler for XNI events from Neko */
    private class DocumentHandler implements XMLDocumentHandler {

        private final Stack<Node> elementStack = new Stack<Node>();

        private final StringBuilder builder;

        private boolean inEntity;

        private DocumentFragment documentFragment;

        private Document document;

        public DocumentHandler(String content) {
            builder = new StringBuilder(content.length() / 10);
        }

        public DocumentFragment getFragment() {
            return documentFragment;
        }

        public Document getDocument() {
            return document;
        }

        public void startDocument(XMLLocator xmlLocator, String encoding, NamespaceContext namespaceContext, Augmentations augs)
                throws XNIException {
            document = documentFactory.createDocument(null, null, null);
            elementStack.clear();
            documentFragment = document.createDocumentFragment();
            elementStack.push(documentFragment);
        }

        public void xmlDecl(String version, String encoding, String standalone, Augmentations augs) throws XNIException {
            // Dont really do anything with this
            builder.append("<?xml");
            if (version != null) {
                builder.append(" version=\"").append(version).append('\"');
            }
            if (encoding != null) {
                builder.append(" encoding=\"").append(encoding).append('\"');
            }
            if (standalone != null) {
                builder.append(" standalone=\"").append(standalone).append('\"');
            }
            builder.append('>');
        }

        public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs) throws XNIException {
            document = documentFactory.createDocument(null, null, documentFactory.createDocumentType(rootElement, publicId,
                    systemId));
            elementStack.clear();
            documentFragment = document.createDocumentFragment();
            elementStack.push(documentFragment);
        }

        public void comment(XMLString text, Augmentations augs) throws XNIException {
            flushTextBuffer();

            // Add comments as comment nodes - needed to support sanitization
            // of SocialMarkup-parsed content
            Node comment = getDocument().createComment(new String(text.ch, text.offset, text.length));
            appendChild(comment);
        }

        public void processingInstruction(String s, XMLString xmlString, Augmentations augs) throws XNIException {
            // No-op
        }

        public void startElement(QName qName, XMLAttributes xmlAttributes, Augmentations augs) throws XNIException {
            Element element = startElementImpl(qName, xmlAttributes);
            // Not an empty element, so push on the stack
            elementStack.push(element);
        }

        public void emptyElement(QName qName, XMLAttributes xmlAttributes, Augmentations augs) throws XNIException {
            startElementImpl(qName, xmlAttributes);
        }

        /**
         * Flush any existing text content to the document. Call this before
         * appending any nodes.
         */
        protected void flushTextBuffer() {
            if (builder.length() > 0) {
                appendChild(document.createTextNode(builder.toString()));
                builder.setLength(0);
            }
        }

        /** Create an Element in the DOM */
        private Element startElementImpl(QName qName, XMLAttributes xmlAttributes) {
            flushTextBuffer();

            Element element;
            // Preserve XML namespace if present
            if (qName.uri != null) {
                element = document.createElementNS(qName.uri, qName.rawname);
            } else {
                element = document.createElement(qName.rawname);
            }

            for (int i = 0; i < xmlAttributes.getLength(); i++) {
                if (xmlAttributes.getURI(i) != null) {
                    element.setAttributeNS(xmlAttributes.getURI(i), xmlAttributes.getQName(i), xmlAttributes.getValue(i));
                } else {
                    try {
                        element.setAttribute(xmlAttributes.getLocalName(i), xmlAttributes.getValue(i));
                    } catch (DOMException e) {
                        switch (e.code) {
                            case DOMException.INVALID_CHARACTER_ERR:
                                StringBuilder sb = new StringBuilder(e.getMessage());
                                sb.append("Around ...<");
                                if (qName.prefix != null) {
                                    sb.append(qName.prefix);
                                    sb.append(':');
                                }
                                sb.append(qName.localpart);
                                for (int j = 0; j < xmlAttributes.getLength(); j++) {
                                    if (StringUtils.isNotBlank(xmlAttributes.getLocalName(j)) &&
                                            StringUtils.isNotBlank(xmlAttributes.getValue(j))) {
                                        sb.append(' ');
                                        sb.append(xmlAttributes.getLocalName(j));
                                        sb.append("=\"");
                                        sb.append(xmlAttributes.getValue(j)).append('\"');
                                    }
                                }
                                sb.append("...");
                                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, sb.toString());
                            default:
                                throw e;
                        }
                    }
                }
            }
            appendChild(element);
            return element;
        }

        public void startGeneralEntity(String entityName, XMLResourceIdentifier id, String encoding, Augmentations augs)
                throws XNIException {
            String name = entityName;
            if (name.startsWith("#")) {
                try {
                    boolean hex = name.startsWith("#x");
                    int offset = hex ? 2 : 1;
                    int base = hex ? 16 : 10;
                    int value = Integer.parseInt(name.substring(offset), base);
                    String entity = HTMLEntities.get(value);
                    if (entity != null) {
                        name = entity;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                    LOGGER.debug("Number format exception getting HTML entity", e);
                }
            }
            printEntity(name);
            inEntity = true;
        }

        private void printEntity(String name) {
            builder.append('&');
            builder.append(name);
            builder.append(';');
        }

        public void textDecl(String s, String s1, Augmentations augs) throws XNIException {
            builder.append(s);
        }

        public void endGeneralEntity(String s, Augmentations augs) throws XNIException {
            inEntity = false;
        }

        public void characters(XMLString text, Augmentations augs) throws XNIException {
            if (inEntity) {
                return;
            }
            builder.append(text.ch, text.offset, text.length);
        }

        public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
            builder.append(text.ch, text.offset, text.length);
        }

        public void endElement(QName qName, Augmentations augs) throws XNIException {
            flushTextBuffer();
            
            if (!elementStack.isEmpty()) {
                elementStack.pop();
            }
        }

        public void startCDATA(Augmentations augs) throws XNIException {
            // No-op
        }

        public void endCDATA(Augmentations augs) throws XNIException {
            // No-op
        }

        public void endDocument(Augmentations augs) throws XNIException {
            flushTextBuffer();
            
            if (!elementStack.isEmpty()) {
                elementStack.pop();
            }
        }

        public void setDocumentSource(XMLDocumentSource xmlDocumentSource) {
        }

        public XMLDocumentSource getDocumentSource() {
            return null;
        }

        private void appendChild(Node node) {
            elementStack.peek().appendChild(node);
        }
    }

}
