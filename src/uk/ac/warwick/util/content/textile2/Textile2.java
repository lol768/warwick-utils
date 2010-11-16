package uk.ac.warwick.util.content.textile2;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public static final EnumSet<TransformationOptions> DEFAULT_OPTIONS = 
		EnumSet.noneOf(TransformationOptions.class);
	
	private final EnumSet<TransformerFeature> features;
	
	private final EnumSet<TransformationOptions> options;

	private TextTransformer transformer;

	/**
	 * Set up the transformers
	 * 
	 */
	public Textile2() {
		this(null, DEFAULT_FEATURESET, DEFAULT_OPTIONS);
	}
	
	public Textile2(EnumSet<TransformerFeature> features) {
		this(null, features, DEFAULT_OPTIONS);
	}
	
	public Textile2(EnumSet<TransformerFeature> features, EnumSet<TransformationOptions> options) {
		this(null, features, options);
	}
	
	public Textile2(boolean addNoFollow) {
		this(addNoFollow, DEFAULT_OPTIONS);
	}

	public Textile2(boolean addNoFollow, EnumSet<TransformationOptions> options) {
		EnumSet<TransformerFeature> theFeatures = EnumSet.copyOf(DEFAULT_FEATURESET);
		
		if (addNoFollow) {
			theFeatures.add(TransformerFeature.noFollowLinks);
		}
		
		this.features = theFeatures;
		this.options = options == null ? DEFAULT_OPTIONS : options;
		
		setupTransformers(null);
	}

	/**
	 * Set up the transformers
	 * 
	 */
	public Textile2(String textile2ServiceLocation) {
		this(textile2ServiceLocation, DEFAULT_FEATURESET, DEFAULT_OPTIONS);
	}

	public Textile2(String textile2ServiceLocation, EnumSet<TransformerFeature> features, EnumSet<TransformationOptions> options) {
		this.features = features == null ? DEFAULT_FEATURESET : features;
		this.options = options == null ? DEFAULT_OPTIONS : options;
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
			TextileConfiguration config = TextileConfiguration.getInstance();

			Map<String, MediaUrlHandler> mediaHandlers = new HashMap<String, MediaUrlHandler>();
			mediaHandlers.put("audio", new AudioMediaUrlHandler(config.getWimpyPlayerLocation(), config.getAlternativeMp3PlayerLocation(), options));
			mediaHandlers.put("google", new GoogleMediaUrlHandler());
			mediaHandlers.put("youtube", new YouTubeMediaUrlHandler());
			mediaHandlers.put("quicktime", new QuickTimeMediaUrlHandler(config.getQtPreviewImage()));
			mediaHandlers.put("avi", new AviMediaUrlHandler(config.getWmPreviewImage()));
			mediaHandlers.put("flv", new FlvMediaUrlHandler(config.getFlvPlayerLocation(), config.getNewFlvPlayerLocation()));
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
		
			transformers.add(new MediaUrlTransformer(mediaHandlers, config.getCloseButtonImgUrl()));
		}
		
		if (features.contains(TransformerFeature.latex)) {
			transformers.add(new LatexTextTransformer(TextileConfiguration.getInstance().getLatexLocation()));
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
