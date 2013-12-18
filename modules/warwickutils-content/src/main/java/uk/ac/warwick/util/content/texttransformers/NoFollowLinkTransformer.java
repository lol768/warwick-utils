package uk.ac.warwick.util.content.texttransformers;

import com.google.common.collect.Sets;
import uk.ac.warwick.util.content.MutableContent;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Add rel="nofollow" to any links that we have
 */
public class NoFollowLinkTransformer implements TextTransformer {

	private static final Pattern LINK_PATTERN = Pattern.compile("(<a\\s)(.+?>)", Pattern.CASE_INSENSITIVE);
	private static final Pattern REL_PATTERN = Pattern.compile("\\srel=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

	public MutableContent apply(MutableContent mc) {
		String content = mc.getContent();

		// add nofollow
		Matcher linkMatcher = LINK_PATTERN.matcher(content);
		StringBuffer noFollowed = new StringBuffer();

		while (linkMatcher.find()) {
			linkMatcher.appendReplacement(noFollowed, linkMatcher.group(1) + Matcher.quoteReplacement(tidyTail(linkMatcher.group(2))));
		}
		linkMatcher.appendTail(noFollowed);

		mc.setContent(noFollowed.toString());
		return mc;
	}

	private String tidyTail(String linkTail) {
		Matcher relMatcher = REL_PATTERN.matcher(linkTail);
		StringBuilder newLinkTail = new StringBuilder();
		// use a treeset for predictable ordering
		SortedSet<String> rels = Sets.newTreeSet();

		while (relMatcher.find()) {
			// gotta catch 'em all
			rels.add(relMatcher.group(1).toLowerCase());
		}
		// ah, yes, this was why we came in...
		rels.add("nofollow");

		// add rel attribute concatenating from the set
		newLinkTail.append("rel=\"");
		Iterator iter = rels.iterator();
		while (iter.hasNext()) {
			newLinkTail.append(iter.next());
			if (iter.hasNext()) newLinkTail.append(" ");
		}
		newLinkTail.append("\" ");

		// drop in the existing link tail, minus any pre-existing rels
		newLinkTail.append(relMatcher.replaceAll(""));

		return newLinkTail.toString();
	}
}
