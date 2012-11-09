/**
 * Textile4J
 * Java implementation of Textism's Textile Humane Web Text Generator
 * Portions  Copyright (c) 2003 Mark Lussier, All Rights Reserved
 *
 * --------------------------------------------------------------------------------
 *
 * Textile is Copyright (c) 2003, Dean Allen, www.textism.com, All rights reserved
 * The  origional Textile can be found at http://www.textism.com/tools/textile
 *
 * _______________
 * TEXTILE LICENSE
 *
 * Redistribution and use in source and binary forms, with or without
 * modifcation, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name Textile nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.warwick.util.content.textile2.lite;

/**
 * Textile Constants
 * 
 * @author Mark Lussier
 * @version $Id: TextileConstants.java,v 1.1 2008/09/30 16:00:11 cuscav Exp $
 */
public interface TextileConstants {

	int MODE_ENT_COMPAT = 0;

	int MODE_ENT_NOQUOTES = 2;

	int MODE_ENT_QUOTES = 3;

	String LESS_THAN = "&lt;";

	String GREATER_THAN = "&gt;";

	String EXP_ISHTML = "<.*>";

	String EXP_AMPERSAND = "&(?![#a-zA-Z0-9]+;)";

	String EXP_AMPERSAND_REPLACE = "x%x%";

	String EXP_DOUBLEQUOTE_MATCH = "(^|\\s)==(.*)==([^[:alnum:]]{0,2})(\\s|$)";

	String EXP_DOUBLEQUOTE_REPLACE = "$1<notextile>$2</notextile>$3$4";

	String EXP_TEXTILE_BLOCK_ESC_PAIR_MATCH = "(?:(.*?)(?:\\n\\s*?|\\A\\s*?|^\\s*?)+?)(?:\\\\\\\\)(?:\\n+?)(\\W|.+?)(?:\\n+?)(?:\\\\\\\\??)(?:\\s*?\\n|\\s*?\\z)(.*)";

	String EXP_TEXTILE_BLOCK_ESC_AND_CODE_PAIR_MATCH = "(?:(.*?)(?:\\n\\s*?|\\A\\s*?|^\\s*?)+?)(?:\\\\\\\\\\\\)(?:\\n+?)(\\W|.+?)(?:\\n+?)(?:\\\\\\\\\\\\??)(?:\\s*?\\n|\\s*?\\z)(.*)";

	String EXP_TEXTILE_BLOCK_ESC_NO_LINE_BREAKS_PAIR_MATCH = "(?:(.*?)(?:\\n\\s*?|\\A\\s*?|^\\s*?)+?)(?:\\\\\\\\\\\\\\\\)(?:\\n+?)(\\W|.+?)(?:\\n+?)(?:\\\\\\\\\\\\\\\\??)(?:\\s*?\\n|\\s*?\\z)(.*)";

	String EXP_IMAGE_QTAG_MATCH = "(?:^|\\s)!(?=.*\\..*)([^\\s\\(^!]+?)\\s?(\\(([^\\)]+?)\\))?!";

	String EXP_IMAGE_QTAG_REPLACE = "<img src=\"$1\" alt=\"$3\" />";

	String EXP_IMAGE_WITH_HREF_QTAG_MATCH = "(<img.+ \\/>):(\\S+)";

	String EXP_IMAGE_WITH_HREF_QTAG_REPLACE = "<a href=\"$2\">$1</a>";

	String EXP_HREF_QTAG_MATCH = "\"([^\"\\(]+)\\s?(\\(([^\\)]+)\\))?\":(\\S+)(\\s|$)";

	String EXP_HREF_QTAG_REPLACE = "<a href=\\\"$4\\\" title=\\\"$3\\\">$1</a>$5";

	String EXP_HREF_QTAG_PUNCT_MATCH = "\"([^\"\\(]+)\\s?(\\(([^\\)]+)\\))?\":(\\S+)([\\.,:\\?!;~\\^*_\\+@\\-\\)])(\\s|$)";

	String EXP_HREF_QTAG_PUNCT_REPLACE = "<a href=\\\"$4\\\" title=\\\"$3\\\">$1</a>$5$6";

