package uk.ac.warwick.util.ant;

import org.apache.tools.ant.filters.TokenFilter;
import org.mozilla.javascript.tools.shell.ShrinkSafe;

public class ShrinkSafeFilter implements TokenFilter.Filter {

	private ShrinkSafe shrinkSafe = new ShrinkSafe();
	
	public ShrinkSafeFilter() {
		// Interpreted mode avoids problems when you have
		// extremely large scripts or data structures. In
		// compiled mode, you can get errors about a method being
		// too big (max size for Java is ~64k). Interpreted
		// mode has no such restriction.
		shrinkSafe.setInterpreted(true);
	}
	
	public String filter(String text) {
		return shrinkSafe.compress(text);
	}

}
