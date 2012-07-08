package org.openstreetmap.josm.plugins.scripting.js;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.Require;
import org.openstreetmap.josm.plugins.PluginException;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin;
import org.openstreetmap.josm.plugins.scripting.util.Assert;
import org.openstreetmap.josm.plugins.scripting.util.ExceptionUtil;
import org.openstreetmap.josm.plugins.scripting.util.IOUtil;

/**
 * A facade to the embedded rhino scripting engine.
 */
public class RhinoEngine {
	static private final Logger logger = Logger.getLogger(RhinoEngine.class.getName());
		
	static private  RhinoEngine instance;
	public static RhinoEngine getInstance() {
		if (instance == null) instance = new RhinoEngine();
		return instance; 
	}

	/**
	 * The one and only scope for all scripting contexts in JOSM.
	 * Currently only used for scripts run on the Swing EDT. 
	 */
	private Scriptable scope = null;
	
	private Require require;
	
	public Scriptable getScope() {
		return scope;
	}
	
	public Scriptable require(String moduleId) {
		moduleId = JOSMModuleScriptProvider.normalizeModuleId(moduleId);
		Object module = require.call(Context.getCurrentContext(), scope, null, new Object[]{moduleId});
		return (Scriptable)module;
	}
	
	protected void loadJSMixins(Context ctx, Scriptable scope) {
		Scriptable module = (Scriptable)require.call(ctx, scope, null, new Object[]{"josm/mixin/Mixins"});
		Object o = module.get("mixins", module);
		if (o instanceof NativeArray) {
			Object[] modules = ((NativeArray)o).toArray();
			for (int i = 0; i< modules.length; i++){
				Object m = modules[i];
				if (! (m instanceof String)) continue;
				try {
					JSMixinRegistry.loadJSMixin(scope, (String)m);
				} catch(JSMixinException e){
					logger.log(Level.SEVERE, MessageFormat.format("Failed to load mixin module ''{0}''.", m), e);
					continue;
				}
				logger.info(MessageFormat.format("Successfully loaded mixin module ''{0}''", m));				
			}
		} else {
			logger.warning(MessageFormat.format(
			  "Property ''{0}'' exported by module ''{1}'' should be a NativeArray, got {2} instead",
			  "mixin", "josm/mixin/Mixins", o
			));
		}
	}
	
	protected URL buildRepositoryUrlForBuiltinModules() {
		try {
			PluginInformation info = PluginInformation.findPlugin("scripting");
			if (info != null) {
				return new URL(String.format("jar:%s!/js", info.file.toURI().toURL()));
			} else {
				logger.warning("Plugin information for plugin 'scripting' not found. Failed to initialize CommonJS module loader with path.");
			}
			
		} catch (PluginException e) {
			logger.log(Level.WARNING, "Failed to lookup plugin information for plugin 'scripting'. Cannot load CommonJS modules from the plugin jar.",e);
			e.printStackTrace();
		} catch(MalformedURLException e) {
			logger.log(Level.WARNING, "Failed to create URL referring to the CommonJS modules in the plugin jar. Cannot load CommonJS mdodules from the plugin jar.",e);
			e.printStackTrace();
		}
		return null;
	}
		
	/**
	 * <p>Initializes a standard scope for scripts running in the context of a JOSM instance.</p>
	 * 
	 */
	public void initScope() {
		if (scope != null) return; 
		Context ctx = Context.getCurrentContext();
		if (ctx == null) ctx = Context.enter();
		scope = ctx.initStandardObjects();
		
		JOSMModuleScriptProvider provider = JOSMModuleScriptProvider.getInstance();
		URL pluginJSURL = buildRepositoryUrlForBuiltinModules();
		if (pluginJSURL != null) {
			provider.addRepository(pluginJSURL);
		}
		
		// add the $PLUGIN_HOME/modules to the list of module repositories
		//
		String dir = ScriptingPlugin.getInstance().getPluginDir();
		File f = new File(new File(dir), "modules");
		try {
			provider.addRepository(f.toURI().toURL());
		} catch(MalformedURLException e) {
			logger.log(Level.WARNING, tr("Failed to create URL referring to the module repository ''{0}''", f),e);
		}
		// create and install the require function
		require = new Require(ctx,scope, JOSMModuleScriptProvider.getInstance(), null, null, false);
		require.install(scope);
		
		// load mixin classes 
		loadJSMixins(ctx, scope);
		
		// load the main josm scope
		Object josm = require.call(ctx, scope, null, new Object[]{"josm"});
		scope.put("josm",scope, josm);
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
				Context ctx = Context.getCurrentContext();
				if (ctx == null) {
					ctx = Context.enter();
					ctx.setWrapFactory(new MixinWrapFactory());
				}
				initScope();				
			}
		};
		runOnSwingEDT(r);
	}
	
	/**
	 * Exit the context used on the Swing EDT.
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
	 * Evaluate a script on the Swing EDT
	 * 
	 * @param script the script 
	 */
	public void evaluateOnSwingThread(final String script) {
		if (script == null) return;
		Runnable r = new Runnable() {
			public void run() {
				enterSwingThreadContext();
				Context ctx = Context.getCurrentContext();
				ctx.evaluateString(scope, script, "inlineScript", 1, null /* no security domain */);
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
						s.setParentScope(scope);
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
	
	public void executeOnSwingEDT(final Function f) {
		executeOnSwingEDT(f, null);
	}
	
	public void executeOnSwingEDT(final Function f, Object[] args) {
		final Object[] aargs = args == null ? new Object[]{} : args;
		Runnable r = new Runnable() {
			public void run() { 
				f.call(Context.getCurrentContext(), getScope(), null, aargs);
			}
		};
		enterSwingThreadContext();
		try {
			runOnSwingEDT(r);
		} catch(RhinoException e) {
			logger.log(Level.SEVERE, "Caught exception from JavaScript function", e);
		}
	}
	
	public void runStartScript() {
		Scriptable start = require("onstart");
		if (start == null) {
			logger.warning(MessageFormat.format(
			   "Failed to load start module ''{0}''. Skipping startup script.", "onstart"
			));
			return;
		}
		Object o = start.get("run", start);
		if (o == Scriptable.NOT_FOUND){
			logger.warning(MessageFormat.format(
			    "Startup module ''{0}'' doesn''t provide a ''run'' method. Skipping startup script.", "onstart"
			));
			return;
		}
		if (! (o instanceof Function)) {
			logger.warning(MessageFormat.format(
				"Exported property ''run'' in tartup module ''{0}'' should be a function, got {1} instead. Skipping startup script.", "onstart", o
			));
			return;			
		}
		final Function f = (Function)o;
		Runnable r = new Runnable() {
			public void run() {
				f.call(Context.getCurrentContext(), getScope(), null, new Object[]{});
			}
		};
		runOnSwingEDT(r);
	}
}
