package org.mozilla.javascript.tools.shell;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.SecurityController;
import org.mozilla.javascript.tools.ToolErrorReporter;

import uk.ac.warwick.util.ant.ShrinkSafeFilter;

/**
 * Facade for the Rhino-based ShrinkSafe. All you really need
 * to think about is the compress() method.
 * 
 * Based very loosely on the ..shell.Main class provided with Rhino.
 * 
 * In fact, it's better if you only used {@link ShrinkSafeFilter}
 * since that's in the warwick package and is less prone to change.
 * Don't use this class directly.
 * 
 * @author cusebr
 */
public class ShrinkSafe {

	public static ShellContextFactory shellContextFactory = new ShellContextFactory();
	private static final int EXITCODE_RUNTIME_ERROR = 3;
	private static SecurityProxy securityImpl;
	private ToolErrorReporter errorReporter;
	
	private boolean interpreted;


	static {
		//TODO is this necessary for us? I'm really not sure.
		initJavaPolicySecuritySupport();
	}

	public String compress(String script) {
		try {
			Context context = Context.enter();
			if (interpreted) {
				context.setOptimizationLevel(-1);
			}
			Scriptable scope = context.initStandardObjects();
			return compressScript(context, scope, script, null);
		} finally {
			Context.exit();
		}
	}

	/**
	 * Compile script from a string, and then compress it.
	 */
	private String compressScript(Context cx, Scriptable scope, String source,
			Object securityDomain) {
		String path = "<command>"; // made-up filename.
		Script script = loadScriptFromSource(cx, source, path, 1, securityDomain);
		String compressedSource = null;
		try {
			if (script != null) {
				compressedSource = cx.compressReader(scope, script, source, path, 1, securityDomain);
			} else {
				compressedSource = source;
			}
		} catch (VirtualMachineError ex) {
			// Treat StackOverflow and OutOfMemory as runtime errors
			ex.printStackTrace();
			String msg = ToolErrorReporter.getMessage(
					"msg.uncaughtJSException", ex.toString());
			Context.reportError(msg);
		}
		return compressedSource;
	}

	private Script loadScriptFromSource(Context cx, String scriptSource,
			String path, int lineno, Object securityDomain) {
		try {
			return cx.compileString(scriptSource, path, lineno, securityDomain);
		} catch (EvaluatorException ee) {
			// Already printed message.
		} catch (RhinoException rex) {
			ToolErrorReporter.reportException(cx.getErrorReporter(), rex);
		} catch (VirtualMachineError ex) {
			// Treat StackOverflow and OutOfMemory as runtime errors
			ex.printStackTrace();
			String msg = ToolErrorReporter.getMessage(
					"msg.uncaughtJSException", ex.toString());
			Context.reportError(msg);
		}
		return null;
	}
	
	public boolean isInterpreted() {
		return interpreted;
	}

	/**
	 * If set to true, optimization level will be set to -1
 	 * which should disable compilation and just interpret
 	 * the script directly.
	 */
	public void setInterpreted(boolean interpreted) {
		this.interpreted = interpreted;
	}

	/**
	 * Not entirely sure whether this is necessary, but I'll leave it in.
	 */
	private static void initJavaPolicySecuritySupport() {
		Throwable exObj;
		try {
			Class<?> cl = Class
					.forName("org.mozilla.javascript.tools.shell.JavaPolicySecurity");
			securityImpl = (SecurityProxy) cl.newInstance();
			SecurityController.initGlobal(securityImpl);
			return;
		} catch (ClassNotFoundException ex) {
			exObj = ex;
		} catch (IllegalAccessException ex) {
			exObj = ex;
		} catch (InstantiationException ex) {
			exObj = ex;
		} catch (LinkageError ex) {
			exObj = ex;
		}
		throw Kit.initCause(new IllegalStateException(
				"Can not load security support: " + exObj), exObj);
	}

}
