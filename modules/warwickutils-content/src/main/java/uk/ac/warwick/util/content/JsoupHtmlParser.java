package uk.ac.warwick.util.content;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import uk.ac.warwick.util.core.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsoupHtmlParser implements HtmlParser {

    @Override
    public Document parseDOM(String source) throws HtmlParsingException {
        try (InputStream is = new ByteArrayInputStream(source.getBytes(StandardCharsets.ISO_8859_1))) {
            return Jsoup.parse(is, StringUtils.DEFAULT_ENCODING, "");
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
