package uk.ac.warwick.util.content.texttransformers.embed;

public class OEmbedException extends Exception {
    private static final long serialVersionUID = 6037807180716366744L;

    public OEmbedException(String message) {
        super(message);
    }

    public OEmbedException(Throwable cause) {
        super(cause);
    }
}