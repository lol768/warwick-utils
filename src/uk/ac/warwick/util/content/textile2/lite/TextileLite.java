package uk.ac.warwick.util.content.textile2.lite;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Textile Java implementation of Textism's Textile Humane Web Text Generator
 * 
 * Stripped of some of the gumph to be TextileLite - very fast for use in
 * titles, etc.
 * 
 * For Origional PHP see http://www.textism.com/tools/textile
 * 
 * @author Mat Mannion
 */
public class TextileLite implements TextileConstants {

	/**
	 * Public Constructor
	 */
	public TextileLite() {
	}

	/**
	 * Process a textile formatted string
	 * 
	 * This will JUST do qtags for the moment - a very lite and quick version,
	 * to be used for stuff that doesn't have line breaks!
	 * 
	 * @param content
	 *            Textile formatted content
	 * @return Content converted to HTML
	 */
	public final String process(final String origContent) {

		String content = origContent;

		/*
		 * zap carriage returns
		 */
		content = replace(content, "\r\n", "\n");

		/*
		 * zap tabs
		 */
		content = replace(content, "\t", "");

		Content textContent = new Content(content);

		return textContent.getContentProcessedSoFar()
				+ processTextile(textContent.getRemainingContent());

	}

	private String processTextile(String content) {

		/**
		 * Turn any incoming ampersands into a dummy character for now. This
		 * uses a negative lookahead for alphanumerics followed by a semicolon,
		 * implying an incoming html entity, to be skipped
		 */
		content = content.replaceAll(EXP_AMPERSAND, EXP_AMPERSAND_REPLACE);

		/**
		 * zap carriage returns
		 */
		content = replace(content, "\r\n", "\n");

		/**
		 * zap tabs
		 */
		content = replace(content, "\t", "");

		/**
		 * hyperlink qtag
		 */
		content = content.replaceAll(EXP_HREF_QTAG_PUNCT_MATCH,
				EXP_HREF_QTAG_PUNCT_REPLACE);

		content = content
				.replaceAll(EXP_HREF_QTAG_MATCH, EXP_HREF_QTAG_REPLACE);

		/**
		 * convert urls to hyperlinks
		 */

		content = content.replaceAll(EXP_ONLY_ALLOW_PROPER_LINKS_MATCH,
				EXP_ONLY_ALLOW_PROPER_LINKS_REPLACE);

		content = content.replaceAll(EXP_ONLY_ALLOW_PROPER_SRC_MATCH,
				EXP_ONLY_ALLOW_PROPER_SRC_REPLACE);

		/**
		 * loop through the array, replacing qtags with html
		 */

		for (int x = 0; x < EXP_PHRASE_MODIFIER_SOURCETAGS.length; x++) {

			for (int y = 0; y <= 2; y++) {
				content = doStandardTags(content, x, y);
			}

		}

		/**
		 * replace escaped characters
		 */
		for (int x = 0; x < EXP_PHRASE_MODIFIER_SOURCETAGS.length; x++) {

			for (int y = 0; y <= 2; y++) {
				content = doEscapedStandardTags(content, x, y);
			}

		}

		/**
		 * From the Original Docs: "some weird bs with underscores and \b word
		 * boundaries, so we'll do those on their own"
		 */

		// superscript and subscript matches are quite expensive, so do
		// conditional matching
		if (count(content, '^') > 1) {
			content = content.replaceAll(EXP_SUPERSCRIPT_MATCH,
					EXP_SUPERSCRIPT_REPLACE);
		}
		content = content.replaceAll(EXP_ESCAPED_SUPERSCRIPT_MATCH,
				EXP_ESCAPED_SUPERSCRIPT_REPLACE);

		if (count(content, '~') > 1) {
			content = content.replaceAll(EXP_SUBSCRIPT_MATCH,
					EXP_SUBSCRIPT_REPLACE);
		}

		content = content.replaceAll(EXP_ESCAPED_SUBSCRIPT_MATCH,
				EXP_ESCAPED_SUBSCRIPT_REPLACE);

		/**
		 * small problem with double quotes at the end of a string
		 */
		content = content.replaceAll(EXP_EOL_DBL_QUOTES, " ");

		boolean ishtml = Pattern.compile(EXP_ISHTML).matcher(content).find();
		boolean inpreservation = false;

		if (!ishtml) {
			content = doGlyphs(content);
		} else {
			String[] segments = splitContent(EXP_ISHTML, content);

			StringBuffer segmentBuffer = new StringBuffer();
			for (int x = 0; x < segments.length; x++) {
				// # matches are off if we're between <code>, <pre> etc.
				if (segments[x].toLowerCase().matches(EXP_STARTPRESERVE)) {
					inpreservation = true;
				} else if (segments[x].toLowerCase().matches(EXP_ENDPRESERVE)) {
					inpreservation = false;
				}

				if (!Pattern.compile(EXP_ISHTML).matcher(segments[x]).find()
						&& !inpreservation) {
					segments[x] = doGlyphs(segments[x]);
				}

				// # convert htmlspecial if between <code>
				if (inpreservation) {
					segments[x] = htmlSpecialChars(segments[x],
							MODE_ENT_NOQUOTES);
					segments[x] = replace(segments[x], "&lt;pre&gt;", "<pre>");
					segments[x] = replace(segments[x], "&lt;code&gt;", "<code>");
					segments[x] = replace(segments[x], "&lt;notextile&gt;",
							"<notextile>");
				}

				segmentBuffer.append(segments[x]);

			}

			content = segmentBuffer.toString();

		}

		// Trim trailing EOL
		if (content.endsWith("\n")) {
			content = content.substring(0, content.length() - 1);
		}
		// Trim starting EOL
		if (content.startsWith("\n") || content.startsWith("\t")) {
			content = content.substring(1, content.length());
		}

		if (content.indexOf("\n") == -1) {
			content = content.replaceAll("<p>", "");
			content = content.replaceAll("</p>", "");
		}

		// Turn the temp char back to an ampersand entity
		content = replace(content, "x%x%", "&#38;");
		
		return content;
	}

