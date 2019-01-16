package uk.ac.warwick.util.content.texttransformers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.content.JsoupHtmlParser;
import uk.ac.warwick.util.content.MutableContent;

public class BadLinkRemovingTransformerTest {

    private BadLinkRemovingTransformer transformer;

    @Before
    public void setUp() {
        transformer = new BadLinkRemovingTransformer();
    }

    @Test
    public void testSimpleJsLink() {
        String input = "<html><body><a href='javascript:alert(1)'></a></body></html>";
        String result = transformer.apply(new MutableContent(new JsoupHtmlParser(), input)).getContent();
        Assert.assertFalse(result.contains("javascript:alert"));
    }

    @Test
    public void testInvalidCaretLink() {
        String input = "<html><body><a href='https://google.com/foo^'></a></body></html>";
        String result = transformer.apply(new MutableContent(new JsoupHtmlParser(), input)).getContent();
        Assert.assertFalse(result.toLowerCase().contains("https://google.com/foo%5E"));
    }

    @Test
    public void testInvalidBackslashLink() {
        String input = "<html><body><a href='https://google.com/foo\\'></a></body></html>";
        String result = transformer.apply(new MutableContent(new JsoupHtmlParser(), input)).getContent();
        Assert.assertFalse(result.toLowerCase().contains("https://google.com/foo%5C"));
    }

    @Test
    public void testInvalidSpaceLink() {
        String input = "<html><body><a href='https://google.com/foo bar'></a></body></html>";
        String result = transformer.apply(new MutableContent(new JsoupHtmlParser(), input)).getContent();
        Assert.assertFalse(result.toLowerCase().contains("https://google.com/foo%20bar"));
    }

    @Test
    public void testInvalidPercentEncode() {
        String input = "<html><body><a href='https://google.com/foo%bar'></a></body></html>";
        String result = transformer.apply(new MutableContent(new JsoupHtmlParser(), input)).getContent();
        Assert.assertFalse(result.toLowerCase().contains("https://google.com/foo%25bar"));
    }

    @Test
    public void testWhitespaceJsLink() {
        String input = "<html><body><a href=' javascript:alert(1)'></a></body></html>";
        String result = transformer.apply(new MutableContent(new JsoupHtmlParser(), input)).getContent();
        Assert.assertFalse(result.contains("javascript:alert"));
    }

    @Test
    public void testHtmlEntityJsLink() {
        String input = "<html><body><a href=\"javascript&#x3A;alert(1)\">test one</a></body></html>";
        String result = transformer.apply(new MutableContent(new JsoupHtmlParser(), input)).getContent();
        Assert.assertFalse(result.contains("javascript:alert"));
    }

    @Test
    public void testHtmlEntityDoubleEncodedJsLink() {
        String input = "<html><body><a href=\"javascript&#x26;&#x23;&#x78;&#x33;&#x41;&#x3B;alert(1)\">test one</a></body></html>";
        String result = transformer.apply(new MutableContent(new JsoupHtmlParser(), input)).getContent();
        Assert.assertFalse(result.contains("javascript:alert"));
        Assert.assertFalse(result.contains("javascript&#x3A;alert"));
    }

    @Test
    public void testMixedCaseJavascriptUrl() {
        String input = "<html><body><a href=\"jAvAsCrIpT:alert(1)\">test one</a></body></html>";
        String result = transformer.apply(new MutableContent(new JsoupHtmlParser(), input)).getContent();
        Assert.assertFalse(result.toLowerCase().contains("javascript:alert"));
    }
}
