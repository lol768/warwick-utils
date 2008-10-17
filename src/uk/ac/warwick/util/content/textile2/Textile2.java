package uk.ac.warwick.util.content.textile2;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.warwick.util.content.textile2.TextileTextTransformer;
import uk.ac.warwick.util.content.textile2.TransformerFeature;
import uk.ac.warwick.util.content.textile2.jruby.JRubyTextileTextTransformer;
import uk.ac.warwick.util.content.texttransformers.BadLinkRemovingTransformer;
import uk.ac.warwick.util.content.texttransformers.CompositeTextTransformer;
import uk.ac.warwick.util.content.texttransformers.CustomEscapingTransformer;
import uk.ac.warwick.util.content.texttransformers.EntityConvertingTransformer;
import uk.ac.warwick.util.content.texttransformers.EscapeHtmlCommentsTransformer;
import uk.ac.warwick.util.content.texttransformers.EscapeScriptTagsTransformer;
import uk.ac.warwick.util.content.texttransformers.LatexTextTransformer;
import uk.ac.warwick.util.content.texttransformers.NoFollowLinkTransformer;
import uk.ac.warwick.util.content.texttransformers.TextTransformer;
import uk.ac.warwick.util.content.texttransformers.TidyLineBreaksTransformer;
import uk.ac.warwick.util.content.texttransformers.media.AudioMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.AviMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.EyespotMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.FlvMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.GoogleMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.GrouperMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.GubaMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.IFilmMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.JumpcutMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.MediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.MediaUrlTransformer;
import uk.ac.warwick.util.content.texttransformers.media.MetacafeMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.MySpaceMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.QuickTimeMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.RevverMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.SelfcastTVMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.StandardFlashMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.VimeoMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.YouTubeMediaUrlHandler;

/**
 * Text transformer which sends the text to a remote Textile service, and
 * retrieves the response.
 * 
 * @author Mat Mannion
 */
public final class Textile2 {
	
	public static final EnumSet<TransformerFeature> DEFAULT_FEATURESET = EnumSet.of(
			TransformerFeature.backslashes, 
			TransformerFeature.latex,
			TransformerFeature.media, 
			TransformerFeature.textilise, 
			TransformerFeature.removeJsLinks
		);
	
	private final EnumSet<TransformerFeature> features;

	private TextTransformer transformer;

	/**
	 * Set up the transformers
	 * 
	 */
	public Textile2() {
		this(null, DEFAULT_FEATURESET);
	}
	
	public Textile2(EnumSet<TransformerFeature> features) {
		this(null, features);
	}

	public Textile2(boolean addNoFollow) {
		EnumSet<TransformerFeature> features = EnumSet.copyOf(DEFAULT_FEATURESET);
		
		if (addNoFollow) {
			features.add(TransformerFeature.noFollowLinks);
		}
		
		this.features = features;
		
		setupTransformers(null);
	}

	/**
	 * Set up the transformers
	 * 
	 */
	public Textile2(String textile2ServiceLocation) {
		this(textile2ServiceLocation, DEFAULT_FEATURESET);
	}

	public Textile2(String textile2ServiceLocation, EnumSet<TransformerFeature> features) {
		this.features = features;
		setupTransformers(textile2ServiceLocation);
	}

	/**
	 * @param textile2ServiceLocation
	 */
	private void setupTransformers(String textile2ServiceLocation) {
		List<TextTransformer> transformers = new ArrayList<TextTransformer>();

		// preprocess
		transformers.add(new TidyLineBreaksTransformer());
		transformers.add(new EscapeHtmlCommentsTransformer());
		transformers.add(new EscapeScriptTagsTransformer());
		
		if (features.contains(TransformerFeature.backslashes)) {
			transformers.add(new CustomEscapingTransformer());
		}
		
		transformers.add(new EntityConvertingTransformer());
		
		if (features.contains(TransformerFeature.media)) {
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
			
			if (System.getProperty("textile.media.flvPlayerLocation") == null) {
				throw new IllegalStateException("textile.media.flvPlayerLocation is not set");
			}

			Map<String, MediaUrlHandler> mediaHandlers = new HashMap<String, MediaUrlHandler>();
			mediaHandlers.put("audio", new AudioMediaUrlHandler(System
					.getProperty("textile.media.mp3WimpyPlayerLocation"), System
					.getProperty("textile.media.mp3AlternatePlayerLocation")));
			mediaHandlers.put("google", new GoogleMediaUrlHandler());
			mediaHandlers.put("youtube", new YouTubeMediaUrlHandler());
			mediaHandlers.put("quicktime", new QuickTimeMediaUrlHandler(System
					.getProperty("textile.media.quicktimePreviewImage")));
			mediaHandlers.put("avi", new AviMediaUrlHandler(System
					.getProperty("textile.media.windowsMediaPreviewImage")));
			mediaHandlers.put("flv", new FlvMediaUrlHandler(System
					.getProperty("textile.media.flvPlayerLocation")));
			mediaHandlers.put("flash", new StandardFlashMediaUrlHandler());
			mediaHandlers.put("revver", new RevverMediaUrlHandler());
			mediaHandlers.put("metacafe", new MetacafeMediaUrlHandler());
			mediaHandlers.put("jumpcut", new JumpcutMediaUrlHandler());
			mediaHandlers.put("guba", new GubaMediaUrlHandler());
			mediaHandlers.put("ifilm", new IFilmMediaUrlHandler());
			mediaHandlers.put("selfcasttv", new SelfcastTVMediaUrlHandler());
			mediaHandlers.put("grouper", new GrouperMediaUrlHandler());
			mediaHandlers.put("eyespot", new EyespotMediaUrlHandler());
			mediaHandlers.put("vimeo", new VimeoMediaUrlHandler());
			mediaHandlers.put("myspace", new MySpaceMediaUrlHandler());
		
			transformers.add(new MediaUrlTransformer(mediaHandlers));
		}
		
		if (features.contains(TransformerFeature.latex)) {
			if (System.getProperty("textile.latex.location") == null) {
				throw new IllegalStateException("textile.latex.location is not set");
			}
			
			transformers.add(new LatexTextTransformer(System.getProperty("textile.latex.location")));
		}

		if (features.contains(TransformerFeature.textilise)) {
			// process
			JRubyTextileTextTransformer jrubyTransformer = JRubyTextileTextTransformer
					.getInstance();
	
			if (jrubyTransformer == null) {
				// handle being unable to instantiate jruby
				transformers.add(new TextileTextTransformer(textile2ServiceLocation));
			} else {
				jrubyTransformer.setHardBreaks(true);
				transformers.add(jrubyTransformer);
			}
		}

		// postprocess
		if (features.contains(TransformerFeature.removeJsLinks)) {
			transformers.add(new BadLinkRemovingTransformer());
		}

		if (features.contains(TransformerFeature.noFollowLinks)) {
			transformers.add(new NoFollowLinkTransformer());
		}

		transformer = new CompositeTextTransformer(transformers);
	}

	public String process(String content) {
		return transformer.transform(content);
	}

}
