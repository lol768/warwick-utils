package uk.ac.warwick.util.files.imageresize;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream which does nothing but count the number of count written to it.
 * Useful if you have a write operation and you just want to know the size of
 * the output in bytes.
 *
 * You don't need to bother flushing or closing this stream; doing so does nothing.
 */
public class CountingOutputStream extends OutputStream {

	private long count;

	@Override
	public void write(int b) throws IOException {
		count++;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		count += len;
	}

	/**
	 * The number of bytes written to this stream so far.
	 */
	public long getBytesWritten() {
		return count;
	}

}
