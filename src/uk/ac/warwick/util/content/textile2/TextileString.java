/*
 * Created on 11-Mar-2004
 *
 */
package uk.ac.warwick.util.content.textile2;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import uk.ac.warwick.util.content.cleaner.HtmlCleaner;
import uk.ac.warwick.util.content.textile2.lite.TextileLite;

/**
 * @author Kieran Shaw
 * 
 */
public class TextileString {

	private static final Logger LOGGER = Logger.getLogger(TextileString.class);

	private String textileText;

	private boolean stripAllHtmlBeforeConvert;

	private boolean stripAllHtmlAfterConvert;

	private String disallowTags;

	private String onlyAllowTags;

	private boolean lite;

	private boolean correctHtml;
	
	private boolean addNoFollow;
	
	private boolean allowJavascriptHandlers = true;
	
	private EnumSet<TransformerFeature> features = null;

	public TextileString(final String theTextileText) {
		this.textileText = theTextileText;
	}

	public final String getHtml() {
		if (isStripAllHtmlBeforeConvert()) {
			// doesn't this need to be multi-line?
			textileText = Pattern.compile("\\<\\S.*?\\>", Pattern.DOTALL)
					.matcher(textileText).replaceAll("");
		}

		// remove tags early...
		textileText = stripTags(textileText, getDisallowTags());

		String converted;
		if (isLite()) {
			TextileLite textile = new TextileLite();
			converted = textile.process(textileText);
		} else {
			Textile2 textile;
			
			if (features != null) {
				if (isAddNoFollow() && !features.contains(TransformerFeature.noFollowLinks)) {
					features = EnumSet.copyOf(features);
					features.add(TransformerFeature.noFollowLinks);
				}
				
				textile = new Textile2(features);
			} else {
				textile = new Textile2(isAddNoFollow());
			}
						
			try {
				converted = textile.process(textileText);
			} catch (IllegalStateException e) {
				LOGGER.error("There was an error performing Textile2 conversion:" + e.getMessage(),e);
				return null; // this would mean that the content is not
								// pre-rendered and will be pre-rendered again
								// at the next attempt, which is MUCH nicer!
			}
		}

		if (isStripAllHtmlAfterConvert()) {
			converted = Pattern.compile("\\<\\S.*?\\>", Pattern.DOTALL)
					.matcher(converted).replaceAll("");
		} else {
			converted = onlyAllowTags(converted, getOnlyAllowTags());
		}

		if (isCorrectHtml()) {
			// correct HTML
			converted = correctHtml(converted);
		}

		return converted;
	}

	/**
	 * Will strip specific HTML tags from the original content. The tags should
	 * be a CSV list, such as "style,script"
	 * 
	 * @param origContent
	 * @param tags
	 * @return
	 */
	public static final String stripTags(final String origContent,
			final String tags) {
		String newContent = origContent;
		if (tags != null && origContent != null) {
			StringTokenizer tokenizer = new StringTokenizer(tags, ",");
			while (tokenizer.hasMoreTokens()) {
				String tagToStrip = tokenizer.nextToken();
				tagToStrip = tagToStrip.trim();
				newContent = newContent.replaceAll("(?i)\\<" + tagToStrip
						+ ".*?\\>", "");
				newContent = newContent.replaceAll("(?i)\\</" + tagToStrip
						+ ".*?\\>", "");
			}
		}
		return newContent;
	}

	public static final String onlyAllowTags(final String origContent,
			final String tags) {
		if (tags == null || origContent == null) {
			return origContent;
		}
		String[] individualTags = tags.split(",");
		String sep = "";
		String tagExp = "";
		for (int i = 0; i < individualTags.length; i++) {
			String tag = individualTags[i];
			tagExp = tagExp + sep + tag;
			sep = "|";
			tagExp = tagExp + sep + "/" + tag;
		}

		String converted = origContent.replaceAll("(?i)<(?!(" + tagExp
				+ ")(\\s.*?>|>)).*?>", "");

		return converted;
	}

	public final String correctHtml(final String origContent) {
		HtmlCleaner cleaner = new HtmlCleaner();
		cleaner.setAllowJavascriptHandlers(allowJavascriptHandlers);
		
		return cleaner.clean(origContent);
	}

	public final String getTextileText() {
		return textileText;
	}

	public final String getDisallowTags() {
		return disallowTags;
	}

	public final void setDisallowTags(final String disallowTags) {
		this.disallowTags = disallowTags;
	}

	public final String getOnlyAllowTags() {
		return onlyAllowTags;
	}

	public final void setOnlyAllowTags(final String onlyAllowTags) {
		this.onlyAllowTags = onlyAllowTags;
	}

	public final boolean isStripAllHtmlAfterConvert() {
		return stripAllHtmlAfterConvert;
	}

	public final void setStripAllHtmlAfterConvert(
			final boolean stripAllHtmlAfterConvert) {
		this.stripAllHtmlAfterConvert = stripAllHtmlAfterConvert;
	}

	public final boolean isStripAllHtmlBeforeConvert() {
		return stripAllHtmlBeforeConvert;
	}

	public final void setStripAllHtmlBeforeConvert(
			final boolean stripAllHtmlBeforeConvert) {
		this.stripAllHtmlBeforeConvert = stripAllHtmlBeforeConvert;
	}

	public final boolean isCorrectHtml() {
		return correctHtml;
	}
	
	public final boolean isAddNoFollow() {
		return addNoFollow;
	}
	
	public final void setAddNoFollow(boolean addNoFollow) {
		this.addNoFollow = addNoFollow;
	}

	public final void setCorrectHtml(boolean correctHtml) {
		this.correctHtml = correctHtml;
	}

	public final boolean isLite() {
		return lite;
	}

	public final void setLite(boolean lite) {
		this.lite = lite;
	}

	public boolean isAllowJavascriptHandlers() {
		return allowJavascriptHandlers;
	}

	public void setAllowJavascriptHandlers(boolean allowJavascriptHandlers) {
		this.allowJavascriptHandlers = allowJavascriptHandlers;
	}

	public EnumSet<TransformerFeature> getFeatures() {
		return features;
	}

	public void setFeatures(EnumSet<TransformerFeature> features) {
		this.features = features;
	}
}