	private String doStandardTags(String content, int x, int y) {
		String prefix = "([\\W&&[^;,\\\\]]|^|_)"
				+ EXP_PHRASE_MODIFIER_SOURCETAGS[x];
		String suffix = "";
		if (y == 0) {
			prefix += "([^\\s&&[^;,]]+?)(?<![\\\\])"
					+ EXP_PHRASE_MODIFIER_SOURCETAGS[x];
			suffix = "$1<" + EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$2</"
					+ EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$3";
		}
		if (y == 1) {
			prefix += "([^\\s&&[^;,]]+?)([^\\s&&[^;,]]+?)(?<![\\\\])"
					+ EXP_PHRASE_MODIFIER_SOURCETAGS[x];
			suffix = "$1<" + EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$2$3</"
					+ EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$4";
		}
		if (y == 2) {
			prefix += "([^\\s&&[^;,]]+?)(.+?)([^\\s&&[^;,]]+?)(?<![\\\\])"
					+ EXP_PHRASE_MODIFIER_SOURCETAGS[x];
			suffix = "$1<" + EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$2$3$4</"
					+ EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$5";
		}
		prefix += "(\\W|$|_)";

		content = content.replaceAll(prefix, suffix);
		return content;
	}

	private String doEscapedStandardTags(String content, int x, int y) {
		String prefix = "([\\W&&[^;,]]|^|_)(?:[\\\\]?+)"
				+ EXP_PHRASE_MODIFIER_SOURCETAGS[x];
		String suffix = "";
		if (y == 0) {
			prefix += "([^\\s&&[^;,]]+?)(?:\\\\)"
					+ EXP_PHRASE_MODIFIER_SOURCETAGS[x];
			suffix = "$1" + EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "$2"
					+ EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "$3";
		}
		if (y == 1) {
			prefix += "([^\\s&&[^;,]]+?)([^\\s&&[^;,]]+?)(?:[\\\\]?+)"
					+ EXP_PHRASE_MODIFIER_SOURCETAGS[x];
			suffix = "$1" + EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "$2$3"
					+ EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "$4";
		}
		if (y == 2) {
			prefix += "([^\\s&&[^;,]]+?)(.+?)([^\\s&&[^;,]]+?)(?:[\\\\]?+)"
					+ EXP_PHRASE_MODIFIER_SOURCETAGS[x];
			suffix = "$1" + EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "$2$3$4"
					+ EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "$5";
		}
		prefix += "(\\W|$|_)";
		content = content.replaceAll(prefix, suffix);
		return content;
	}

