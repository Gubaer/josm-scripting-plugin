package org.openstreetmap.josm.plugins.scripting.ui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.plugins.scripting.js.RhinoEngine;
import org.openstreetmap.josm.plugins.scripting.model.JSR223CompiledScriptCache;
import org.openstreetmap.josm.plugins.scripting.model.JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.util.Assert;
import org.openstreetmap.josm.plugins.scripting.util.ExceptionUtil;
import org.openstreetmap.josm.plugins.scripting.util.IOUtil;

/**
 * A utility class providing methods for executing a script (as string or as file) with either an embedded
 * or a plugged script engine, including error handling.
 *
 */
public class ScriptExecutor {
	static private final  Logger logger = Logger.getLogger(ScriptExecutor.class.getName());

	private Component parent = null;

	public ScriptExecutor() {
	}

	/**
	 * Creates a new script executor
	 *
	 * @param parent the parent AWT component. Used to lookup the parent window for error messages.
	 */
	public ScriptExecutor(Component parent) {
		this.parent = parent;
	}

	protected void warnScriptingEngineNotFound(ScriptEngineDescriptor desc) {
		HelpAwareOptionPane.showOptionDialog(
				this.parent,
				"<html>"
				+ tr(
					"<p>The script can''t be executed, because a scripting engine with name ''{0}'' isn''t configured.</p>"
					+ "<p>Refer to the online help for information about how to install/configure a scripting engine for JOSM.</p>"
				)
				+ "</html>"
				,
				tr("Script engine not found"),
				JOptionPane.ERROR_MESSAGE,
				HelpUtil.ht("/Plugin/Scripting")
		);
	}

	protected void warnExecutingScriptFailed(ScriptException e){
		HelpAwareOptionPane.showOptionDialog(
				this.parent,
				tr("Script execution has failed."),
				tr("Script Error"),
				JOptionPane.ERROR_MESSAGE,
				HelpUtil.ht("/Plugin/Scripting")
		);
		System.out.println(tr("Script execution has failed."));
		e.printStackTrace();
	}

	protected void warnJavaScriptExceptionCaught(JavaScriptException e){
		// extract detail information from the property 'description' of the original
		// JavaScript error object
		//
		String details = "";
		Object value = e.getValue();
		if (value instanceof Scriptable) {
			Scriptable s = (Scriptable)value;
			Object desc = s.get("description", s);
			if (! Scriptable.NOT_FOUND.equals(desc)) {
				details = ScriptRuntime.toString(desc);
			}
		}

		HelpAwareOptionPane.showOptionDialog(
				this.parent,
				tr("An error occured in the script.")
				+ (details.isEmpty() ? "" : ("<br><br><strong>Details:</strong> " + details)),
				tr("Script Error"),
				JOptionPane.ERROR_MESSAGE,
				HelpUtil.ht("/Plugin/Scripting")
		);
		System.out.println(tr("Script execution has failed."));
		e.printStackTrace();
	}

	protected void warnOpenScriptFileFailed(File f, Exception e){
		HelpAwareOptionPane.showOptionDialog(
				this.parent,
				tr("Failed to read the script from file ''{0}''.", f.toString()),
				tr("IO error"),
				JOptionPane.ERROR_MESSAGE,
				HelpUtil.ht("/Plugin/Scripting")
		);
		System.out.println(tr("Failed to read the script from file ''{0}''.", f.toString()));
		e.printStackTrace();
	}

	protected void notifyRhinoException(File scriptFile, RhinoException e) {
		HelpAwareOptionPane.showOptionDialog(
				this.parent,
				"<html>"
				+ tr(
					"<p>Failed to execute the script file ''{0}''.</p><p/>"
					+ "<p><strong>Error message:</strong>{1}</p>"
					+ "<p><strong>At:</strong>line {2}, column {3}</p>",
					scriptFile.toString(),
					e.getMessage(),
					e.lineNumber(),
					e.columnNumber()
				)
				+ "</html>"
				,
				tr("Script execution failed"),
				JOptionPane.ERROR_MESSAGE,
				HelpUtil.ht("/Plugin/Scripting")
		);
	}

	protected void notifyRhinoException(RhinoException e) {
		HelpAwareOptionPane.showOptionDialog(
				this.parent,
				"<html>"
				+ tr(
					"<p>Failed to execute a script.</p><p/>"
					+ "<p><strong>Error message:</strong>{0}</p>"
					+ "<p><strong>At:</strong>line {1}, column {2}</p>",
					e.getMessage(),
					e.lineNumber(),
					e.columnNumber()
				)
				+ "</html>"
				,
				tr("Script execution failed"),
				JOptionPane.ERROR_MESSAGE,
				HelpUtil.ht("/Plugin/Scripting")
		);
	}

	protected void notifyIOExeption(File scriptFile, IOException e) {
		HelpAwareOptionPane.showOptionDialog(
				this.parent,
				"<html>"
				+ tr(
					"<p>Failed to execute the script file ''{0}''.</p><p/>"
					+ "<p><strong>Error message:</strong>{1}</p>",
					scriptFile.toString(),
					e.getMessage()
				)
				+ "</html>"
				,
				tr("Script execution failed"),
				JOptionPane.ERROR_MESSAGE,
				HelpUtil.ht("/Plugin/Scripting")
		);
	}