	// (?i) for case insensitivity
	String EXP_URL_PUNCT_MATCH = "(?i)(^|\\s|\\()(http://\\S+?)([\\.,:\\?!;~\\^*_\\+@\\-\\)]+)(\\s|$)";

	String EXP_URL_PUNCT_REPLACE = "$1<a href=\\\"$2\\\">link</a>$3$4";

	String EXP_URL_MATCH = "(?i)(^|\\s)(http://\\S+)(\\s|$)";

	String EXP_URL_REPLACE = "$1<a href=\\\"$2\\\">link</a>$3";

	String EXP_ONLY_ALLOW_PROPER_LINKS_MATCH = "(?i) href=\"((?!(http://))(?!/)(?!(https://))).*?\"";

	String EXP_ONLY_ALLOW_PROPER_LINKS_REPLACE = " href=\"\"";

	String EXP_ONLY_ALLOW_PROPER_SRC_MATCH = "(?i) src=\"((?!(http://))(?!/)(?!(https://))).*?\"";

	String EXP_ONLY_ALLOW_PROPER_SRC_REPLACE = " src=\"\"";

	// temporarily commenting out <ins> until find some use/style for it

	// String[] EXP_PHRASE_MODIFIER_SOURCETAGS = { "\\*", "\\?\\?",
	// "(?<![-])-(?![-])", /*"\\+",*/ "@", "_" };
	//    
	// String[] EXP_PHRASE_MODIFIER_REPLACETAGS = { "strong", "cite", "del",
	// /*"ins",*/ "code", "em" };

	String[] EXP_PHRASE_MODIFIER_SOURCETAGS = { "\\*", "\\?\\?",
			"(?<![-])-(?![-])", "@", "_" };

	String[] EXP_PHRASE_MODIFIER_REPLACETAGS = { "strong", "cite", "del",
			"code", "em" };

	String EXP_PHRASE_MODIFIER = "";

	String EXP_SUPERSCRIPT_MATCH = "(?!.*http://[\\S&&[^\\\"]]+\\^)(?!.*www\\.[\\S&&[^\\\"]]+\\^)([^\\\\])(?:\\^)(.*?)([^\\\\])(?:\\^)";

	String EXP_SUPERSCRIPT_REPLACE = "$1<sup>$2$3</sup>";

	String EXP_ESCAPED_SUPERSCRIPT_MATCH = "\\\\\\^";

	String EXP_ESCAPED_SUPERSCRIPT_REPLACE = "\\^";

	String EXP_SUBSCRIPT_MATCH = "(?!.*http://[\\S&&[^\\\"]]+\\~)(?!.*www\\.[\\S&&[^\\\"]]+\\~)([^\\\\])(?:\\~)(.*?)([^\\\\])(?:\\~)";

	String EXP_SUBSCRIPT_REPLACE = "$1<sub>$2$3</sub>";

	String EXP_ESCAPED_SUBSCRIPT_MATCH = "\\\\\\~";

	String EXP_ESCAPED_SUBSCRIPT_REPLACE = "\\~";

	String EXP_EOL_DBL_QUOTES = "\"$";

	String EXP_SINGLE_CLOSING = "\"([^\\\\']*)\\\\'([^\\\\']*)\"";

	String EXP_SINGLE_OPENING = "\\'";

	String EXP_DOUBLE_CLOSING = "([^\\']*)\\\"([^\\\"]*)";

	String EXP_DOUBLE_OPENING = "\"";

	String EXP_ELLIPSES = "(\\w|^|\\s)( )?\\.{3}";

	String EXP_POUND_SIGN = "�";

	String EXP_3UPPER_ACCRONYM = "\\b([A-Z][A-Z0-9]{2,})\\b(\\(([^\\)]+)\\))";

	String EXP_3UPPERCASE_CAPS = "(^|[^\"][>\\s])([A-Z][A-Z0-9 ]{2,})([^<a-z0-9]|$)";

	String EXP_EM_DASH = "(\\s)?--(\\s)?";

	String EXP_EN_DASH = "(\\s)-(\\s)";

	String EXP_EN_DECIMAL_DASH = "(\\d+)-(\\d+)";

	String EXP_DIMENSION_SIGN = "(\\d+) ?x ?(\\d+)";

	String EXP_TRADEMARK = "([^\\\\])(\\((TM)\\))";

