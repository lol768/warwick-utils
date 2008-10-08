package uk.ac.warwick.util.ant;

import org.apache.tools.ant.filters.TokenFilter;
import org.mozilla.javascript.tools.shell.ShrinkSafe;

public class ShrinkSafeFilter implements TokenFilter.Filter {

	private ShrinkSafe shrinkSafe = new ShrinkSafe();
	
	public String filter(String text) {
		return shrinkSafe.compress(text);
	}

}
