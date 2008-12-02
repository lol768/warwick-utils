package uk.ac.warwick.util.ant;

import java.io.InputStreamReader;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import junit.framework.TestCase;

public class YUICompressorFilterTest extends TestCase {

	private YUICompressorFilter filter;
	
	public void setUp() {
		filter = new YUICompressorFilter();
	}
	
	/**
	 * Test that the filter basically does compress, and also checks
	 * that the variable name $super is maintained (a specific workaround
	 * for something that Prototype 1.6 does).
	 */
	public void testFilter() {
		String input = "/* my library */\n" +
				"var albatross = 'my albatross';" +
				"function z(ab,cd,efghi) {\n" +
				"  return ab + cd + efghi;\n" +
				"}\n" +
				"function zz($super, x) {\n" +
				"  x.call($super,arguments);\n" +
				"}\n";
		String expected = "var albatross=\"my albatross\";\n"+
			"function z(b,c,a){return b+c+a\n"+
			"}function zz($super,a){a.call($super,arguments)\n"+
			"};";
		String output = filter.filter(input);
		assertEquals(expected, output);
	}
	
	public void testPrototype() throws Exception {
		String prototypeFile = "prototype-1.6.0.3.js";
		String prototype = FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource(prototypeFile).getInputStream()));
		int originalLength = prototype.length();
		String compressed = filter.filter(prototype);
		int newLength = compressed.length();
		int percent = (newLength*100)/originalLength;
		assertTrue("Compression is not very good!", percent < 70);
		System.out.println(prototypeFile + " compressed to " + (percent + "%"));
	}

}
