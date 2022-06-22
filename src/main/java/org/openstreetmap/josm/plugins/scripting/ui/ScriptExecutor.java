package org.openstreetmap.josm.plugins.scripting.ui;

import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMEvalException;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;
import org.openstreetmap.josm.plugins.scripting.graalvm.IGraalVMFacade;
import org.openstreetmap.josm.plugins.scripting.js.RhinoEngine;
import org.openstreetmap.josm.plugins.scripting.model.JSR223CompiledScriptCache;
import org.openstreetmap.josm.plugins.scripting.model.JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.*;
import javax.validation.constraints.NotNull;
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
 *
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

    private void warnExecutingScriptFailed(ScriptException e){
        logger.log(Level.SEVERE, tr("Script execution has failed."), e);
        ScriptErrorDialog.showErrorDialog(e);
    }

    private void warnExecutingScriptFailed(GraalVMEvalException e){
        logger.log(Level.SEVERE, tr("Script execution has failed."), e);
        ScriptErrorDialog.showErrorDialog(e);
    }

    private void warnJavaScriptExceptionCaught(JavaScriptException e){
        // extract detail information from the property 'description' of
        // the original JavaScript error object
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

        logger.log(Level.SEVERE, String.format(
            tr("Script execution has failed. Details: %s"),
            details
        ), e);
        ScriptErrorDialog.showErrorDialog(e);
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

    private void notifyRhinoException(File scriptFile, RhinoException e) {
        HelpAwareOptionPane.showOptionDialog(
            this.parent,
            "<html>"
            + tr(
                "<p>Failed to execute the script file ''{0}''.</p>"
                + "<p><strong>Error message:</strong>{1}</p>"
                + "<p><strong>At:</strong>line {2}, column {3}</p>",
                scriptFile.toString(),
                e.getMessage(),
                e.lineNumber(),
                e.columnNumber()
            )
            + "</html>",
            tr("Script execution failed"),
            JOptionPane.ERROR_MESSAGE,
            HelpUtil.ht("/Plugin/Scripting")
        );
    }

    private void notifyRhinoException(RhinoException e) {
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

    private void notifyRuntimeException(RuntimeException e) {
        logger.log(Level.SEVERE, tr("Failed to execute a script."),e);
        ScriptErrorDialog.showErrorDialog(e);
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
     * <p>Runs the script in the file <tt>scriptFile</tt> using the script
     * engine described in <tt>desc</tt> on the Swing EDT.</p>
     *
     * @param desc the script engine descriptor. Must not be null.
     * @param scriptFile the script file. Must not be null. Readable file
     *      expected.
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
        Runnable task = () -> {
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
                warnExecutingScriptFailed(e);
            } catch (IOException e) {
                warnOpenScriptFileFailed(scriptFile, e);
            }
        };
        runOnSwingEDT(task);
    }

    /**
     * <p>Runs the script <tt>script</tt> using the script engine described
     * in <tt>desc</tt> on the Swing EDT.</p>
     *
     * @param desc the script engine descriptor. Must not be null.
     * @param script the script. Ignored if null.
     */
    public void runScriptWithPluggedEngine(
            @NotNull final ScriptEngineDescriptor desc,
            final String script) {
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
                warnExecutingScriptFailed(e);
            }
        };
        runOnSwingEDT(task);
    }

    /**
     * Runs a script with a GraalVM engine.
     *
     * Runs the script on the Swing EDT and prompts the user with a modal
     * dialog, in case of an exception.
     *
     * @param desc the descriptor
     * @param script the script
     */
    public void runScriptWithGraalEngine(
            @NotNull final ScriptEngineDescriptor desc,
            final String script) {
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
            } catch(GraalVMEvalException e){
                warnExecutingScriptFailed(e);
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
     * <p>Runs the script in the script file <tt>scriptFile</tt> using the
     * embedded scripting engine on the Swing EDT.</p>
     *
     * @param scriptFile the script file. Must not be null. Expects a
     *      readable file.
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
            warnJavaScriptExceptionCaught(e);
        } catch(RhinoException e){
            logger.log(Level.SEVERE, String.format("failed to execute script file. file='%s'",
                scriptFile.getAbsolutePath()), e);
            notifyRhinoException(scriptFile, e);
        } catch(IOException e){
            logger.log(Level.SEVERE, String.format("failed to execute script file. file='%s'",
                scriptFile.getAbsolutePath()), e);
            notifyIOException(scriptFile, e);
        } catch(RuntimeException e){
            logger.log(Level.SEVERE, String.format("failed to execute script file. file='%s'",
                scriptFile.getAbsolutePath()), e);
            notifyRuntimeException(e);
        }
    }

    /**
     * <p>Runs the script <tt>script</tt> using the embedded scripting engine
     * on the Swing EDT.</p>
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
            logger.log(Level.SEVERE, "failed to execute script", e);
            warnJavaScriptExceptionCaught(e);
        } catch(RhinoException e){
            logger.log(Level.SEVERE, "failed to execute script", e);
            notifyRhinoException(e);
        } catch(RuntimeException e){
            logger.log(Level.SEVERE, "failed to execute script", e);
            notifyRuntimeException(e);
        }
    }
}
