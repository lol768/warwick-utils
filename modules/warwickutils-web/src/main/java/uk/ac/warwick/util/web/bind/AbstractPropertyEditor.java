package uk.ac.warwick.util.web.bind;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

public abstract class AbstractPropertyEditor<T> extends PropertyEditorSupport {
    
    private boolean allowEmpty;
    
    private boolean allowNotFound;
    
    public AbstractPropertyEditor() {
        this(true, false);
    }
    
    public AbstractPropertyEditor(boolean isAllowEmpty, boolean isAllowNotFound) {
        this.allowEmpty = isAllowEmpty;
        this.allowNotFound = isAllowNotFound;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final String getAsText() {
        T object = (T) getValue();
        if (object == null) {
            return null;
        }

        return toString(object);
    }

    @Override
    public final void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text)) {
            if (allowEmpty) {
                super.setValue(null);
            } else {
                throw new IllegalArgumentException();
            }
            
            return;
        }
        
        T object = fromString(text);
        if (object != null) {
            super.setValue(object);
        } else if (allowNotFound) {
            super.setValue(null);
        } else {
            throw new IllegalArgumentException("Could not find for " + text);
        }
    }
    
    public abstract String toString(T object);
    
    public abstract T fromString(String id);

    public final boolean isAllowEmpty() {
        return allowEmpty;
    }

    public final boolean isAllowNotFound() {
        return allowNotFound;
    }
    
    public final AbstractPropertyEditor<T> allowEmpty() {
        this.allowEmpty = true;
        return this;
    }
    
    public final AbstractPropertyEditor<T> nonEmpty() {
        this.allowEmpty = false;
        return this;
    }
    
    public final AbstractPropertyEditor<T> allowNotFound() {
        this.allowNotFound = true;
        return this;
    }
    
    public final AbstractPropertyEditor<T> mustExist() {
        this.allowNotFound = false;
        return this;
    }

}
