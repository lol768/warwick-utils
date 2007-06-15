package uk.ac.warwick.util.web.tags;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Outputs the current year in roman numerals for use in copyright notices.
 * Roman Numeral conversion based on
 * http://www.hawaga.org.uk/java/benno/number/Roman.java
 * 
 * @author Mat Mannion
 */
public final class CopyrightTag extends TagSupport {

    private static final long serialVersionUID = 8819001538449884405L;

    private static final SymTab[] ROMAN_SYMBOLS = { new SymTab('M', 1000), new SymTab('D', 500), new SymTab('C', 100),
            new SymTab('L', 50), new SymTab('X', 10), new SymTab('V', 5), new SymTab('I', 1) };

    public int doStartTag() throws JspException {
        String copyrightDate = getRomanNumerals();
        try {
            pageContext.getOut().print(copyrightDate);
        } catch (IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }

    private String getRomanNumerals() {
        Calendar rightNow = Calendar.getInstance();
        int year = rightNow.get(Calendar.YEAR);

        return toRoman(year);
    }

    /**
     * This method converts a long integer to capitalised Roman notation. From
     * http://www.hawaga.org.uk/java/benno/number/Roman.java
     * 
     * @param n
     *            The integer to convert to Roman Numerals.
     * @return A String object containing the Roman Numerals.
     */

    private static String toRoman(final long input) {
        int i;
        long n = input;
        String s;
        s = "";
        while (n > 0) {
            for (i = 0; i < ROMAN_SYMBOLS.length; i++) {
                if (ROMAN_SYMBOLS[i].getValue() <= n) {
                    int shift = i + (i % 2);
                    if (i > 0 && shift < ROMAN_SYMBOLS.length
                            && (ROMAN_SYMBOLS[i - 1].getValue() - ROMAN_SYMBOLS[shift].getValue()) <= n) {
                        s = s + ROMAN_SYMBOLS[shift].getSymbol() + ROMAN_SYMBOLS[i - 1].getSymbol();
                        n = n - ROMAN_SYMBOLS[i - 1].getValue() + ROMAN_SYMBOLS[shift].getValue();

                        i = -1;

                    } else {
                        s += ROMAN_SYMBOLS[i].getSymbol();
                        n -= ROMAN_SYMBOLS[i].getValue();
                        i = -1;
                    }
                }
            }
        }
        return s;
    }

    public static class SymTab {
        /** Roman symbol */
        private char symbol;

        /** Numerical value */
        private long value;

        /**
         * Constructor to build a SymTab from supplied symbol and value
         * 
         * @param s
         *            Roman symbol
         * @param v
         *            Numerical value
         */
        public SymTab(final char s, final long v) {
            this.symbol = s;
            this.value = v;
        }

        public final char getSymbol() {
            return symbol;
        }

        public final long getValue() {
            return value;
        }
    };

}
