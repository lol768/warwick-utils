package uk.ac.warwick.util.content;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import com.google.common.collect.Lists;

public final class DocumentUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentUtils.class);
    
    private static final List<String> HIDDEN_TAGS = Arrays.asList("script","style","input","textarea","select");
    
    private static DOMImplementation documentFactory;

    private DocumentUtils() {
    }

    public static List<Element> getElementsByTagName(final Node parent, final String tagName) {
        List<Element> results = Lists.newArrayList();

        getElementsByTagName(parent, tagName, results);

        return results;
    }

    private static void getElementsByTagName(final Node parent, final String tagName, final List<Element> list) {
        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                list.add((Element)node);
            } else {
                getElementsByTagName(node, tagName, list);
            }
        }
    }
    
    public static Node getElementById(final Node parent, final String id) {
        NodeList children = parent.getChildNodes();
        
        Node result = null;
        
        int i = 0;
        while (i < children.getLength() && result == null) {
            Node node = children.item(i);            
            if (node instanceof Element) {
                String thisId = getAttribute(node, "id");
                if (thisId != null && thisId.equals(id)) {
                    return node;
                } else {
                    result = getElementById(node, id);
                }
            }
            
            i++;
        }
        
        return result;
    }
    
    public static Node getFirstOccurrenceOfElementByTagName(final Node parent, final String tagName) {
        NodeList children = parent.getChildNodes();
        Node result = null;
        
        for (int i=0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                result = node;
                break;
            } else {
                Node thisResult = getFirstOccurrenceOfElementByTagName(node, tagName);
                if (thisResult != null) {
                    result = thisResult;
                    break;
                }
            }
        }
        
        return result;
    }
    
    public static Node getFirstNamedChildNode(Node root, String nodeName) {
        Node current = root.getFirstChild();
        while (current != null) {
            if (current.getNodeName().equalsIgnoreCase(nodeName)) {
                return current;
            }
            current = current.getNextSibling();
        }
        return null;
    }
    
    public static Node getBody(final Document document) {
        Node body = DocumentUtils.getFirstOccurrenceOfElementByTagName(document, "body");

        // if we can't find a body, use the document
        if (body == null) {
            body = document;
        }
        
        return body;
    }

    public static String getAttribute(final Node node, final String attributeName) {
        return node.getAttributes().getNamedItem(attributeName) == null ? null : node.getAttributes().getNamedItem(attributeName)
                .getNodeValue();
    }

    /**
     * Get all text contents of this node. May require trimming
     */
    public static String getContents(final Node node) {
        if (node instanceof Text) {
            return node.getNodeValue() != null ? " " + node.getNodeValue() : "";
        } else {
            StringBuilder result = new StringBuilder();
            if (node.getChildNodes() != null) {
                for (int i=0; i < node.getChildNodes().getLength(); i++) {
                    Node child = node.getChildNodes().item(i);
                    if (!HIDDEN_TAGS.contains(child.getNodeName().toLowerCase())) {
                        result.append(getContents(child));
                    }
                }
            }
            
            return result.toString();
        }
    }
    
    public static String getInnerHTML(final Node node) {
        StringBuilder sb = new StringBuilder();
        
        NodeList children = node.getChildNodes();
        
        for (int i=0; i < children.getLength(); i++) {
            sb.append(getHtml(children.item(i)));
        }
        
        return sb.toString();
    }
    
    private static String getHtml(final Node node) {
        StringBuilder sb = new StringBuilder();
        if (node instanceof Element) {
            sb.append("<");
            sb.append(node.getNodeName());
            
            NamedNodeMap attributes = node.getAttributes();
            for (int i=0; i < attributes.getLength(); i++) {
                sb.append(" " + attributes.item(i).getNodeName() + "=\"" + attributes.item(i).getNodeValue() + "\"");
            }
            sb.append(">");
            
            NodeList children = node.getChildNodes();
            
            for (int i=0; i < children.getLength(); i++) {
                sb.append(getHtml(children.item(i)));
            }
            
            sb.append("</" + node.getNodeName() + ">");
        } else if (node instanceof Text) {
            sb.append(node.getNodeValue());
        }
        
        return sb.toString();
    }

    public static Node getNextNonTextSibling(final Node node) {
        if (node.getNextSibling() == null || !(node.getNextSibling() instanceof Text)) {
            return node.getNextSibling();
        } else {
            return getNextNonTextSibling(node.getNextSibling());
        }
    }
    
    public static synchronized void setDOMImplementation(DOMImplementation implementation) {
        documentFactory = implementation;
    }

    public static synchronized DOMImplementation getDOMImplementation() {
        if (documentFactory != null) {
            return documentFactory;
        }
        
        DOMImplementation df = null;
        
        try {
            df = fromRegistry();
        } catch (Exception e1) {
            // Couldn't do it
            LOGGER.debug("Couldn't find DOMImplementation in DOMImplementationRegistry");
        }
        
        if (df == null) {
            try {
                df = fromXerces();
            } catch (Exception e2) {
                // More badness has happened
                LOGGER.debug("No valid xerces implementation");
            }
        }
        
        if (df == null) {
            try {
                documentFactory = fromInternalXerces();
                return documentFactory;
            } catch (Exception e3) {
                throw new IllegalStateException("Couldn't find a DOMImplementation", e3);
            }
        }
        
        documentFactory = df;
        return df;
    }
    
    private static DOMImplementation fromRegistry() throws Exception {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        
        // Require the traversal API
        return registry.getDOMImplementation("XML 1.0 Traversal 2.0");
    }
    
    private static DOMImplementation fromXerces() throws Exception {
        return (DOMImplementation) Class.forName("org.apache.xerces.dom.DOMImplementationImpl").getMethod(
            "getDOMImplementation").invoke(null);
    }
    
    private static DOMImplementation fromInternalXerces() throws Exception {
        return (DOMImplementation) Class.forName("com.sun.org.apache.xerces.internal.dom.DOMImplementationImpl")
            .getMethod("getDOMImplementation").invoke(null);
    }

}
