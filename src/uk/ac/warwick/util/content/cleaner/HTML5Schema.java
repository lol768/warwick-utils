package uk.ac.warwick.util.content.cleaner;

import org.ccil.cowan.tagsoup.HTMLSchema;

/**
 * By default, TagSoup's {@link HTMLSchema} doesn't support HTML5 elements and
 * this can throw tags like <code>&lt;video&gt;</code>,
 * <code>&lt;audio&gt;</code> and <code>&lt;source&gt;</code> out of kilter.
 * <p>
 * This doesn't claim or attempt to be a full specification, but we can attempt
 * to support tags as we come across them until TagSoup release a new TSSL file
 * or TagSoup >1.2 is released.
 */
public final class HTML5Schema extends HTMLSchema {
    
    public HTML5Schema() {
        super(); // Populate HTML 4.01 Transitional elements/attributes and all HTML entities
        
        /* <video> */
        elementType(
            "video",  
            M_PCDATA|M_PARAM|M_INLINE|M_BLOCK, /* Can contain PCDATA, <param>s, or other <inline> or <block> elements (fallbacks) */ 
            M_INLINE|M_NOLINK, /* Can be in INLINE elements but not in a link */
            0);
        
        // The natural parent of <video> is the <body>
        parent("video", "body");
        
        // Standard attributes
        standardAttributes("video");
        
        // Attributes - autoplay (boolean), controls (boolean), height (int), loop (boolean), preload (boolean), src (url), width (int)
        // We only have to define the boolean ones
        attribute("video", "autoplay", "BOOLEAN", null);
        attribute("video", "controls", "BOOLEAN", null);
        attribute("video", "loop", "BOOLEAN", null);
        attribute("video", "preload", "BOOLEAN", null);
        /* </video> */
        
        /* <audio> */
        elementType(
                "audio",  
                M_PCDATA|M_PARAM|M_INLINE|M_BLOCK, /* Can contain PCDATA, <param>s, or other <inline> or <block> elements (fallbacks) */ 
                M_INLINE|M_NOLINK, /* Can be in INLINE elements but not in a link */
                0);
        
        parent("audio", "body");
        standardAttributes("audio");
        /* </audio> */
        
        /* <source> */
        elementType(
                "source",
                M_PCDATA|M_INLINE|M_BLOCK, 
                M_INLINE|M_NOLINK, 
                0);
        
        // What should the parent for <source> be? Ugh. Just call it video for now
        parent("source", "video");
        /* </source> */
    }
    
    private void standardAttributes(String tagName) {
        attribute(tagName, "class", "NMTOKEN", null);
        attribute(tagName, "dir", "NMTOKEN", null);
        attribute(tagName, "id", "ID", null);
        attribute(tagName, "lang", "NMTOKEN", null);
    }

}
