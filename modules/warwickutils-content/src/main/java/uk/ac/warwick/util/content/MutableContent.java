/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package uk.ac.warwick.util.content;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.util.core.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

/**
 * Object that maintains a String representation of arbitrary contents and a
 * consistent view of those contents as an HTML parse tree.
 */
public class MutableContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(MutableContent.class);

    // String representation of contentBytes taking into account the correct
    // encoding of the content.
    private String content;

    private byte[] contentBytes;

    // Encoding of the content bytes. UTF-8 by default.
    private Charset contentEncoding;

    private Document document;

    private int numChanges = 0;

    private final HtmlParser contentParser;

    private Map<String, Object> pipelinedData;

    /**
     * Construct with decoded string content
     */
    public MutableContent(HtmlParser contentParser, String content) {
        this(contentParser, content, StringUtils.DEFAULT_CHARSET);
    }
    
    public MutableContent(HtmlParser contentParser, String content, Charset contentEncoding) {
        this.contentParser = contentParser;
        this.content = content;
        this.contentEncoding = contentEncoding;
    }

    /**
     * Retrieves the current content for this object in String form. If content
     * has been retrieved in parse tree form and has been edited, the String
     * form is computed from the parse tree by rendering it. It is
     * <b>strongly</b> encouraged to avoid switching between retrieval of parse
     * tree (through {@code getParseTree}), with subsequent edits and retrieval
     * of String contents to avoid repeated serialization and deserialization.
     * As a final fallback, if content has been set as bytes, interprets them as
     * a UTF8 String.
     * 
     * @return Renderable/active content.
     */
    public String getContent() {
        if (document != null) {
            content = new DefaultHtmlSerializer().serialize(document);
            return content;
        }
        if (content == null) {
            if (contentBytes != null) {
                Charset useEncoding = contentEncoding != null ? contentEncoding : Charsets.UTF_8;
                content = useEncoding.decode(ByteBuffer.wrap(contentBytes)).toString();
            }
        }
        return content;
    }

    /**
     * Sets the object's content as a raw String. Note, this operation may clear
     * the document if the content has changed
     * 
     * @param newContent
     *            New content.
     */
    public void setContent(String newContent) {
        // TODO - Equality check may be unnecessary overhead
        if (content == null || !content.equals(newContent)) {
            content = newContent;
            document = null;
            contentBytes = null;
            incrementNumChanges();
        }
    }

    /**
     * Retrieves the current content for this object as an InputStream.
     * 
     * @return Active content as InputStream.
     */
    public InputStream getContentBytes() {
        return new ByteArrayInputStream(getRawContentBytes());
    }

    protected byte[] getRawContentBytes() {
        if (contentBytes == null) {
            if (content != null) {
                setContentBytesState(getBytes(content), contentEncoding);
            } else if (document != null) {
                // TODO settings
                setContentBytesState(getBytes(new DefaultHtmlSerializer().serialize(document)), contentEncoding);
            }
        }
        return contentBytes;
    }
    
    /**
     * @return UTF-8 byte array for the input string.
     */
    public byte[] getBytes(String s) {
        if (s == null) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        ByteBuffer bb = contentEncoding.encode(s);
        return ArrayUtils.subarray(bb.array(), 0, bb.limit());
    }

    /**
     * Sets the object's contentBytes as the given raw input. If ever
     * interpreted as a String, the data will be decoded as the encoding
     * specified. Note, this operation may clear the document if the content has
     * changed. Also note, it's mandated that the new bytes array will NOT be
     * modified by the caller of this API. The array is not copied, for
     * performance reasons. If the caller may modify a byte array, it MUST pass
     * in a new copy.
     * 
     * @param newBytes
     *            New content.
     */
    public void setContentBytes(byte[] newBytes, Charset newEncoding) {
        if (contentBytes == null || !Arrays.equals(contentBytes, newBytes)) {
            setContentBytesState(newBytes, newEncoding);
            document = null;
            content = null;
            incrementNumChanges();
        }
    }

    /**
     * Sets content to new byte array, with unspecified charset. It is
     * recommended to use the {@code setContentBytes(byte[], Charset)} API
     * instead, where possible.
     * 
     * @param newBytes
     *            New content.
     */
    public final void setContentBytes(byte[] newBytes) {
        setContentBytes(newBytes, null);
    }

    /**
     * Sets internal state having to do with content bytes, from the provided
     * byte array and charset. This MUST be the only place in which
     * MutableContent's notion of encoding is mutated.
     * 
     * @param newBytes
     *            New content.
     * @param newEncoding
     *            Encoding for the bytes, or null for unspecified.
     */
    protected void setContentBytesState(byte[] newBytes, Charset newEncoding) {
        contentBytes = newBytes;
        contentEncoding = newEncoding;
    }

    /**
     * Notification that the content of the document has changed. Causes the
     * content string and bytes to be cleared.
     */
    public void documentChanged() {
        if (document != null) {
            content = null;
            contentBytes = null;
            incrementNumChanges();
        }
    }

    /**
     * Retrieves the object contents in parsed form, if a
     * {@code GadgetHtmlParser} is configured and is able to parse the string
     * contents appropriately. To modify the object's contents by parse tree
     * after setting new String contents, this method must be called again.
     * However, this practice is highly discouraged, as parsing a tree from
     * String is a costly operation and should be done at most once per rewrite.
     */
    public Document getDocument() {
        // TODO - Consider actually imposing one parse limit on rewriter
        // pipeline
        if (document != null) {
            return document;
        }
        try {
            document = contentParser.parseDOM(getContent());
        } catch (HtmlParsingException e) {
            LOGGER.warn(e.getMessage(), e);
            
            return null;
        }
        return document;
    }

    public int getNumChanges() {
        return numChanges;
    }

    protected void incrementNumChanges() {
        ++numChanges;
    }

    /**
     * True if current state has a parsed document. Allows rewriters to switch
     * mode based on which content is most readily available
     */
    public boolean hasDocument() {
        return (document != null);
    }

    public void addPipelinedData(String key, Object value) {
        if (null == pipelinedData) {
            pipelinedData = Maps.newHashMap();
        }
        pipelinedData.put(key, value);
    }

    public Map<String, Object> getPipelinedData() {
        return (null == pipelinedData) ? ImmutableMap.<String, Object>of() : pipelinedData;
    }
}
