package uk.ac.warwick.util.content.texttransformers;

import org.jsoup.nodes.Element;
import uk.ac.warwick.util.content.MutableContent;

import java.net.URI;
import java.net.URISyntaxException;
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
		sanitiseAttributes(mc, "href", BANNED_HREF_PROTOCOLS);
		sanitiseAttributes(mc, "src", BANNED_SRC_PROTOCOLS);

		return mc;
    }

	private void sanitiseAttributes(MutableContent mc, String attributeToCheck, String[] bannedSchemes) {
		for (Element el : mc.getDocument().select("[" + attributeToCheck + "]")) {
			boolean allowAttribute;
			boolean parseError = false;
			try {
				URI uri = new URI(el.attr(attributeToCheck));

				allowAttribute = (Arrays.stream(bannedSchemes).noneMatch(a -> uri.getScheme() != null && uri.getScheme().equalsIgnoreCase(a)));
			} catch (URISyntaxException e) {
				allowAttribute = false;
				parseError = true;
			}

			if (!allowAttribute) {
				el.removeAttr(attributeToCheck);
				el.attr("data-error", !parseError ? "Stripped " + attributeToCheck + " due to banned scheme" : "Failed to parse URI");
				mc.documentChanged();
			}
		}
	}
}
