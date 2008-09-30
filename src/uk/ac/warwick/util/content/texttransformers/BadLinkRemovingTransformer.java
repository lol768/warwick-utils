package uk.ac.warwick.util.content.texttransformers;

/**
 * Remove any links that have protocols other than we allow
 * 
 * @author Mat Mannion
 */
public class BadLinkRemovingTransformer implements TextTransformer {
	
	private static String[] BANNED_HREF_PROTOCOLS = {"javascript:"};
	
	private static String[] BANNED_SRC_PROTOCOLS = {"javascript:"};

	private static String EXP_ONLY_ALLOW_PROPER_LINKS_REPLACE = " href=\"\"";

	private static String EXP_ONLY_ALLOW_PROPER_SRC_REPLACE = " src=\"\"";
	
    public String transform(String content) {
    	
    	//generate regexes
    	String EXP_ONLY_ALLOW_PROPER_LINKS_MATCH = "(?i) href=\"(";
    	
    	for (int i=0;i<BANNED_HREF_PROTOCOLS.length;i++) {
    		EXP_ONLY_ALLOW_PROPER_LINKS_MATCH += "((" + BANNED_HREF_PROTOCOLS[i] + "))";
    	}
    	
    	EXP_ONLY_ALLOW_PROPER_LINKS_MATCH += ").*?\"";
    	
    	String EXP_ONLY_ALLOW_PROPER_SRC_MATCH = "(?i) src=\"(";
    			
    	for (int i=0;i<BANNED_SRC_PROTOCOLS.length;i++) {
    		EXP_ONLY_ALLOW_PROPER_SRC_MATCH += "((" + BANNED_SRC_PROTOCOLS[i] + "))";
    	}
    	
    	EXP_ONLY_ALLOW_PROPER_SRC_MATCH += ").*?\"";
    	
    	content = content.replaceAll(EXP_ONLY_ALLOW_PROPER_LINKS_MATCH, EXP_ONLY_ALLOW_PROPER_LINKS_REPLACE);
		
		content = content.replaceAll(EXP_ONLY_ALLOW_PROPER_SRC_MATCH, EXP_ONLY_ALLOW_PROPER_SRC_REPLACE);
		
		return content;
    }
}
