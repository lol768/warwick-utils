package uk.ac.warwick.util.content.textile2.jruby;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.texttransformers.TextTransformer;


/**
 * JRuby implementation of Redcloth textile text transformer
 * 
 * @author Mat
 */
public final class JRubyTextileTextTransformer implements TextTransformer {
    
    private static JRubyTextileTextTransformer INSTANCE;
    
    private static final Logger LOGGER = Logger.getLogger(JRubyTextileTextTransformer.class);
    
    static {
        try {
            INSTANCE = new JRubyTextileTextTransformer();
        } catch (IOException e) {
            INSTANCE = null;
            LOGGER.fatal("Could not instantiate JRuby server", e);
        }
    }
    
    public static JRubyTextileTextTransformer getInstance() {
        return INSTANCE;
    }
    
    private boolean hardBreaks;
    
    private final TextileService service;

	private JRubyTextileTextTransformer() throws IOException {	    
	    final Ruby runtime = Ruby.newInstance();
	    
	    // Read the file into a String to be evaluated by JRuby
	    InputStream redclothFileInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("redcloth_jruby.rb");
	    if (redclothFileInputStream == null) {
	        throw new IllegalStateException("Could not find redcloth source");
	    }
	    
        StringBuffer outputBuffer = new StringBuffer();
        byte[] bytes = new byte[4096];
        for (int n; (n = redclothFileInputStream.read(bytes)) != -1;) {
            outputBuffer.append(new String(bytes, 0, n));
        }
        
        // Evaluate the script in the current scope
        runtime.evalScriptlet(outputBuffer.toString());
        
        // Create a new Ruby object
        String expr = "RedClothTextileEngine.new";
        final IRubyObject rawRubyObject = runtime.evalScriptlet(expr);
        
        // Retrieve the object and cast it as a concrete implementation of TextileService
        service = (TextileService) JavaEmbedUtils.rubyToJava(runtime, rawRubyObject, TextileService.class);
	}

	public MutableContent apply(final MutableContent mc) {
	    String text = mc.getContent();
		text = service.textileToHtml(text, hardBreaks);
		mc.setContent(text);
		return mc;
	}

    public boolean isHardBreaks() {
        return hardBreaks;
    }

    public void setHardBreaks(boolean hardBreaks) {
        this.hardBreaks = hardBreaks;
    }
}
