package uk.ac.warwick.util.content.texttransformers;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.content.MutableContent;

public class CssUrlRewriteTransformerTest {
	@Test public void rewriting() throws Exception {
		CssUrlRewriteTransformer transformer = new CssUrlRewriteTransformer("http://www2.warwick.ac.uk/services/its/");
		InputStream resource = getClass().getClassLoader().getResourceAsStream("style.css");
		assertNotNull("file not foond", resource);
		String content = FileCopyUtils.copyToString(new InputStreamReader(resource , Charset.defaultCharset()));
		String result = transformer.apply(new MutableContent(null, content)).getContent();
		System.out.println(result);
		
		assertTrue("unquoted", result.contains("url(http://www2.warwick.ac.uk/services/fab.gif)"));
		assertTrue("quoted",   result.contains("url(http://www2.warwick.ac.uk/services/its/ab/doozy.png)"));
		assertTrue("absolute", result.contains("background-image: url(http://www.example.com/fensler.jpg);"));
	}
}
