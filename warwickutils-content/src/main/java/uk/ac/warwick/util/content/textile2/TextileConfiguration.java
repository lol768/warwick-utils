package uk.ac.warwick.util.content.textile2;

public final class TextileConfiguration {
	
	private static TextileConfiguration INSTANCE;
	
	public synchronized static void init() {
		if (INSTANCE == null) {
			if (!(
					System.getProperty("textile.media.mp3WimpyPlayerLocation") != null
		  		 || System.getProperty("textile.media.mp3AlternatePlayerLocation") != null)) {
			        	throw new IllegalStateException("Properties textile.media.mp3WimpyPlayerLocation and " +
				"textile.media.mp3AlternatePlayerLocation are both not set");
			}
			
			if (System.getProperty("textile.media.quicktimePreviewImage") == null) {
				throw new IllegalStateException("textile.media.quicktimePreviewImage is not set");
			}
			
			if (System.getProperty("textile.media.windowsMediaPreviewImage") == null) {
				throw new IllegalStateException("textile.media.windowsMediaPreviewImage is not set");
			}
			
			if (System.getProperty("textile.media.flvPlayerLocation") == null && System.getProperty("textile.media.newFlvPlayerLocation") == null) {
				throw new IllegalStateException("textile.media.flvPlayerLocation is not set");
			}
			
			if (System.getProperty("textile.latex.location") == null) {
				throw new IllegalStateException("textile.latex.location is not set");
			}
			
			TextileConfiguration config = new TextileConfiguration();
			config.setWimpyPlayerLocation(System.getProperty("textile.media.mp3WimpyPlayerLocation"));
			config.setAlternativeMp3PlayerLocation(System.getProperty("textile.media.mp3AlternatePlayerLocation"));
			config.setQtPreviewImage(System.getProperty("textile.media.quicktimePreviewImage"));
			config.setWmPreviewImage(System.getProperty("textile.media.windowsMediaPreviewImage"));
			config.setFlvPlayerLocation(System.getProperty("textile.media.flvPlayerLocation"));
			config.setNewFlvPlayerLocation(System.getProperty("textile.media.newFlvPlayerLocation"));
			config.setLatexLocation(System.getProperty("textile.latex.location"));
			config.setCloseButtonImgUrl(System.getProperty("textile.popup.closeButtonUrl"));
			
			TextileConfiguration.setInstance(config);
		}
	}
	
	public synchronized static TextileConfiguration getInstance() {
		if (INSTANCE == null) {
			init();
		}
		
		return INSTANCE;
	} 
	
	public synchronized static void setInstance(TextileConfiguration configuration) {
		INSTANCE = configuration;
	}
	
	private String wimpyPlayerLocation;
	private String alternativeMp3PlayerLocation;
	private String qtPreviewImage;
	private String wmPreviewImage;
	private String flvPlayerLocation;
	private String newFlvPlayerLocation;
	private String latexLocation;
	private String closeButtonImgUrl;
	
	public TextileConfiguration() {}

	public String getWimpyPlayerLocation() {
		return wimpyPlayerLocation;
	}

	public void setWimpyPlayerLocation(String wimpyPlayerLocation) {
		this.wimpyPlayerLocation = wimpyPlayerLocation;
	}

	public String getAlternativeMp3PlayerLocation() {
		return alternativeMp3PlayerLocation;
	}

	public void setAlternativeMp3PlayerLocation(
			String alternativeMp3PlayerLocation) {
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
