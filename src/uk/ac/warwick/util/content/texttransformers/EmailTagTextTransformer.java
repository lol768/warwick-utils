package uk.ac.warwick.util.content.texttransformers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;

import uk.ac.warwick.util.content.MutableContent;

/**
 * Obfuscates an email address passed in the contents. It does this by writing
 * the address out using Javascript after first splitting each character into
 * either a numerical entity, hex entity or the original character randomly.
 * <p>
 * Also stores a blah at blah dot com form in the noscript tags.
 * <p>
 * TODO This replicates a lot of functionality from AbstractSquareTagTransformer.
 * 
 * @author Mat Mannion
 */
public final class EmailTagTextTransformer implements TextTransformer {

	private static final Pattern EMAIL_TAG_PATTERN = Pattern.compile("\\[email"
			+ "(\\s+.+?[^\\\\])?" + "\\]" + "(.+?)" + "\\[/email\\]",
			Pattern.CASE_INSENSITIVE);

	private static final int RANDOM_RESTART_THRESHOLD = 15;

	private static final String[] ALLOWED_PARAMETERS = new String[] { "address" };

	public MutableContent apply(MutableContent mc) {
		String result = mc.getContent();
		if (result.indexOf("[email") != -1) {
			result = doTransform(result);
			mc.setContent(result);
		}
		return mc;
	}

	private String doTransform(String text) {
		//we need to split HTML into do and don't do...
		Pattern noTextile = Pattern.compile("<notextile>(.*?)</notextile>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher matcher = noTextile.matcher(text);
        StringBuilder sb = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;    
        
        Map<String, String> emails = Maps.newHashMap();
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            sb.append(doEmailTransform(text.substring(lastMatch, startIndex), emails));
            sb.append(text.substring(startIndex, endIndex));
            lastMatch = endIndex;
        }
        
        sb.append(doEmailTransform(text.substring(endIndex), emails));
        
        String html = sb.toString();
        
        // now we need to inject the script tag at the top
        if (emails.isEmpty()) {
        	return html;
        } else {
        	return insertScript(html, emails);
        }
	}
	
	private String doEmailTransform(String text, Map<String, String> emails) {
		// Treat tags in comments differently to those not in comments
		Pattern htmlComment = Pattern.compile("<!--(.*?)-->", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher matcher = htmlComment.matcher(text);
        StringBuilder sb = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            sb.append(doEmailTransform(text.substring(lastMatch, startIndex), emails, false));
            sb.append(doEmailTransform(text.substring(startIndex, endIndex), emails, true));
            lastMatch = endIndex;
        }
        
        sb.append(doEmailTransform(text.substring(endIndex), emails, false));
        
        return sb.toString();
	}

	private String doEmailTransform(String text, Map<String, String> emails, boolean isComment) {
		Matcher matcher = EMAIL_TAG_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            sb.append(text.substring(lastMatch, startIndex));
            
            sb.append(replaceWithSpan(text.substring(startIndex, endIndex), emails, matcher, isComment));
            
            lastMatch = endIndex;
        }
        
        sb.append(text.substring(endIndex));
        
