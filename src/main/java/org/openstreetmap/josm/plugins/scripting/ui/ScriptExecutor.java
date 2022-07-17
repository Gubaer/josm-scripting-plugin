package org.openstreetmap.josm.plugins.scripting.ui;

import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMEvalException;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;
import org.openstreetmap.josm.plugins.scripting.graalvm.IGraalVMFacade;
import org.openstreetmap.josm.plugins.scripting.rhino.RhinoEngine;
import org.openstreetmap.josm.plugins.scripting.jsr223.JSR223CompiledScriptCache;
import org.openstreetmap.josm.plugins.scripting.jsr223.JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.runner.ScriptErrorDialog;
import org.openstreetmap.josm.plugins.scripting.ui.widgets.ScriptErrorViewerModel;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.plugins.scripting.util.FileUtils.buildTextFileReader;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * A utility class providing methods for executing a script (as string or
 * as file) with either an embedded or a plugged script engine, including
 * error handling.
 */
public class ScriptExecutor {
    static private final Logger logger =
         Logger.getLogger(ScriptExecutor.class.getName());

    private final Component parent;

    /**
     * Creates a new script executor
     *
     * @param parent the parent AWT component. Used to lookup the parent
     *      window for error messages.
     */
    public ScriptExecutor(Component parent) {
        this.parent = parent;
    }

    private void warnScriptingEngineNotFound() {
        HelpAwareOptionPane.showOptionDialog(
            this.parent,
            "<html>"
            + tr(
                "<p>The script can''t be executed, because a scripting "
                + "engine with name ''{0}'' isn''t configured.</p>"
                + "<p>Refer to the online help for information about how "
                + "to install/configure a scripting engine for JOSM.</p>"
            )
            + "</html>"
            ,
            tr("Script engine not found"),
            JOptionPane.ERROR_MESSAGE,
            HelpUtil.ht("/Plugin/Scripting")
        );
    }


    private void warnOpenScriptFileFailed(File f, Exception e){
        logger.log(Level.SEVERE,
            tr("Failed to read the script from file ''{0}''.", f.toString()),
            e);
        HelpAwareOptionPane.showOptionDialog(
            this.parent,
            tr("Failed to read the script from file ''{0}''.", f.toString()),
            tr("IO error"),
            JOptionPane.ERROR_MESSAGE,
            HelpUtil.ht("/Plugin/Scripting")
        );
    }

