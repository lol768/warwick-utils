package uk.ac.warwick.util.web.bind;

import java.beans.PropertyEditorSupport;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.util.StringUtils;

public final class UrlPropertyEditor extends PropertyEditorSupport {

    public String getAsText() {
        URL url = (URL) getValue();
        if (url == null ) {
            return null;
        }
        return url.toString();
    }

    public void setAsText(final String value) throws IllegalArgumentException {
        if (!StringUtils.hasLength(value)) {
            setValue(null);
            return;
        }

        try {
            URL url = new URL(value);
            super.setValue(url);
        } catch (final MalformedURLException e) {
            super.setValue(null);
            throw new IllegalArgumentException("Cannot convert " + value + " to URL", e);
        }
    }
}
