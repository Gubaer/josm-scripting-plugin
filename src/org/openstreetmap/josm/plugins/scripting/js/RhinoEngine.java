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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.openstreetmap.josm.plugins.PluginException;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.scripting.js.wrapper.JSMixinRegistry;
import org.openstreetmap.josm.plugins.scripting.js.wrapper.MixinWrapFactory;
import org.openstreetmap.josm.plugins.scripting.js.wrapper.WrappingException;
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
	
	/**
	 * A thread local storage for root scopes. In particular, holds the
	 * root scope for scripts run on the Swing EDT.
	 */
	private static ThreadLocal<Scriptable> scope = new ThreadLocal<Scriptable>(); 
	
	static public Scriptable getRootScope() {
		return scope.get();
	}
	
	static private  RhinoEngine instance;
	public static RhinoEngine getInstance() {
		if (instance == null) instance = new RhinoEngine();
		return instance; 
	}
	
	static public void loadJSMixins(Context ctx, Scriptable scope) {
		String script = "require('josm/mixin/Mixins').mixins;";
		Object o = ctx.evaluateString(scope, script, "fragment: loading list of mixins", 0, null);
		if (o instanceof NativeArray) {
			Object[] modules = ((NativeArray)o).toArray();
			for (int i = 0; i< modules.length; i++){
				Object m = modules[i];
				if (! (m instanceof String)) continue;
				try {
					JSMixinRegistry.loadJSPrototype(scope, (String)m);
				} catch(WrappingException e){
					logger.log(Level.SEVERE, MessageFormat.format("Failed to load mixin module ''{0}''.", m), e);
					continue;
				}
				logger.info(MessageFormat.format("Successfully loaded mixin module ''{0}''", m));				
			}
		} else {
			logger.warning(MessageFormat.format(
			  "Property ''{0}''' exported by module ''{1}'' should be a NativeArray, got {2} instead",
			  "mixin", "josm/mixin/Mixins", o
			));
		}
	}
		
	/**
	 * <p>Initializes a standard scope for scripts running in the context of a JOSM instance.</p>
	 * 
	 * <p>Initialized the context with the standard Rhino context, loads 
	 * <code>require.js</code> from the scripting jar and initializes the
	 * CommonJS module loader.</p>
	 * 
	 * @param ctx the context 
	 * @return the initialized scope 
	 */
	static public Scriptable initStandardScope(Context ctx, String resRequire) {
		Scriptable scope  = ctx.initStandardObjects();
		if (!loadResource(ctx, scope, resRequire)) return scope;				
		// make sure the CommonJS module loader looks for modules in the
		// scripting plugin jar 
		try {
			PluginInformation info = PluginInformation.findPlugin("scripting");
			if (info != null) {
				String url = "jar:" + info.file.toURI().toURL().toString() + "!" + "/js";
				String script = MessageFormat.format("require.addRepository(new java.net.URL(''{0}''));", url);
				ctx.evaluateString(scope, script, "fragment: adding default repository", 0, null);	
				logger.info(tr("Sucessfully loaded CommonJS module loader from resource ''{0}''", "/js/require.js"));
				logger.info(tr("Added the plugin jar as module respository. jar URL is: {0}", url.toString()));
				loadJSMixins(ctx, scope);
				script = "var josm=require('josm');";
				ctx.evaluateString(scope, script, "fragment: loading module 'josm'", 1, null);
			} else {
				logger.warning("Plugin information for plugin 'scripting' not found. Failed to initialize CommonJS module loader with path.");
			}
		} catch (PluginException e) {
			e.printStackTrace();
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}
		return scope;
	}
	
	static public Scriptable initStandardScope(Context ctx) {
		return initStandardScope(ctx, "/js/require.js");
	}
	
	static protected boolean loadResource(Context ctx, Scriptable scope, String resource) {
		InputStream in = RhinoEngine.class.getResourceAsStream(resource);
		if (in == null) {
			logger.log(Level.SEVERE,tr("Failed to load javascript file from ressource ''{0}''. Ressource not found.", resource));
		}
		Reader reader = new InputStreamReader(in);
		try {
			ctx.evaluateReader(scope, reader, resource, 1, null);
			return true;
		} catch(IOException e){
			logger.log(Level.SEVERE, tr("Failed to load javascript file from resource ''{0}''.", resource));
			logger.log(Level.SEVERE, tr("Exception ''{0}'' occured.", e.toString()), e);
			return false;
		} catch(RhinoException e){
			logger.log(Level.SEVERE, tr("Failed to load javascript  file from resource ''{0}''.", resource));
			logger.log(Level.SEVERE,tr("Exception ''{0}'' occured at position {1}/{2}.", e.toString(), e.lineNumber(), e.columnNumber()), e);
			return false;
		} finally {
			IOUtil.close(reader);
		}		
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
				ctx.setWrapFactory(new MixinWrapFactory());
				scope.set(initStandardScope(ctx));
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
				scope.remove();
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
		if (script == null) return;
		Runnable r = new Runnable() {
			public void run() {
				enterSwingThreadContext();
				Context ctx = Context.getCurrentContext();
				ctx.evaluateString(RhinoEngine.scope.get(), script, "inlineScript", 1, null /* no security domain */);
			}
		};
		runOnSwingEDT(r);
	}

	/**
	 * Reads and evaluates the script in the file <code>file</code> on the current Swing thread.
	 * 
	 * @param file the script file. Ignored if null. Must be a readable file
	 * @param scope the scope. If null, creates a new scope. In any case, sets the standard scope for the Swing thread as
	 * parent scope.
	 * @throws IllegalArgumentException thrown if file is a directory
	 * @throws IllegalArgumentException thrown if file isn't readable
	 * @throws FileNotFoundException thrown if file isn't found 
	 * @throws EvaluatorException thrown if the evaluation of the script fails
	 */
	public void evaluateOnSwingThread(final File file, final Scriptable scope) throws FileNotFoundException, IOException, EvaluatorException {		
		if (file == null) return;
		Assert.assertArg(!file.isDirectory(), "Can''t read script from a directory ''{0}''", file);
		Assert.assertArg(file.canRead(), "Can''t read script from file, because file isn''t readable. Got file ''{0}''", file);
		Reader reader = null;
		try {
			final Reader fr = new FileReader(file);
			enterSwingThreadContext();
			Runnable r = new Runnable() {
				public void run() {
					try {
						Scriptable s = (scope == null) ? new NativeObject() : scope; 
						s.setParentScope(RhinoEngine.scope.get());
						Context.getCurrentContext().evaluateReader(s, fr, file.toString(), 1, null /* no security domain */);
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
			IOUtil.close(reader);
		}
	}

	/**
	 * <p>Loads a CommonJS module and populates the variables <code>exports</code> and <code>module</code>.</p>
	 * 
	 * <p>This method is used in require.js to load CommonJS module. This way, Rhino keeps a reference from
	 * the compiled script to the module file name given by <code>moduleLocation</code>. 
	 * 
	 * @param moduleLocation the module location, i.e. a file name 
	 * @param moduleContent the module content, i.e. the javascript source for the module 
	 * @param require a reference to the require function 
	 * @param exports the "exports" variable populated by a CommonJS module
	 * @param module the "module" variable populated by a CommonJS module 
	 */
	static public void compileModule(String moduleLocation, String moduleContent, Scriptable require, Scriptable exports, Scriptable module) {
		Context ctx = Context.getCurrentContext();		
		Scriptable scope = new NativeObject();		
		Scriptable parentScope = RhinoEngine.scope.get();
		if (parentScope == null) {
			parentScope = ctx.initStandardObjects();
		}		
		scope.setParentScope(parentScope);
		scope.put("require", scope, require);
		scope.put("exports", scope, exports);
		scope.put("module", scope, module);
		ctx.evaluateString(scope, moduleContent, moduleLocation, 1, null /* no security domain */);
	}
}
