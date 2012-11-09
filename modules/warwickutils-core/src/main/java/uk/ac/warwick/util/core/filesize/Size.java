package uk.ac.warwick.util.core.filesize;

/**
 * Type to represent a file size, with methods to get in various binary units.
 */
final class Size {
	private static final int MULTIPLIER = 1024;
	private static final int K = MULTIPLIER;
	private static final int M = K*MULTIPLIER;
	private static final int G = M*MULTIPLIER;
	
	private long bytes;

	Size(long b) {
		this.bytes = b;
	}
	
	public long getBytes() {
		return bytes;
	}
	
	public long getKibibytes() {
		return bytes / K;
	}
	
	public long getMibibytes() {
		return bytes / M;
	}
	
	public long getGibibytes() {
		return bytes / G;
	}
	
	public static Size bytes(long b) {
		return new Size(b);
	}
	
	public static Size kibibytes(long kib) {
		return new Size(kib * K);
	}
	
	public static Size mibibytes(long mib) {
		return new Size(mib * M);
	}
	
	public static Size gibibytes(long gib) {
		return new Size(gib * G);
	}
}
