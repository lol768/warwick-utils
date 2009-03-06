package uk.ac.warwick.util.ant;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.tools.ant.filters.TokenFilter;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JarClassLoader;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class YUICompressorFilter implements TokenFilter.Filter {
	
	private boolean disableOptimizations = false;
	private boolean verbose = false;
	private boolean munge = true;
	private boolean preserveAllSemiColons = false;
	private int linebreak = 0;

	public YUICompressorFilter() {
		
	}
	
	public String filter(String text) {
		/*
		 * This method borrows the classloader trickery used in the command-line version
		 * of the compressor. To get around issues when the original Rhino happens to be
		 * in the classpath, this alternate classloader will explicitly check for the Rhino
		 * classes within its own JAR first.
		 * 
		 * I set the original classloader back after the compressor is done. Since it is
		 * thread local, there shouldn't be any side effects for other code.
		 */
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			ClassLoader loader = new JarClassLoader();
	        Thread.currentThread().setContextClassLoader(loader);
			JavaScriptCompressor compressor = new JavaScriptCompressor(new StringReader(text), new ErrorPrinter());
			StringWriter out = new StringWriter();
			compressor.compress(out, linebreak , munge, verbose, preserveAllSemiColons, disableOptimizations);
			return out.toString();
		} catch (EvaluatorException e) {
			throw new RuntimeException("Error evaluating scripts", e);
		} catch (IOException e) {
			throw new RuntimeException("Error reading scripts", e);
		} finally {
			//Restore the original class loader.
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	
	public boolean isDisableOptimizations() {
		return disableOptimizations;
	}

	public void setDisableOptimizations(boolean disableOptimizations) {
		this.disableOptimizations = disableOptimizations;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isMunge() {
		return munge;
	}

	public void setMunge(boolean munge) {
		this.munge = munge;
	}

	public boolean isPreserveAllSemiColons() {
		return preserveAllSemiColons;
	}

	public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
		this.preserveAllSemiColons = preserveAllSemiColons;
	}

	public int getLinebreak() {
		return linebreak;
	}

	/**
	 * Suggests the maximum length of lines. -1 means to 
	 * avoid newlines where possible. 0 (default) means to newline after every
	 * semicolon. Setting it to 1000 would break after about 1000 characters
	 * (at a suitable point in the code of course).
	 */
	public void setLinebreak(int linebreak) {
		this.linebreak = linebreak;
	}


	private static class ErrorPrinter implements ErrorReporter {
        public void warning(String message, String sourceName,
                int line, String lineSource, int lineOffset) {
            if (line < 0) {
            	System.err.println("\n[WARN] " + message);
            } else {
            	System.err.println("\n[WARN] " + line + ':' + lineOffset + ':' + message);
            }
        }

        public void error(String message, String sourceName,
                int line, String lineSource, int lineOffset) {
            if (line < 0) {
                System.err.println("\n[ERROR] " + message);
            } else {
            	System.err.println("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
            }
        }

        public EvaluatorException runtimeError(String message, String sourceName,
                int line, String lineSource, int lineOffset) {
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message);
        }
    }
}
