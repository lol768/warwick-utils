package uk.ac.warwick.util.content.texttransformers;

import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

import uk.ac.warwick.util.content.freemarker.FreeMarkerRenderingUtils;
import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer.Callback;

/**
 * Obfuscates an email address passed in the contents. It does this by writing
 * the address out using Javascript after first splitting each character into
 * either a numerical entity, hex entity or the original character randomly.
 * <p>
 * Also stores a blah at blah dot com form in the noscript tags.
 * 
 * @author Mat Mannion
 */
public final class EmailTagTextTransformer extends AbstractSquareTagTransformer {

    private static final int RANDOM_RESTART_THRESHOLD = 15;
    
    private static final String[] ALLOWED_PARAMETERS = new String[] { "address" };

    private static final String EMAIL_TAG_TEMPLATE = "magictags/email.ftl";

    public EmailTagTextTransformer() {
        super("email");
    }

    @Override
    protected String[] getAllowedParameters() {
        return ALLOWED_PARAMETERS;
    }

    @Override
    protected Callback getCallback() {
        return new TextPatternTransformer.Callback() {
            public String transform(final String input) {
                Matcher matcher = getTagPattern().matcher(input);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Failed to match email tag, but shouldn't be here if it didn't");
                }

                Map<String, Object> parameters = getParameters(matcher);
                String contents = getContents(matcher);
                if (contents == null) {
                    return input;
                }
                
                String caption;
                String email;
                
                if (parameters.containsKey("address")) {
                    caption = contents;
                    email = (String)parameters.get("address");
                } else {
                    //trim the email and strip out any HTML
                    contents = contents.replaceAll("\\<[^\\>]+\\>", "");
                    contents = contents.replaceAll("\\s+","");
                    
                    caption = contents;
                    email = contents;
                }

                StringBuilder sb = new StringBuilder("<a href=\"mailto:");
                Random r = new Random();

                for (int i = 0; i < email.length(); i++) {
                    sb.append(convertChar(email.charAt(i), r));
                }

                sb.append("\">");

                for (int i = 0; i < caption.length(); i++) {
                    sb.append(convertChar(caption.charAt(i), r));
                }

                sb.append("</a>");

                StringBuilder noscriptSb = generateNoScriptCode(parameters, caption, email, r);
                
                StringBuilder javascriptSb = generateJavascriptCode(sb, r);

                parameters.put("javascript", javascriptSb.toString());
                parameters.put("noscriptContents", noscriptSb.toString());

                return renderTemplate(EMAIL_TAG_TEMPLATE, parameters);
            }

            private StringBuilder generateNoScriptCode(final Map<String, Object> parameters, final String caption, final String email, final Random r) {
                String modifiedEmail = email.replaceAll("@", " at ");
                modifiedEmail = modifiedEmail.replaceAll("\\.", " dot ");
                
                if (parameters.containsKey("address")) {
                    modifiedEmail = caption + " (" + modifiedEmail + ")";
                }

                StringBuilder noscriptSb = new StringBuilder();

                for (int i = 0; i < modifiedEmail.length(); i++) {
                    noscriptSb.append(convertChar(modifiedEmail.charAt(i), r));
                }
                return noscriptSb;
            }

            private StringBuilder generateJavascriptCode(final StringBuilder sb, final Random r) {
                StringBuilder javascriptSb = new StringBuilder("document.write('");
                
                for (int i = 0; i < sb.length(); i++) {
                    if (r.nextInt(RANDOM_RESTART_THRESHOLD) == 0) {
                        //every 15 or so, start a new document.write
                        javascriptSb.append("');\ndocument.write('");
                    }
                    char thisChar = sb.charAt(i);
                    if (Character.toString(thisChar).equals("/")) {
                        javascriptSb.append("\\");
                    }
                    javascriptSb.append(thisChar);
                }
                javascriptSb.append("');\n");
                return javascriptSb;
            }
        };
    }

    private String renderTemplate(final String templateName, final Map<String, Object> model) {
        return FreeMarkerRenderingUtils.processTemplate(templateName, model).toString();
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