	protected void notifyRuntimeException(RuntimeException e) {
		HelpAwareOptionPane.showOptionDialog(
				this.parent,
				"<html>"
				+ tr(
					"<p>Failed to execute a script.</p><p/>"
					+ "<p><strong>Error message:</strong>{0}</p>",
					e.getMessage()
				)
				+ "</html>"
				,
				tr("Script execution failed"),
				JOptionPane.ERROR_MESSAGE,
				HelpUtil.ht("/Plugin/Scripting")
		);
	}

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
	 * <p>Runs the script in the file <tt>scriptFile</tt> using the script engine described in <tt>desc</tt>
	 * on the Swing EDT.</p>
	 *
	 * @param desc the script engine descriptor. Must not be null.
	 * @param scriptFile the script file. Must not be null. Readable file expected.
	 */
	public void runScriptWithPluggedEngine(final ScriptEngineDescriptor desc, final File scriptFile) throws IllegalArgumentException {
		Assert.assertArgNotNull(desc, "desc");
		Assert.assertArgNotNull(scriptFile, "scriptFile");
		Assert.assertArg(scriptFile.isFile(), "Expected a file a script file, got ''{0}''", scriptFile);
		Assert.assertArg(scriptFile.canRead(), "Expected a readable script file, got ''{0}''", scriptFile);

		final ScriptEngine engine = JSR223ScriptEngineProvider.getInstance().getScriptEngine(desc);
		if (engine == null) {
			warnScriptingEngineNotFound(desc);
			return;
		}
		Runnable task = new Runnable() {
	    	@Override
            public void run() {
	    		FileReader reader = null;
				try {
					if (engine instanceof Compilable) {
						CompiledScript script = JSR223CompiledScriptCache.getInstance().compile((Compilable)engine,scriptFile);
						script.eval();
					} else {
						reader = new FileReader(scriptFile);
						engine.eval(reader);
					}
				} catch(ScriptException e){
					warnExecutingScriptFailed(e);
				} catch(IOException e){
					warnOpenScriptFileFailed(scriptFile, e);
				} finally {
					IOUtil.close(reader);
				}
	    	}
	    };
	    runOnSwingEDT(task);
	}

	/**
	 * <p>Runs the script <tt>script</tt> using the script engine described in <tt>desc</tt>
	 * on the Swing EDT.</p>
	 *
	 * @param desc the script engine descriptor. Must not be null.
	 * @param script the script. Ignored if null.
	 */
	public void runScriptWithPluggedEngine(final ScriptEngineDescriptor desc, final String script) {
		Assert.assertArgNotNull(desc, "desc");
		if (script == null) return;
		final ScriptEngine engine = JSR223ScriptEngineProvider.getInstance().getScriptEngine(desc);
		if (engine == null) {
			warnScriptingEngineNotFound(desc);
			return;
		}
		Runnable task = new Runnable() {
	    	@Override
            public void run() {
	    		FileReader reader = null;
				try {
					engine.eval(script);
				} catch(ScriptException e){
					warnExecutingScriptFailed(e);
				} finally {
					IOUtil.close(reader);
				}
	    	}
	    };
	    runOnSwingEDT(task);
	}

	protected String readFile(File scriptFile) throws IOException {
	    BufferedReader reader = new BufferedReader(
	            new FileReader(scriptFile)
	    );
	    StringBuilder sb = new StringBuilder();
	    String line;
	    while((line = reader.readLine()) != null) {
	        sb.append(line).append("\n");
	    }
	    reader.close();
	    return sb.toString();
	}

	/**
	 * <p>Runs the script in the script file <tt>scriptFile</tt> using the embedded scripting engine
	 * on the Swing EDT.</p>
	 *
	 * @param scriptFile the script file. Must not be null. Expects a readable file.
	 */
	public void runScriptWithEmbeddedEngine(final File scriptFile) throws IllegalArgumentException {
		Assert.assertArgNotNull(scriptFile, "scriptFile");
		try {
		    String script = readFile(scriptFile);
		    RhinoEngine engine = RhinoEngine.getInstance();
            engine.enterSwingThreadContext();
			engine.evaluateOnSwingThread(script, scriptFile.getAbsolutePath());
		} catch(JavaScriptException e){
			warnJavaScriptExceptionCaught(e);
		} catch(RhinoException e){
			System.err.println(e);
			e.printStackTrace();
			notifyRhinoException(scriptFile, e);
		} catch(IOException e){
			notifyIOExeption(scriptFile, e);
			System.err.println(e);
			e.printStackTrace();
		} catch(RuntimeException e){
			System.err.println(e);
			e.printStackTrace();
			//TODO: notify with file name
			notifyRuntimeException(e);
		}
	}

	/**
	 * <p>Runs the script <tt>script</tt> using the embedded scripting engine on the Swing EDT.</p>
	 *
	 * @param script the script. Ignored if null.
	 */
	public void runScriptWithEmbeddedEngine(final String script) {
		if (script  == null) return;
		try {
			RhinoEngine engine = RhinoEngine.getInstance();
			engine.enterSwingThreadContext();
			engine.evaluateOnSwingThread(script);
		} catch(JavaScriptException e){
			System.err.println(e);
			e.printStackTrace();
			warnJavaScriptExceptionCaught(e);
		} catch(RhinoException e){
			System.err.println(e);
			e.printStackTrace();
			notifyRhinoException(e);
		} catch(RuntimeException e){
			System.err.println(e);
			e.printStackTrace();
			notifyRuntimeException(e);
		}
	}
}