	/**
	 * An implementation of the PHP htmlspecialchars()
	 * 
	 * @param content
	 *            Source string
	 * @param mode
	 *            Mode to select replacement string for quotes
	 * 
	 * @return String with replace occurrences
	 */
	private String htmlSpecialChars(final String origContent, final int mode) {
		String content = origContent;
		content = replace(content, "&", "&amp;");

		if (mode != MODE_ENT_NOQUOTES) {
			content = replace(content, "\"", "&quot;");
		}
		if (mode == MODE_ENT_QUOTES) {
			content = replace(content, "'", "&#039;");
		}
		content = replace(content, "<", LESS_THAN);
		content = replace(content, ">", GREATER_THAN);
		return content;
	}

	/**
	 * Splits a string into a string array based on a matching regex
	 * 
	 * @param matchexp
	 *            Expression to match
	 * @param content
	 *            Content to split
	 * @return String array of split content
	 */
	private String[] splitContent(final String matchexp, final String content) {
		int startAt = 0;
		List<String> tempList = new ArrayList<String>();
		Pattern pattern = Pattern.compile(matchexp);

		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			tempList.add(content.substring(startAt, matcher.start()));
			tempList.add(matcher.group());
			startAt = matcher.end();
		}

		tempList.add(content.substring(startAt));

		String[] result = new String[tempList.size()];

		for (int i = 0; i < result.length; i++) {
			result[i] = (String) tempList.get(i);
		}

