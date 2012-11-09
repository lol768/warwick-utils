package uk.ac.warwick.util.web.bind;

import java.beans.PropertyEditorSupport;

import uk.ac.warwick.util.core.StringUtils;

/**
 * Spring PropertyEditor which both
 * 
 * * Trims whitespace
 * * Converts any non-ASCII bytes to HTML entities. Browsers should mostly do this anyway
 *     but there are some annoying compatibility features that make them send some characters
 *     as Windows-1252 instead, so this is needed.
 * 
 * @author cusebr
 */
public final class TrimAndEscapeStringPropertyEditor extends PropertyEditorSupport {
    private TrimmedStringPropertyEditor trimmer;
    public TrimAndEscapeStringPropertyEditor() {
        trimmer = new TrimmedStringPropertyEditor();
        setValue("");
    }
    
    public void setValue(String value) {
        trimmer.setValue(value);
        super.setValue(StringUtils.htmlEscapeHighCharacters( trimmer.getAsText() ));
    }
    
    public String getValue() {
        trimmer.setValue(super.getValue());
        return escape(trimmer.getAsText());
    }
    
    public void setAsText(String text) {
        setValue(text);
    }
    
    public String getAsText() {
        return StringUtils.nullGuard("" + getValue());
    }
    
    private String escape(String text) {
        return StringUtils.htmlEscapeHighCharacters(text);
    }
}
