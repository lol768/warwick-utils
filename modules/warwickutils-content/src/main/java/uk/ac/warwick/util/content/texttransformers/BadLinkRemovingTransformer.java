package uk.ac.warwick.util.content.texttransformers;

import org.jsoup.nodes.Element;
import uk.ac.warwick.util.content.MutableContent;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Remove any links that have protocols other than we allow
 * 
 * @author Mat Mannion
 */
public class BadLinkRemovingTransformer implements TextTransformer {
	
	private static String[] BANNED_HREF_PROTOCOLS = {"javascript"};
	
	private static String[] BANNED_SRC_PROTOCOLS = {"javascript"};



    public MutableContent apply(MutableContent mc) {
    	boolean changed;
		changed = sanitiseAttributes(mc, "href", BANNED_HREF_PROTOCOLS);
		changed |= sanitiseAttributes(mc, "src", BANNED_SRC_PROTOCOLS);
		if (changed) {
			mc.documentChanged();
		}
		return mc;
    }

	private boolean sanitiseAttributes(MutableContent mc, String attributeToCheck, String[] bannedSchemes) {
    	boolean changed = false;
		for (Element el : mc.getDocument().select("[" + attributeToCheck + "]")) {
			boolean allowAttribute;
			boolean parseError = false;
			try {
				String attrValue = el.attr(attributeToCheck);
				URI uri = getValidUri(attrValue);

				allowAttribute = (Arrays.stream(bannedSchemes).noneMatch(a -> uri.getScheme() != null && uri.getScheme().equalsIgnoreCase(a)));
			} catch (URISyntaxException| UnsupportedEncodingException e) {
				allowAttribute = false;
				parseError = true;
			}

			if (!allowAttribute) {
				el.removeAttr(attributeToCheck);
				el.attr("data-error", !parseError ? "Stripped " + attributeToCheck + " due to banned scheme" : "Failed to parse URI");
				changed = true;
			}
		}
		return changed;
	}

	private URI getValidUri(String attrValue) throws URISyntaxException, UnsupportedEncodingException {
    	// this is a bit hacky.. since are different permitted characters in the path, query string and fragment.
		// but if we can't parse the string, we can't really tell what is the path, what is the query string or what is the fragment.

    	// try and fix the user's provided href - namely by encoding unsafe characters for them
		String[] alwaysUnsafe = new String[]{"^", "|", "\\", "\"", " ", "{", "}"};
		for (String replacement : alwaysUnsafe) {
			attrValue = attrValue.replace(replacement, URLEncoder.encode(replacement, StandardCharsets.UTF_8.name()));
		}

		attrValue = attrValue.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
		return new URI(attrValue);
	}
}
