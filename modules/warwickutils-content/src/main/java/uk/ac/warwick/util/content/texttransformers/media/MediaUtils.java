package uk.ac.warwick.util.content.texttransformers.media;

public class MediaUtils {
	
	/**
     * Return the extension of the specified fileName. If there is no extension,
     * this will return "".
     */
    public static String getExtension(final String s) {
        int indexOfLastDot = s.lastIndexOf('.');
        if (indexOfLastDot < 0) {
            return "";
        }
        return s.substring(indexOfLastDot + 1);
    }

}
