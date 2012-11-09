/*
 * Created on 08-Nov-2005
 *
 */
package uk.ac.warwick.util.content.textile2.lite;

public class Content {

	private String originalContent = "";

	private String contentProcessedSoFar = "";

	private String remainingContent = "";

	public Content(String originalContent) {
		this.originalContent = originalContent;
		this.remainingContent = originalContent;
	}

	public final String getContentProcessedSoFar() {
		return contentProcessedSoFar;
	}

	public final void setContentProcessedSoFar(String contentProcessedSoFar) {
		this.contentProcessedSoFar = contentProcessedSoFar;
	}

	public final String getOriginalContent() {
		return originalContent;
	}

	public final void setOriginalContent(String originalContent) {
		this.originalContent = originalContent;
	}

	public final String getRemainingContent() {
		return remainingContent;
	}

	public final void setRemainingContent(String remainingContent) {
		this.remainingContent = remainingContent;
	}

}
