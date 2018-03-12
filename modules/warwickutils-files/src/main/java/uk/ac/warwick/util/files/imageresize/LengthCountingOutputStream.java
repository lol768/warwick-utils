package uk.ac.warwick.util.files.imageresize;

import java.io.OutputStream;

/**
 * Discards all written input - just records how many bytes were written.
 */
class LengthCountingOutputStream extends OutputStream {
    private int count = 0;

    @Override
    public void write(int i) {
        count++;
    }

    @Override
    public void write(byte[] bytes) {
        count += bytes.length;
    }

    @Override
    public void write(byte[] bytes, int offset, int length) {
        if (bytes == null) {
            throw new NullPointerException();
        } else if (offset >= 0 && offset <= bytes.length && length >= 0 && offset + length <= bytes.length && offset + length >= 0) {
            count += length;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public int length() {
        return count;
    }
}
