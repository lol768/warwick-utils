package uk.ac.warwick.util.concurrency.promise;

/**
 * An implementation of a {@link Promise} that is allowed to be written to exactly once.
 */
public class WriteOncePromise<T> extends MutablePromise<T> {
	
	private boolean written;
	
	public WriteOncePromise() {
		super();
	}

	public WriteOncePromise(T theValue) {
		super(theValue);
	}

	@Override
	public void setValue(T value) {
		if (written) throw new IllegalStateException("The promise was written to more than once!");
		
		super.setValue(value);
		written = true;
	}

	public boolean isWritten() {
		return written;
	}

}
