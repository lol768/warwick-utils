package uk.ac.warwick.util.ant;

import java.io.ByteArrayInputStream;

import net.sf.packtag.implementation.IBloomCssPackStrategy;
import net.sf.packtag.strategy.PackException;

import org.apache.tools.ant.filters.TokenFilter;

/**
 * A filter that can be used as part of a TokenFilter to run an IBloom pack on a
 * CSS file.
 * 
 * @author Mat Mannion
 */
public final class IBloomFilter implements TokenFilter.Filter {

    public String filter(final String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        String result;
        try {
            result = new IBloomCssPackStrategy().pack(new ByteArrayInputStream(input.getBytes()));
        } catch (PackException e) {
            result = input;
        }

        return result;
    }

}