    private void notifyIOException(File scriptFile, IOException e) {
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

    private void runOnSwingEDT(Runnable r){
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
                logger.log(Level.WARNING, String.format(
                    "Unexpected exception wrapped in InvocationTargetException: %s",
                    throwable.toString()
                ), throwable);
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Runs the script in the file <tt>scriptFile</tt> using the script
     * engine described in <tt>desc</tt> on the Swing EDT.
     *
     * @param desc the script engine descriptor
     * @param scriptFile the script file
     * @throws NullPointerException if <code>desc</code> is null
     * @throws NullPointerException if <code>scriptFile</code> is null
     * @throws IllegalArgumentException if <code>scriptFile</code> isn't a readable file
     */
    void runScriptWithPluggedEngine(
            @NotNull final ScriptEngineDescriptor desc,
            @NotNull  final File scriptFile) throws IllegalArgumentException {
        Objects.requireNonNull(desc);
        Objects.requireNonNull(scriptFile);
        Assert.assertArg(scriptFile.isFile(),
            "Expected a file a script file, got ''{0}''", scriptFile);
        Assert.assertArg(scriptFile.canRead(),
            "Expected a readable script file, got ''{0}''", scriptFile);

        final ScriptEngine engine = JSR223ScriptEngineProvider
                .getInstance().getScriptEngine(desc);
        if (engine == null) {
            warnScriptingEngineNotFound();
            return;
        }
        final Runnable task = () -> {
            try {
                if (engine instanceof Compilable) {
                    JSR223CompiledScriptCache
                        .getInstance()
                        .compile((Compilable) engine, scriptFile)
                        .eval();
                } else {
                    try (Reader reader =
                        new InputStreamReader(new FileInputStream(scriptFile),
                                StandardCharsets.UTF_8)) {
                        engine.eval(reader);
                    }
                }
            } catch (ScriptException e) {
                //warnExecutingScriptFailed(e);
                ScriptErrorDialog.showErrorDialog(e);
            } catch (IOException e) {
                warnOpenScriptFileFailed(scriptFile, e);
            }
        };
        runOnSwingEDT(task);
    }

    /**
     * Runs the script <tt>script</tt> using the script engine described
     * in <tt>desc</tt> on the Swing EDT.
     *
     * @param desc the script engine descriptor. Must not be null.
     * @param script the script. Ignored if null.
     * @param errorViewerModel the error viewer model
     */
    public void runScriptWithPluggedEngine(
            @NotNull final ScriptEngineDescriptor desc,
            final String script,
            final ScriptErrorViewerModel errorViewerModel) {
        Objects.requireNonNull(desc);
        if (script == null) return;
        final ScriptEngine engine = JSR223ScriptEngineProvider.getInstance()
                .getScriptEngine(desc);
        if (engine == null) {
            warnScriptingEngineNotFound();
            return;
        }
        Runnable task = () -> {
            try {
                engine.eval(script);
            } catch(ScriptException e){
                errorViewerModel.setError(e);
            }
        };
        runOnSwingEDT(task);
    }

    /**
     * Runs a script with a GraalVM engine.
     *
     * Runs the script on the Swing EDT. Handling errors is delegated to
     * <code>errorViewerModel</code>.
     *
     * @param desc the descriptor
     * @param script the script
     * @param errorViewerModel callback to handle errors
     */
    public void runScriptWithGraalEngine(
            @NotNull final ScriptEngineDescriptor desc,
            final String script,
            final @Null ScriptErrorViewerModel errorViewerModel) {
        Objects.requireNonNull(desc);
        if (!desc.getEngineType().equals(
            ScriptEngineDescriptor.ScriptEngineType.GRAALVM)) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Expected GraalVM descriptor, got {0}", desc.getEngineType()
            ));
        }
        if (script == null) return;
        final IGraalVMFacade facade = GraalVMFacadeFactory.createGraalVMFacade();
        if (facade == null) {
            // should not happen. Make sure this method is only invoked
            // if GraalVM is present. Log a warning and return, don't prompt
            // the user with an error message.
            logger.warning(tr("GraalVM not present, can''t run script with GraalVM"));
            return;
        }
        Runnable task = () -> {
            try {
                facade.resetContext();
                facade.eval(desc, script);
            } catch(Throwable e) {
                errorViewerModel.setError(e);
            } finally {
                facade.resetContext();
            }
        };
        runOnSwingEDT(task);
    }

    private String readFile(File scriptFile) throws IOException {
        try (BufferedReader reader =
             new BufferedReader(buildTextFileReader(scriptFile))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Runs the script in the script file <tt>scriptFile</tt> using the
     * embedded scripting engine on the Swing EDT.
     *
     * @param scriptFile the script file. Expects a readable file.
     * @throws NullPointerException thrown if <code>scriptFile</code> is null
     */
    @SuppressWarnings("WeakerAccess") // part of the public API
    public void runScriptWithEmbeddedEngine(@NotNull final File scriptFile)
                throws IllegalArgumentException {
        Objects.requireNonNull(scriptFile);
        try {
            String script = readFile(scriptFile);
            RhinoEngine engine = RhinoEngine.getInstance();
            engine.enterSwingThreadContext();
            engine.evaluateOnSwingThread(script, scriptFile.getAbsolutePath());
        } catch(JavaScriptException e){
            ScriptErrorDialog.showErrorDialog(e);
            //warnJavaScriptExceptionCaught(e);
        } catch(RhinoException e){
            logger.log(Level.SEVERE, String.format("failed to execute script file. file='%s'",
                scriptFile.getAbsolutePath()), e);
            ScriptErrorDialog.showErrorDialog(e);
            //notifyRhinoException(scriptFile, e);
        } catch(IOException e){
            logger.log(Level.SEVERE, String.format("failed to execute script file. file='%s'",
                scriptFile.getAbsolutePath()), e);
            notifyIOException(scriptFile, e);
        } catch(RuntimeException e){
            logger.log(Level.SEVERE, String.format("failed to execute script file. file='%s'",
                scriptFile.getAbsolutePath()), e);
            ScriptErrorDialog.showErrorDialog(e);
            //notifyRuntimeException(e);
        }
    }

    /**
     * Runs the script <tt>script</tt> using the embedded scripting engine
     * on the Swing EDT.
     *
     * @param script the script. Ignored if null.
     * @param errorModel the error model
     */
    public void runScriptWithEmbeddedEngine(final String script, final ScriptErrorViewerModel errorModel) {
        if (script  == null) return;
        try {
            RhinoEngine engine = RhinoEngine.getInstance();
            engine.enterSwingThreadContext();
            engine.evaluateOnSwingThread(script);
        } catch(RuntimeException e){
            logger.log(Level.SEVERE, "failed to execute script", e);
            errorModel.setError(e);
        }
    }

    protected void runScriptWithGraalVM(final File script, final ScriptEngineDescriptor engine) {
        if (logger.isLoggable(Level.FINE)) {
            final String message =  MessageFormat.format(
                "executing script with GraalVM ''{0}''. Script file: ''{1}''",
                engine.getEngineId(),
                script.getAbsolutePath()
            );
            logger.log(Level.FINE, message);
        }
        final IGraalVMFacade facade = GraalVMFacadeFactory
                .getOrCreateGraalVMFacade();
        try {
            facade.eval(engine, script);
        } catch (IOException e) {
            warnOpenScriptFileFailed(script, e);
            throw new RuntimeException(e);
        } catch (GraalVMEvalException e) {
            ScriptErrorDialog.showErrorDialog(e);
        }
    }
}
