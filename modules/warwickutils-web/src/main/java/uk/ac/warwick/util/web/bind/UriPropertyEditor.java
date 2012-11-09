package uk.ac.warwick.util.web.bind;

import uk.ac.warwick.util.web.Uri;

/**
 * @author Mat
 */
public final class UriPropertyEditor extends AbstractPropertyEditor<Uri> {

    @Override
    public Uri fromString(String url) {
        return Uri.parse(url.trim());
    }

    @Override
    public String toString(Uri uri) {
        return uri.toString();
    }

}
