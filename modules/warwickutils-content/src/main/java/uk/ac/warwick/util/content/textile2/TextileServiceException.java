package uk.ac.warwick.util.content.textile2;

public class TextileServiceException extends Exception {

	private static final long serialVersionUID = -9212566792265883515L;
	
	public TextileServiceException(final String exception) {
		super(exception);
	}
	
	public TextileServiceException(final String exception, final Throwable e) {
		super(exception, e);
	}

}