	String EXP_REGISTERED = "(\\s+|\\A|^)(\\([R]\\))";

	String EXP_COPYRIGHT = "(\\s+|\\A|^)(\\([C]\\))";

	String EXP_ESCAPED_GLYPHS = "\\\\(\\((C|R|TM)\\))";

	// String REPLACE_SINGLE_CLOSING = "$1&#8217;$2";
	// String REPLACE_SINGLE_OPENING = "&#8216;";
	// String REPLACE_DOUBLE_CLOSING = "$1&#8221;$2";
	// String REPLACE_DOUBLE_OPENING = "&#8220;";

	String REPLACE_ELLIPSES = "$1$2&hellip;";

	String REPLACE_POUND_SIGN = "&pound;";

	String REPLACE_3UPPER_ACCRONYM = "<acronym title=\"$3\">$1</acronym>";

	String REPLACE_3UPPERCASE_CAPS = "$1<span class=\"caps\">$2</span>$3";

	String REPLACE_EM_DASH = "$1&mdash;$2";

	String REPLACE_EN_DASH = "$1&ndash;$2";

	String REPLACE_EN_DECIMAL_DASH = "$1&ndash;$2";

	String REPLACE_DIMENSION_SIGN = "$1&times;$2";

	String REPLACE_TRADEMARK = "$1&trade;";

	String REPLACE_REGISTERED = "$1&reg;";

	String REPLACE_COPYRIGHT = "$1&copy;";

	String REPLACE_ESCAPED_GLYPHS = "$1";

	String EXP_STARTPRESERVE = "<(code|pre|kbd|notextile|latex|media)>";

	String EXP_ENDPRESERVE = "</(code|pre|kbd|notextile|latex|media)>";

	String EXP_NEW_LINE = "(.*)\\n(.*)";

	String REPLACE_LINEBREAK = "$1<br />$2";

	String EXP_FORCESLINEBREAKS = "(\\S)(_*)([:punct:]*) *\\n([^#*\\s])";

	String REPLACE_FORCESLINEBREAK = "$1$2$3<br />$4";

	String EXP_BULLETED_LIST = "^\\*\\s(.*)$";

	String EXP_NUMERIC_LIST = "^#\\s(.*)$";

	String EXP_BLOCKQUOTE = "^bq\\. (.*)";

	String EXP_HEADER_WITHCLASS = "^h(\\d)\\(([\\w]+)\\)\\.\\s(.*)";

	String EXP_HEADER = "^h(\\d)\\. (.*)";

	String EXP_PARA_WITHCLASS = "^p\\(([\\w]+)\\)\\.\\s(.*)";

	String EXP_PARA = "^p\\. (.*)";

	String EXP_REMAINING_PARA = "^([^\\t]+.*)";

	String REPLACE_BULLETED_LIST = "\t<liu>$1</liu>";

	String REPLACE_BULLETED_LIST_EXTRA_LINE_BREAK = "\t<liu>$1</liu><br />";

	String REPLACE_NUMERIC_LIST = "\t<lio>$1</lio>";

	String REPLACE_NUMERIC_LIST_EXTRA_LINE_BREAK = "\t<lio>$1</lio><br />";

	String REPLACE_BLOCKQUOTE = "\t<blockquote>$1</blockquote>";

	String REPLACE_HEADER_WITHCLASS = "\t<h$1 class=\"$2\">$3</h$1>";

	String REPLACE_HEADER = "\t<h$1>$2</h$1>";

	String REPLACE_PARA_WITHCLASS = "\t<p class=\"$1\">$2</p>";

	String REPLACE_PARA = "\t<p>$1</p>";

	String REPLACE_REMAINING_PARA = "\t<p>$1</p>";

	String EXP_LISTSTART = "\\t<li";

	String EXP_MATCHLIST = "^(\\t<li)(o|u)";

	String REPLACE_MATCHLIST = "\n<$2l>\n$1$2";

	String EXP_ENDMATCHLIST = "^(.*)$";

	String REPLACE_ENDMATCHLIST = "l>\n$1";

	String[] ALLOWED_MEDIA_PARAMS = new String[] { "height", "width", "type",
			"bg", "leftbg", "lefticon", "rightbg", "rightbghover", "righticon",
			"righticonhover", "text", "slider", "track", "border", "loader" };

}