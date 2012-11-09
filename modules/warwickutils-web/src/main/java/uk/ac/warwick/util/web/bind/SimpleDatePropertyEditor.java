package uk.ac.warwick.util.web.bind;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.warwick.util.core.StringUtils;

/**
 * Simple property editor to read a Date as a single string. Defaults
 * to the value of pattern but can be changed.
 * @author cusebr
 * 
 * @deprecated Use DateTimePropertyEditor
 */
public final class SimpleDatePropertyEditor extends PropertyEditorSupport {
    
    private String pattern = "dd/MM/yy";
    
    public SimpleDatePropertyEditor() {
        setValue(null);
    }
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        if (!StringUtils.hasText(text)) {
            setValue(null);
        } else {
            try {
                setValue(format.parse(text));
            } catch (ParseException e) {
                throw new IllegalArgumentException("Couldn't parse "+text+" as a date");
            }
        }
    }
    
    @Override
    public String getAsText() {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        if (getValue() == null) {
            return "";
        }
        return format.format((Date)getValue());
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
