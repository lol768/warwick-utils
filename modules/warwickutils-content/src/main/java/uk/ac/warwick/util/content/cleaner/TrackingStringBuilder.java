package uk.ac.warwick.util.content.cleaner;

/**
 * Wrapper around StringBuilder to track various things such as whether
 * we're at the start of a new line. 
 * 
 * Could have extended AbstractStringBuilder instead.
 */
public final class TrackingStringBuilder {
    private StringBuilder builder;

    private boolean startOfLine;

    public TrackingStringBuilder() {
        builder = new StringBuilder();
        startOfLine = true;
    }

    public void append(final StringBuilder builder2) {
        builder.append(builder2);
    }

    public void append(final String s) {
        builder.append(s);
        startOfLine = (s.endsWith("\n"));
    }

    public void append(final char c) {
        builder.append(c);
        startOfLine = (c == '\n');
    }

    public boolean isStartOfLine() {
        return startOfLine;
    }

    public String toString() {
        return builder.toString();
    }
}
