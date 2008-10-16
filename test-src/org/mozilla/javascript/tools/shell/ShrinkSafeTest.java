package org.mozilla.javascript.tools.shell;

import junit.framework.TestCase;

/**
 * Test that the ShrinkSafe filter works, and also highlight
 * some of its interesting behaviours.
 */
public class ShrinkSafeTest extends TestCase {
	
	private ShrinkSafe shrinkSafe;
	
	public void setUp() {
		shrinkSafe = new ShrinkSafe();
	}
	
	public void testCompress() throws Exception {
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
	
	/**
	 * If you reference a property called float it looks
	 * like the compressor doesn't like it. The output is
	 * uncompressed.
	 * 
	 * The workaround is to refer to the property as a hash key,
	 * as we do in the second part of this test.
	 */
	public void testFloatPropertyFails() throws Exception {
		String input = "//Hello\nelement.style.float = 'left';\nalert('bye');";
		assertEquals(input, shrinkSafe.compress(input));
		
		String input2 = input.replaceAll("\\.float", "['float']");
		String expected = "element.style[\"float\"]=\"left\";\nalert(\"bye\");\n";
		assertEquals(expected, shrinkSafe.compress(input2));
	}
	
	/**
	 * Single quotes turn into double quotes. But don't worry, escaping
	 * is handled properly even if you had double quotes in your string.
	 * I should think so too.
	 */
	public void testQuotes() throws Exception {
		String input = "var greeting = 'Welcome to \"Didgeredoo World\"!';\n";
		String expected = "var greeting=\"Welcome to \\\"Didgeredoo World\\\"!\";\n";
		assertEquals(expected, shrinkSafe.compress(input));
	}
	
	/**
	 * Variables that Rhino deems internal to the function are
	 * renamed to shorthand.
	 * 
	 * ambiguousVar is not declared with "var", which means it
	 * will be declared in whatever scope it exists in or globally
	 * otherwise. Thus, ShrinkSafe is correct not to rename it.
	 * Code writers should use var when they define internal vars.
	 */
	public void testInternalVariables() throws Exception {
		String input = "function go(a,b,c) {\n" +
				"  var internalVar = a + b + c;\n" +
				"  ambiguousVar = 'Am I global or what?';\n" +
				"  return internalVar;" +
				"}";
		//_4 is chosen because it's the fourth variable encountered
		String expected = "function go(a,b,c){\n"+
					"var _4=a+b+c;\n"+
					"ambiguousVar=\"Am I global or what?\";\n"+
					"return _4;\n"+
					"};\n";
		assertEquals(expected, shrinkSafe.compress(input));
	}
}
