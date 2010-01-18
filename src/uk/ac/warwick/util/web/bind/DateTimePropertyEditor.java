package uk.ac.warwick.util.web.bind;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.LenientChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Property editor for JodaTime's {@link DateTime}s, using one or more custom
 * date formats.
 * <p>
 * allowEmpty will default to <code>true</code> if not specified.
 * 
 * @author Mat
 */
public final class DateTimePropertyEditor extends AbstractPropertyEditor<DateTime> {

    private final DateTimeFormatter[] dateFormats;
    
    public DateTimePropertyEditor(String pattern) {
        this(pattern, true);
    }
    
    public DateTimePropertyEditor(String pattern, boolean isAllowEmpty) {
        this(pattern, isAllowEmpty, false);
    }
    
    public DateTimePropertyEditor(String pattern, boolean isAllowEmpty, boolean isLenient) {
    	this(new String[] { pattern }, isAllowEmpty, isLenient);
    }
    
    public DateTimePropertyEditor(String[] patterns) {
        this(patterns, true);
    }
    
    public DateTimePropertyEditor(String[] patterns, boolean isAllowEmpty) {
        this(patterns, isAllowEmpty, false);
    }

    public DateTimePropertyEditor(String[] patterns, boolean isAllowEmpty, boolean isLenient) {
        super(isAllowEmpty, false);
        
        if (patterns.length == 0) {
            throw new IllegalArgumentException("At least one pattern must be specified");
        }
        
        this.dateFormats = new DateTimeFormatter[patterns.length];
        for (int i=0;i<patterns.length;i++) {
        	if (isLenient) {
        		dateFormats[i] = DateTimeFormat.forPattern(patterns[i])
        			.withChronology(LenientChronology.getInstance(ISOChronology.getInstance()));
        	} else {
        		dateFormats[i] = DateTimeFormat.forPattern(patterns[i]);
        	}
        }
    }

    /**
     * Parse the DateTime from the given text, using the specified DateFormatters.
     */
    public DateTime fromString(String text) {
        DateTime parsed = null;
        for (DateTimeFormatter format : this.dateFormats) {
            try {
                parsed = format.parseDateTime(text);
                break;
            } catch (IllegalArgumentException e) {  
                // ignore until we have tried all formats
                parsed = null;
            }
        }
        
        return parsed;
    }

    /**
     * Format the Date as String, using the first DateTimeFormatter.
     */
    public String toString(DateTime value) {
        return this.dateFormats[0].print(value);
    }

}
