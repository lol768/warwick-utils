package org.mozilla.javascript.tools.shell;

import junit.framework.TestCase;

public class ShrinkSafeTest extends TestCase {
	public void testCompress() throws Exception {
		ShrinkSafe shrinkSafe = new ShrinkSafe();
		String input = "/* this function is the bees knees.\n" +
				" It basically solves all problems, ever. */\n" +
				"function crab() {\n" +
				"  //apply danger\n" +
				"  alert('CRAB!');" +
				"  alert('snap snap')\n" +
				"}";
		String expected = "function crab(){\n" +
				"alert(\"CRAB!\");\n" +
				"alert(\"snap snap\");\n" +
				"};\n";
		
		String compressed = shrinkSafe.compress(input);
		
		assertEquals(expected,compressed);
	}
}
