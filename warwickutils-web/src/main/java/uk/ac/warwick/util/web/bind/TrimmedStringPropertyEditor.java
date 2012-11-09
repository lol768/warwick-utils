package uk.ac.warwick.util.web.bind;

import java.beans.PropertyEditorSupport;

public final class TrimmedStringPropertyEditor extends PropertyEditorSupport {

    public void setAsText(String text) {
        super.setValue(trim(text));
    }
    
    public String getAsText() {
        return trim(getValue());
    }
    
    private String trim (Object text) {
        if (text == null) {
            return "";
        }
        return text.toString().trim();
    }
 
}
