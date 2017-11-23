package uk.ac.warwick.util.web.bind;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Property editor for JSR310's {@link LocalDateTime}s, using one or more custom
 * date formats.
 * <p>
 * allowEmpty will default to <code>true</code> if not specified.
 * 
 * @author Mat
 */
public final class LocalDateTimePropertyEditor extends AbstractPropertyEditor<LocalDateTime> {

    private final DateTimeFormatter[] dateFormats;
    
    public LocalDateTimePropertyEditor(String pattern) {
        this(pattern, true);
    }
    
    public LocalDateTimePropertyEditor(String pattern, boolean isAllowEmpty) {
        this(pattern, isAllowEmpty, false);
    }
    
    public LocalDateTimePropertyEditor(String pattern, boolean isAllowEmpty, boolean isLenient) {
    	this(new String[] { pattern }, isAllowEmpty, isLenient);
    }
    
    public LocalDateTimePropertyEditor(String[] patterns) {
        this(patterns, true);
    }
    
    public LocalDateTimePropertyEditor(String[] patterns, boolean isAllowEmpty) {
        this(patterns, isAllowEmpty, false);
    }

    public LocalDateTimePropertyEditor(String[] patterns, boolean isAllowEmpty, boolean isLenient) {
        super(isAllowEmpty, false);
        
        if (patterns.length == 0) {
            throw new IllegalArgumentException("At least one pattern must be specified");
        }
        
        this.dateFormats = new DateTimeFormatter[patterns.length];
        for (int i=0;i<patterns.length;i++) {
        	if (isLenient) {
        		dateFormats[i] = DateTimeFormatter.ofPattern(patterns[i])
                    .withResolverStyle(ResolverStyle.LENIENT);
        	} else {
        		dateFormats[i] = DateTimeFormatter.ofPattern(patterns[i]);
        	}
        }
    }

    /**
     * Parse the DateTime from the given text, using the specified DateFormatters.
     */
    public LocalDateTime fromString(String text) {
        LocalDateTime parsed = null;
        for (DateTimeFormatter format : this.dateFormats) {
            try {
                parsed = LocalDateTime.parse(text, format);
                break;
            } catch (DateTimeParseException e) {
                // ignore until we have tried all formats
                parsed = null;
            }
        }
        
        return parsed;
    }

    /**
     * Format the Date as String, using the first DateTimeFormatter.
     */
    public String toString(LocalDateTime value) {
        return this.dateFormats[0].format(value);
    }

}