        return sb.toString();
	}
	
	private String insertScript(String originalHtml, Map<String, String> emails) {
		String html = originalHtml;
		
		String script = "<script type=\"text/javascript\">\nEvent.onDOMReady(function() { " + StringUtils.join(emails.values(), "\n") + "});\n</script>";

        if (!HTMLSTART.matcher(html).find()) {
            //if originalHtml is just body, wrap it in tags
            html = "<html><head>" + script +"</head><body>" + 
                        html + 
                        "</body></html>";
        } else if (!HEADEND.matcher(html).find()) {
            //if there is html but no head, add a head
            html = HTMLSTART.matcher(html).replaceFirst("$1<head>"+Matcher.quoteReplacement(script)+"</head>");
        } else {
            html = HEADEND.matcher(html).replaceFirst(Matcher.quoteReplacement(script)+"$1");
        }
        return html;
	}

	private String replaceWithSpan(String input,
			Map<String, String> emails, Matcher matcher, boolean isComment) {
		Map<String, Object> parameters = getParameters(matcher);
		String contents = getContents(matcher);
		if (contents == null) {
			return input;
		}

		String caption;
		String email;

		if (parameters.containsKey("address")) {
			caption = contents;
			email = (String) parameters.get("address");
		} else {
			// trim the email and strip out any HTML
			contents = contents.replaceAll("\\<[^\\>]+\\>", "");
			contents = contents.replaceAll("\\s+", "");

			caption = contents;
			email = contents;
		}
		
		StringBuilder sb = new StringBuilder("<a href=\"mailto:");
		Random r = new Random();
		
		int randomInt = r.nextInt();
		if (randomInt < 0) randomInt = -randomInt;
		
		String uniqueId = "email" + randomInt;

		for (int i = 0; i < email.length(); i++) {
			sb.append(convertChar(email.charAt(i), r));
		}

		sb.append("\">");

		for (int i = 0; i < caption.length(); i++) {
			sb.append(convertChar(caption.charAt(i), r));
		}

		sb.append("</a>");
		String js = generateJavascriptCode(sb, r, uniqueId);
		
		if (!isComment) {
			emails.put(uniqueId, js);
		}

		return generateSpanCode(parameters,	caption, email, r, uniqueId, isComment);
	}
	
	private String generateSpanCode(
			final Map<String, Object> parameters, final String caption,
			final String email, final Random r, final String uniqueId, final boolean isComment) {
		String modifiedEmail = email.replaceAll("@", " at ");
		modifiedEmail = modifiedEmail.replaceAll("\\.", " dot ");

		if (parameters.containsKey("address")) {
			modifiedEmail = caption + " (" + modifiedEmail + ")";
		}

		StringBuilder spanSb = new StringBuilder();
		
		if (!isComment) {
			spanSb.append("<span id=\"" + uniqueId + "\">");
		}

		for (int i = 0; i < modifiedEmail.length(); i++) {
			spanSb.append(convertChar(modifiedEmail.charAt(i), r));
		}
		
		if (!isComment) {
			spanSb.append("</span>");
		}
		
		return spanSb.toString();
	}

	private String generateJavascriptCode(
			final StringBuilder sb, final Random r, final String uniqueId) {
		StringBuilder javascriptSb = new StringBuilder("var " + uniqueId + " = '");

		for (int i = 0; i < sb.length(); i++) {
			if (r.nextInt(RANDOM_RESTART_THRESHOLD) == 0) {
				// every 15 or so, start a new document.write
				javascriptSb.append("';\n"+uniqueId+" += '");
			}
			char thisChar = sb.charAt(i);
			if (Character.toString(thisChar).equals("/")) {
				javascriptSb.append("\\");
			}
			javascriptSb.append(thisChar);
		}
		javascriptSb.append("';\ndocument.getElementById('"+uniqueId+"').innerHTML = "+uniqueId+";\n");
		return javascriptSb.toString();
	}

	protected final Map<String, Object> getParameters(Matcher matcher) {      
        return extractParameters(matcher.group(1));
    }
	
	protected final Map<String, Object> extractParameters(final String string) {
        if (string == null) {
            return new HashMap<String, Object>();
        }
        Map<String,Object> result = new HashMap<String,Object>();
        AttributeStringParser parser = new AttributeStringParser(string);
        List<Attribute> attributes = parser.getAttributes();
        for (Attribute a : attributes) {
            String name = a.getName().toLowerCase();
            for (String allowedParameter : ALLOWED_PARAMETERS) {
                if (name.equals(allowedParameter)) {
                    result.put(name, a.getValue());
                }
            }
        }
        return result;
    }
    
    protected final String getContents(Matcher matcher) {
        return matcher.group(2);
    }

	private String convertChar(final char c, final Random r) {
		String result;
		switch (r.nextInt(2)) {
		case 0:
			result = convertCharToDecimalEntity(c);
			break;
		case 1:
			result = convertCharToHexadecimalEntity(c);
			break;
		default:
			result = Character.toString(c);
		}
		return result;
	}

	private String convertCharToDecimalEntity(final char c) {
		return "&#" + Integer.toString(c) + ";";
	}

	private String convertCharToHexadecimalEntity(final char c) {
		return "&#x" + Integer.toHexString(c) + ";";
	}

}
