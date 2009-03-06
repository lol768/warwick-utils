package uk.ac.warwick.util.ant;

import java.io.ByteArrayInputStream;

import net.sf.packtag.implementation.JsminPackStrategy;
import net.sf.packtag.strategy.PackException;

import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.TokenFilter;

/**
 * A filter that can be used as part of a TokenFilter to run a JSMin pack on a
 * javascript file.
 * 
 * @author Mat Mannion
 */
public final class JSMinFilter implements TokenFilter.Filter {
	
	private static final Logger LOGGER = Logger.getLogger(JSMinFilter.class);

    public String filter(final String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        String result;
        try {
            result = new JsminPackStrategy().pack(new ByteArrayInputStream(input.getBytes()));
        } catch (PackException e) {
        	LOGGER.error("Could not pack", e);
            result = input;
        }

        return result;
    }

}
