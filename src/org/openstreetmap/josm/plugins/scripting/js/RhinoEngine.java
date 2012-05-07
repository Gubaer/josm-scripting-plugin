package org.openstreetmap.josm.plugins.scripting.js;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.openstreetmap.josm.plugins.PluginException;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.scripting.util.Assert;
import org.openstreetmap.josm.plugins.scripting.util.ExceptionUtil;
import org.openstreetmap.josm.plugins.scripting.util.IOUtil;


/**
 * A facade to the embedded rhino scripting engine.
 * <p>
 * Provides methods to prepare the a script context on the Swing EDT and to evaluate script
 * in this context.
 * 
 */
public class RhinoEngine {
	static private final Logger logger = Logger.getLogger(RhinoEngine.class.getName());
	
	private Scriptable swingThreadScope;
	
	static private  RhinoEngine instance;
	public static RhinoEngine getInstance() {
		if (instance == null) instance = new RhinoEngine();
		return instance; 
	}
	
	
	private RhinoEngine(){}
		
	protected void runOnSwingEDT(Runnable r){
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else { 
			try {
				SwingUtilities.invokeAndWait(r);
			} catch(InvocationTargetException e){
				Throwable throwable = e.getCause(); 
				if (throwable instanceof Error) { 
		            throw (Error) throwable; 
		        } else if(throwable instanceof RuntimeException) { 		        	
		            throw (RuntimeException) throwable; 
		        }
		        // no other checked exceptions expected - log a warning 
		        logger.warning("Unexpected exception wrapped in InvocationTargetException: " + throwable.toString());
		        logger.warning(ExceptionUtil.stackTraceAsString(throwable));
			} catch(InterruptedException e){
				Thread.currentThread().interrupt();
			}
		}
	}
	
	protected boolean loadResource(Context ctx, String resource) {
		InputStream in = getClass().getResourceAsStream(resource);
		if (in == null) {
			System.out.println(tr("Failed to load javascript file from ressource ''{0}''. Ressource not found.", resource));
		}
		Reader reader = new InputStreamReader(in);
		try {
			ctx.evaluateReader(swingThreadScope, reader, resource, 0, null);
			return true;
		} catch(IOException e){
			System.out.println(tr("Failed to load javascript file from resource ''{0}''.", resource));
			System.out.println(tr("Exception ''{0}'' occured.", e.toString()));
			e.printStackTrace();
			return false;
		} catch(RhinoException e){
			System.out.println(tr("Failed to load javascript  file from resource ''{0}''.", resource));
			System.out.println(tr("Exception ''{0}'' occured at position {1}/{2}.", e.toString(), e.lineNumber(), e.columnNumber()));
			e.printStackTrace();
			return false;
		} finally {
			IOUtil.close(reader);
		}		
	}
	
	/**
	 * Enter a scripting context on the Swing EDT. This method has to be invoked only once. 
	 * The context is maintained by Rhino as thread local variable.
	 * 
	 * @see #exitSwingThreadContext()
	 */
	public void enterSwingThreadContext() {
		Runnable r = new Runnable() {
			public void run() {
				if (Context.getCurrentContext() != null) return;
				Context ctx = Context.enter();				
				swingThreadScope = ctx.initStandardObjects();
				if (!loadResource(ctx, "/js/require.js")) return;				
				// make sure the CommonJS module loader looks for modules in the
				// scripting plugin jar 
				try {
					PluginInformation info = PluginInformation.findPlugin("scripting");
					String url = "jar:" + info.file.toURI().toURL().toString() + "!" + "/js";
					String script = MessageFormat.format("require.addRepository(new java.net.URL(''{0}''));", url);
					ctx.evaluateString(swingThreadScope, script, "inline", 0, null);	
					System.out.println(tr("INFO: Sucessfully loaded CommonJS module loader from resource ''{0}''", "/js/require.js"));
					System.out.println(tr("INFO: Added the plugin jar as module respository. jar URL is: {0}", url.toString()));
				} catch (PluginException e) {
					e.printStackTrace();
				} catch(MalformedURLException e) {
					e.printStackTrace();
				}
			}
		};
		runOnSwingEDT(r);
	}
	
	/**
	 * Exit and discard the scripting context on the Swing EDT (if any).
	 */
	public void exitSwingThreadContext() {
		Runnable r = new Runnable() {
			public void run() {
				if (Context.getCurrentContext() == null) return;
				Context.exit();
			}
		};
		runOnSwingEDT(r);
	}
	
	/**
	 * Evaluate a script on the Swing EDT in a given scope.
	 * 
	 * @param script the script. Ignored if null.
	 * @param scope the scope for script execution. If null, creates a new context with standard objects 
	 * @throws EvaluatorException thrown if evaluating the script fails
	 */
	public void evaluateOnSwingThread(final String script, Scriptable scope)  throws EvaluatorException{
		if (script == null) return;
		final Scriptable s = scope == null ? Context.getCurrentContext().initStandardObjects() : scope;
		Runnable r = new Runnable() {
			public void run() {
				Context ctx = Context.getCurrentContext();
				if (ctx == null){
					ctx = Context.enter();
				}
				ctx.evaluateString(s, script, "inlineScript", 0, null /* no security domain */);
			}
		};
		runOnSwingEDT(r);
	}
	
	/**
	 * Evaluate a script on the Swing EDT in a standard scope for scripts run on the Swing EDT 
	 * 
	 * @param script the script 
	 */
	public void evaluateOnSwingThread(final String script) {
		if (swingThreadScope == null) {
			enterSwingThreadContext();
		}
		evaluateOnSwingThread(script, swingThreadScope);
	}

	/**
	 * Reads and evaluates the script in the file <code>file</code> on the current Swing thread.
	 * 
	 * @param file the script file. Ignored if null. Must be a readable file
	 * @throws IllegalArgumentException thrown if file is a directory
	 * @throws IllegalArgumentException thrown if file isn't readable
	 * @throws FileNotFoundException thrown if file isn't found 
	 * @throws EvaluatorException thrown if the evaluation of the script fails
	 */
	public void evaluateOnSwingThread(final File file, Scriptable scope) throws FileNotFoundException, IOException, EvaluatorException {		
		if (file == null) return;
		Assert.assertArg(!file.isDirectory(), "Can''t read script from a directory ''{0}''", file);
		Assert.assertArg(file.canRead(), "Can''t read script from file, because file isn''t readable. Got file ''{0}''", file);
		Reader reader = null;
		try {
			final Reader fr = new FileReader(file);
			enterSwingThreadContext();
			final Scriptable s = scope == null? Context.getCurrentContext().initStandardObjects() : scope;
			Runnable r = new Runnable() {
				public void run() {
					try {
						Context.getCurrentContext().evaluateReader(s, fr, file.toString(), 0, null /* no security domain */);
					} catch(IOException e){
						throw new RuntimeException(e);
					}
				}
			};
			try {		
				runOnSwingEDT(r);
			} catch(RuntimeException e) {
				// unwrapping IO exception thrown from the runnable
				if (e.getCause() != null && e.getCause() instanceof IOException) {
					throw (IOException)e.getCause();
				} 
				throw e;
			}
		} finally {
			if (reader != null) IOUtil.close(reader);
		}
	}
}
