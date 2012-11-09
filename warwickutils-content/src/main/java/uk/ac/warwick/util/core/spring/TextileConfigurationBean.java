package uk.ac.warwick.util.core.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import uk.ac.warwick.util.content.textile2.TextileConfiguration;

public final class TextileConfigurationBean extends AbstractFactoryBean {
	
	private String wimpyPlayerLocation;
	private String alternativeMp3PlayerLocation;
	private String qtPreviewImage;
	private String wmPreviewImage;
	private String flvPlayerLocation;
	private String newFlvPlayerLocation;
	private String latexLocation;
	private String closeButtonImgUrl;

	protected TextileConfiguration createInstance() throws Exception {
		TextileConfiguration config = new TextileConfiguration();
		
		config.setWimpyPlayerLocation(wimpyPlayerLocation);
		config.setAlternativeMp3PlayerLocation(alternativeMp3PlayerLocation);
		config.setQtPreviewImage(qtPreviewImage);
		config.setWmPreviewImage(wmPreviewImage);
		config.setFlvPlayerLocation(flvPlayerLocation);
		config.setNewFlvPlayerLocation(newFlvPlayerLocation);
		config.setLatexLocation(latexLocation);
		config.setCloseButtonImgUrl(closeButtonImgUrl);
		
		TextileConfiguration.setInstance(config);
		
		return TextileConfiguration.getInstance();
	}

	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return TextileConfiguration.class;
	}

	public String getWimpyPlayerLocation() {
		return wimpyPlayerLocation;
	}

	public void setWimpyPlayerLocation(String wimpyPlayerLocation) {
		this.wimpyPlayerLocation = wimpyPlayerLocation;
	}

	public String getAlternativeMp3PlayerLocation() {
		return alternativeMp3PlayerLocation;
	}

	public void setAlternativeMp3PlayerLocation(String alternativeMp3PlayerLocation) {
		this.alternativeMp3PlayerLocation = alternativeMp3PlayerLocation;
	}

	public String getQtPreviewImage() {
		return qtPreviewImage;
	}

	public void setQtPreviewImage(String qtPreviewImage) {
		this.qtPreviewImage = qtPreviewImage;
	}

	public String getWmPreviewImage() {
		return wmPreviewImage;
	}

	public void setWmPreviewImage(String wmPreviewImage) {
		this.wmPreviewImage = wmPreviewImage;
	}

	public String getFlvPlayerLocation() {
		return flvPlayerLocation;
	}

	public void setFlvPlayerLocation(String flvPlayerLocation) {
		this.flvPlayerLocation = flvPlayerLocation;
	}

	public String getNewFlvPlayerLocation() {
		return newFlvPlayerLocation;
	}

	public void setNewFlvPlayerLocation(String newFlvPlayerLocation) {
		this.newFlvPlayerLocation = newFlvPlayerLocation;
	}

	public String getLatexLocation() {
		return latexLocation;
	}

	public void setLatexLocation(String latexLocation) {
		this.latexLocation = latexLocation;
	}

    public String getCloseButtonImgUrl() {
        return closeButtonImgUrl;
    }

    public void setCloseButtonImgUrl(String closeButtonImgUrl) {
        this.closeButtonImgUrl = closeButtonImgUrl;
    }

}