		return result;
	}

	/**
	 * Replace an array of match patterns in a string
	 * 
	 * @param content
	 *            Source string
	 * @param matches
	 *            Match patterns
	 * @param replaces
	 *            Replacement patterns
	 * @return String with replaced occurrences
	 */
	private String arrayReplaceAll(final String content,
			final String[] matches, final String[] replaces) {
		String result = content;

		for (int x = 0; x < matches.length; x++) {
			String match = matches[x];
			String replace = replaces[x];
			result = result.replaceAll(match, replace);
		}

		return result;
	}

	/**
	 * Replace any occurances of a string pattern within a string with a
	 * different string.
	 * 
	 * @param str
	 *            The source string. This is the string that will be searched
	 *            and have the replacements
	 * @param pattern
	 *            The pattern to look for in str
	 * @param replace
	 *            The string to insert in the place of <i>pattern </i>
	 * @return String with replaced occurences
	 */
	private static String replace(final String str, final String pattern,
			final String replace) {
		if (str == null || "".equals(str)) {
			return str;
		}

		if (replace == null) {
			return str;
		}

		if ("".equals(pattern)) {
			return str;
		}

		int s = 0;
		int e = 0;
		StringBuffer result = new StringBuffer();

		while ((e = str.indexOf(pattern, s)) >= 0) {
			result.append(str.substring(s, e));
			result.append(replace);
			s = e + pattern.length();
		}
		result.append(str.substring(s));
		return result.toString();
	}

	private String doGlyphs(String content) {

		String[] glyphMatches = { EXP_ELLIPSES, EXP_3UPPER_ACCRONYM,
				EXP_3UPPERCASE_CAPS, EXP_EM_DASH, EXP_EN_DECIMAL_DASH,
				EXP_DIMENSION_SIGN, EXP_TRADEMARK, EXP_REGISTERED,
				EXP_COPYRIGHT };

		String[] glyphReplacement = { REPLACE_ELLIPSES,
				REPLACE_3UPPER_ACCRONYM, REPLACE_3UPPERCASE_CAPS,
				REPLACE_EM_DASH, REPLACE_EN_DECIMAL_DASH,
				REPLACE_DIMENSION_SIGN, REPLACE_TRADEMARK, REPLACE_REGISTERED,
				REPLACE_COPYRIGHT };

		String stringAfterGlyphs = arrayReplaceAll(content, glyphMatches,
				glyphReplacement);

		stringAfterGlyphs = insertEntities(stringAfterGlyphs);

		return stringAfterGlyphs.replaceAll(EXP_ESCAPED_GLYPHS,
				REPLACE_ESCAPED_GLYPHS);

	}

	private String insertEntities(String text) {

		if (text == null) {
			return null;
		}

		int originalTextLength = text.length();
		StringBuffer sb = new StringBuffer(originalTextLength * 110 / 100);
		int charsToAppend = 0;
		for (int i = 0; i < originalTextLength; i++) {
			char c = text.charAt(i);
			String entity = charToEntity(c);
			if (entity == null) {
				// we could sb.append( c ), but that would be slower
				// than saving them up for a big append.
				charsToAppend++;
			} else {
				if (charsToAppend != 0) {
					sb.append(text.substring(i - charsToAppend, i));
					charsToAppend = 0;
				}
				sb.append(entity);
			}
		}

		// append chars to the right of the last entity.
		if (charsToAppend != 0) {
			sb.append(text.substring(originalTextLength - charsToAppend,
					originalTextLength));
		}

		// if result is not longer, we did not do anything. Save RAM.
		if (sb.length() == originalTextLength) {
			return text;
		}
		return sb.toString();

	}

	/**
	 * convert a single char to its equivalent HTML entity. Ordinary chars are
	 * not changed. 160 -> &nbsp;
	 * 
	 * @param characterToConvert
	 * @return
	 */
	private static String charToEntity(char characterToConvert) {
		switch (characterToConvert) {
		case 145:
			return "&lsquo;";
		case 146:
			return "&rsquo;";
		case 150:
			return "&ndash;";
		case 45:
			return "&ndash;";
		case 160:
			return "&nbsp;";
		case 161:
			return "&iexcl;";
		case 162:
			return "&cent;";
		case 163:
			return "&pound;";
		case 164:
			return "&curren;";
		case 165:
			return "&yen;";
		case 166:
			return "&brvbar;";
		case 167:
			return "&sect;";
		case 168:
			return "&uml;";
		case 169:
			return "&copy;";
		case 170:
			return "&ordf;";
		case 171:
			return "&laquo;";
		case 172:
			return "&not;";
		case 173:
			return "&shy;";
		case 174:
			return "&reg;";
		case 175:
			return "&macr;";
		case 176:
			return "&deg;";
		case 177:
			return "&plusmn;";
		case 178:
			return "&sup2;";
		case 179:
			return "&sup3;";
		case 180:
			return "&acute;";
		case 181:
			return "&micro;";
		case 182:
			return "&para;";
		case 183:
			return "&middot;";
		case 184:
			return "&cedil;";
		case 185:
			return "&sup1;";
		case 186:
			return "&ordm;";
		case 187:
			return "&raquo;";
		case 188:
			return "&frac14;";
		case 189:
			return "&frac12;";
		case 190:
			return "&frac34;";
		case 191:
			return "&iquest;";
		case 192:
			return "&Agrave;";
		case 193:
			return "&Aacute;";
		case 194:
			return "&Acirc;";
		case 195:
			return "&Atilde;";
		case 196:
			return "&Auml;";
		case 197:
			return "&Aring;";
		case 198:
			return "&AElig;";
		case 199:
			return "&Ccedil;";
		case 200:
			return "&Egrave;";
		case 201:
			return "&Eacute;";
		case 202:
			return "&Ecirc;";
		case 203:
			return "&Euml;";
		case 204:
			return "&Igrave;";
		case 205:
			return "&Iacute;";
		case 206:
			return "&Icirc;";
		case 207:
			return "&Iuml;";
		case 208:
			return "&ETH;";
		case 209:
			return "&Ntilde;";
		case 210:
			return "&Ograve;";
		case 211:
			return "&Oacute;";
		case 212:
			return "&Ocirc;";
		case 213:
			return "&Otilde;";
		case 214:
			return "&Ouml;";
		case 215:
			return "&times;";
		case 216:
			return "&Oslash;";
		case 217:
			return "&Ugrave;";
		case 218:
			return "&Uacute;";
		case 219:
			return "&Ucirc;";
		case 220:
			return "&Uuml;";
		case 221:
			return "&Yacute;";
		case 222:
			return "&THORN;";
		case 223:
			return "&szlig;";
		case 224:
			return "&agrave;";
		case 225:
			return "&aacute;";
		case 226:
			return "&acirc;";
		case 227:
			return "&atilde;";
		case 228:
			return "&auml;";
		case 229:
			return "&aring;";
		case 230:
			return "&aelig;";
		case 231:
			return "&ccedil;";
		case 232:
			return "&egrave;";
		case 233:
			return "&eacute;";
		case 234:
			return "&ecirc;";
		case 235:
			return "&euml;";
		case 236:
			return "&igrave;";
		case 237:
			return "&iacute;";
		case 238:
			return "&icirc;";
		case 239:
			return "&iuml;";
		case 240:
			return "&eth;";
		case 241:
			return "&ntilde;";
		case 242:
			return "&ograve;";
		case 243:
			return "&oacute;";
		case 244:
			return "&ocirc;";
		case 245:
			return "&otilde;";
		case 246:
			return "&ouml;";
		case 247:
			return "&divide;";
		case 248:
			return "&oslash;";
		case 249:
			return "&ugrave;";
		case 250:
			return "&uacute;";
		case 251:
			return "&ucirc;";
		case 252:
			return "&uuml;";
		case 253:
			return "&yacute;";
		case 254:
			return "&thorn;";
		case 255:
			return "&yuml;";
		case 338:
			return "&OElig;";
		case 339:
			return "&oelig;";
		case 352:
			return "&Scaron;";
		case 353:
			return "&scaron;";
		case 376:
			return "&Yuml;";
		case 402:
			return "&fnof;";
		case 710:
			return "&circ;";
		case 732:
			return "&tilde;";
		case 913:
			return "&Alpha;";
		case 914:
			return "&Beta;";
		case 915:
			return "&Gamma;";
		case 916:
			return "&Delta;";
		case 917:
			return "&Epsilon;";
		case 918:
			return "&Zeta;";
		case 919:
			return "&Eta;";
		case 920:
			return "&Theta;";
		case 921:
			return "&Iota;";
		case 922:
			return "&Kappa;";
		case 923:
			return "&Lambda;";
		case 924:
			return "&Mu;";
		case 925:
			return "&Nu;";
		case 926:
			return "&Xi;";
		case 927:
			return "&Omicron;";
		case 928:
			return "&Pi;";
		case 929:
			return "&Rho;";
		case 931:
			return "&Sigma;";
		case 932:
			return "&Tau;";
		case 933:
			return "&Upsilon;";
		case 934:
			return "&Phi;";
		case 935:
			return "&Chi;";
		case 936:
			return "&Psi;";
		case 937:
			return "&Omega;";
		case 945:
			return "&alpha;";
		case 946:
			return "&beta;";
		case 947:
			return "&gamma;";
		case 948:
			return "&delta;";
		case 949:
			return "&epsilon;";
		case 950:
			return "&zeta;";
		case 951:
			return "&eta;";
		case 952:
			return "&theta;";
		case 953:
			return "&iota;";
		case 954:
			return "&kappa;";
		case 955:
			return "&lambda;";
		case 956:
			return "&mu;";
		case 957:
			return "&nu;";
		case 958:
			return "&xi;";
		case 959:
			return "&omicron;";
		case 960:
			return "&pi;";
		case 961:
			return "&rho;";
		case 962:
			return "&sigmaf;";
		case 963:
			return "&sigma;";
		case 964:
			return "&tau;";
		case 965:
			return "&upsilon;";
		case 966:
			return "&phi;";
		case 967:
			return "&chi;";
		case 968:
			return "&psi;";
		case 969:
			return "&omega;";
		case 977:
			return "&thetasym;";
		case 978:
			return "&upsih;";
		case 982:
			return "&piv;";
		case 8194:
			return "&ensp;";
		case 8195:
			return "&emsp;";
		case 8201:
			return "&thinsp;";
		case 8204:
			return "&zwnj;";
		case 8205:
			return "&zwj;";
		case 8206:
			return "&lrm;";
		case 8207:
			return "&rlm;";
		case 8211:
			return "&ndash;";
		case 8212:
			return "&mdash;";
		case 8216:
			return "&lsquo;";
		case 8217:
			return "&rsquo;";
		case 8218:
			return "&sbquo;";
		case 8220:
			return "&ldquo;";
		case 8221:
			return "&rdquo;";
		case 8222:
			return "&bdquo;";
		case 8224:
			return "&dagger;";
		case 8225:
			return "&Dagger;";
		case 8226:
			return "&bull;";
		case 8230:
			return "&hellip;";
		case 8240:
			return "&permil;";
		case 8242:
			return "&prime;";
		case 8243:
			return "&Prime;";
		case 8249:
			return "&lsaquo;";
		case 8250:
			return "&rsaquo;";
		case 8254:
			return "&oline;";
		case 8260:
			return "&frasl;";
		case 8364:
			return "&euro;";
		case 8465:
			return "&image;";
		case 8472:
			return "&weierp;";
		case 8476:
			return "&real;";
		case 8482:
			return "&trade;";
		case 8501:
			return "&alefsym;";
		case 8592:
			return "&larr;";
		case 8593:
			return "&uarr;";
		case 8594:
			return "&rarr;";
		case 8595:
			return "&darr;";
		case 8596:
			return "&harr;";
		case 8629:
			return "&crarr;";
		case 8656:
			return "&lArr;";
		case 8657:
			return "&uArr;";
		case 8658:
			return "&rArr;";
		case 8659:
			return "&dArr;";
		case 8660:
			return "&hArr;";
		case 8704:
			return "&forall;";
		case 8706:
			return "&part;";
		case 8707:
			return "&exist;";
		case 8709:
			return "&empty;";
		case 8711:
			return "&nabla;";
		case 8712:
			return "&isin;";
		case 8713:
			return "&notin;";
		case 8715:
			return "&ni;";
		case 8719:
			return "&prod;";
		case 8721:
			return "&sum;";
		case 8722:
			return "&minus;";
		case 8727:
			return "&lowast;";
		case 8730:
			return "&radic;";
		case 8733:
			return "&prop;";
		case 8734:
			return "&infin;";
		case 8736:
			return "&ang;";
		case 8743:
			return "&and;";
		case 8744:
			return "&or;";
		case 8745:
			return "&cap;";
		case 8746:
			return "&cup;";
		case 8747:
			return "&int;";
		case 8756:
			return "&there4;";
		case 8764:
			return "&sim;";
		case 8773:
			return "&cong;";
		case 8776:
			return "&asymp;";
		case 8800:
			return "&ne;";
		case 8801:
			return "&equiv;";
		case 8804:
			return "&le;";
		case 8805:
			return "&ge;";
		case 8834:
			return "&sub;";
		case 8835:
			return "&sup;";
		case 8836:
			return "&nsub;";
		case 8838:
			return "&sube;";
		case 8839:
			return "&supe;";
		case 8853:
			return "&oplus;";
		case 8855:
			return "&otimes;";
		case 8869:
			return "&perp;";
		case 8901:
			return "&sdot;";
		case 8968:
			return "&lceil;";
		case 8969:
			return "&rceil;";
		case 8970:
			return "&lfloor;";
		case 8971:
			return "&rfloor;";
		case 9001:
			return "&lang;";
		case 9002:
			return "&rang;";
		case 9674:
			return "&loz;";
		case 9824:
			return "&spades;";
		case 9827:
			return "&clubs;";
		case 9829:
			return "&hearts;";
		case 9830:
			return "&diams;";
		default:
			return null;

		}

	}

	private static int count(String str, char c) {
		int index = 0;
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == c)
				index++;
		}
		return index;
	}

}