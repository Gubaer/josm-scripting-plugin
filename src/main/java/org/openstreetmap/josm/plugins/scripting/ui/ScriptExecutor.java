package org.openstreetmap.josm.plugins.scripting.ui;

import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMEvalException;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;
import org.openstreetmap.josm.plugins.scripting.graalvm.IGraalVMFacade;
import org.openstreetmap.josm.plugins.scripting.model.JSR223CompiledScriptCache;
import org.openstreetmap.josm.plugins.scripting.model.JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.plugins.scripting.ui.SwingUtil.runOnSwingEDT;
import static org.openstreetmap.josm.tools.I18n.tr;
import static java.text.MessageFormat.format;

/**
 * A utility class providing methods for executing a script (as string or
 * as file) with either an embedded or a plugged script engine, including
 * error handling.
 */
public class ScriptExecutor {
    static private final Logger logger = Logger.getLogger(ScriptExecutor.class.getName());

    private final Component parent;

    /**
     * Creates a new script executor
     *
     * @param parent the parent AWT component. Used to lookup the parent
     *               window for error messages.
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
            + "</html>",
            tr("Script engine not found"),
            JOptionPane.ERROR_MESSAGE,
            HelpUtil.ht("/Plugin/Scripting")
    );
    }


    private void warnOpenScriptFileFailed(File f, Exception e) {
        logger.log(Level.SEVERE, tr("Failed to read the script from file ''{0}''.", f.toString()), e);
        HelpAwareOptionPane.showOptionDialog(
            this.parent,
            tr("Failed to read the script from file ''{0}''.", f.toString()),
            tr("IO error"),
            JOptionPane.ERROR_MESSAGE,
            HelpUtil.ht("/Plugin/Scripting")
        );
    }

    /**
     * Runs the script in the file <tt>scriptFile</tt> using the script
     * engine described in <tt>desc</tt> on the Swing EDT.
     *
     * @param desc       the script engine descriptor
     * @param scriptFile the script file
     * @throws NullPointerException     if <code>desc</code> is null
     * @throws NullPointerException     if <code>scriptFile</code> is null
     * @throws IllegalArgumentException if <code>scriptFile</code> isn't a readable file
     */
    public void runScriptWithPluggedEngine(
            @NotNull final ScriptEngineDescriptor desc,
            @NotNull final File scriptFile) throws IllegalArgumentException {
        Objects.requireNonNull(desc);
        Objects.requireNonNull(scriptFile);
        Assert.assertArg(scriptFile.isFile(), "Expected a script file, got ''{0}''", scriptFile);
        Assert.assertArg(scriptFile.canRead(), "Expected a readable script file, got ''{0}''", scriptFile);

        final ScriptEngine engine = JSR223ScriptEngineProvider.getInstance().getScriptEngine(desc);
        if (engine == null) {
            warnScriptingEngineNotFound();
            return;
        }
        final Runnable task = () -> {
            final var bindings = engine.createBindings();
            if (desc.isJython()) {
                // For a python script we initialize the bindings as follows:
                //    __file__ = <full path to script file>
                bindings.put("__file__", scriptFile.getPath());

                // For a python script we initialize the Jython engine as follows:
                //    add the script's parent directory to sys.path
                //    add the paths in JYTHONPATH to sys.path
                final var sysPathStatement = format("import sys; sys.path.append(''{0}'')", scriptFile.getParent());
                try {
                    engine.eval(sysPathStatement);
                } catch(ScriptException e) {
                    logger.log(Level.WARNING, format("Failed to add ''{0}'' to sys.path", scriptFile.getParent()), e);
                }
                final var jythonPath = System.getenv("JYTHONPATH");
                if (jythonPath != null) {
                    final var paths = jythonPath.split(File.pathSeparator);
                    if (paths.length > 0) {
                        final var script = "import sys; " +
                            Arrays.stream(paths)
                                .distinct()
                                .map(path -> format("sys.path.append(''{0}'')", path))
                                .collect(Collectors.joining("; "));
                        try {
                            engine.eval(script);
                        } catch(ScriptException e) {
                            logger.log(Level.WARNING, format("Failed to add JYTHONPATH ''{0}'' to sys.path", jythonPath), e);
                        }
                    }
                }
            }
            try {
                if (engine instanceof Compilable) {
                    JSR223CompiledScriptCache
                        .getInstance()
                        .compile((Compilable) engine, scriptFile)
                        .eval(bindings);
                } else {
                    try (var reader = new InputStreamReader(new FileInputStream(scriptFile), StandardCharsets.UTF_8)) {
                        engine.eval(reader, bindings);
                    }
                }
            } catch (ScriptException e) {
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
     * @param desc             the script engine descriptor. Must not be null.
     * @param script           the script. Ignored if null.
     * @param errorViewerModel the error viewer model
     */
    public void runScriptWithPluggedEngine(
            @NotNull final ScriptEngineDescriptor desc,
            final String script,
            final ScriptErrorViewerModel errorViewerModel) {
        Objects.requireNonNull(desc);
        if (script == null) return;
        final ScriptEngine engine = JSR223ScriptEngineProvider.getInstance().getScriptEngine(desc);
        if (engine == null) {
            warnScriptingEngineNotFound();
            return;
        }
        Runnable task = () -> {
            try {
                engine.eval(script);
            } catch (ScriptException e) {
                errorViewerModel.setError(e);
            }
        };
        runOnSwingEDT(task);
    }

    /**
     * Runs a script with a GraalVM engine.
     * <p>
     * Runs the script on the Swing EDT. Handling errors is delegated to
     * <code>errorViewerModel</code>.
     *
     * @param engine           the descriptor
     * @param script           the script
     * @param errorViewerModel callback to handle errors
     */
    public void runScriptWithGraalEngine(
            @NotNull final ScriptEngineDescriptor engine,
            final String script,
            final @Null ScriptErrorViewerModel errorViewerModel) {
        Objects.requireNonNull(engine);
        if (!engine.getEngineType().equals(
                ScriptEngineDescriptor.ScriptEngineType.GRAALVM)) {
            throw new IllegalArgumentException(format(
                    "Expected GraalVM descriptor, got {0}", engine.getEngineType()
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
        final Runnable task = () -> {
            try {
                facade.resetContext();
                facade.eval(engine, script);
            } catch (Throwable e) {
                errorViewerModel.setError(e);
            } finally {
                facade.resetContext();
            }
        };
        runOnSwingEDT(task);
    }

    /**
     * Runs a script file with a GraalVM engine.
     * <p>
     * Runs the script on the Swing EDT.
     *
     * @param engine  the descriptor. Must not be null.
     * @param script  the script. Must not be null.
     * @throws NullPointerException if <code>engine</code> or <code>script</code> is null
     */
    public void runScriptWithGraalEngine(@NotNull final ScriptEngineDescriptor engine, @NotNull final File script) {
        if (logger.isLoggable(Level.FINE)) {
            final var message = format(
                "executing script with GraalVM ''{0}''. Script file: ''{1}''",
                engine.getLocalEngineId(),
                script.getAbsolutePath()
            );
            logger.log(Level.FINE, message);
        }
        final var facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade();
        final Runnable task = () -> {
            try {
                facade.resetContext();
                facade.eval(engine, script);
            } catch (IOException e) {
                warnOpenScriptFileFailed(script, e);
            } catch (GraalVMEvalException e) {
                ScriptErrorDialog.showErrorDialog(e);
            } finally {
                facade.resetContext();
            }
        };
        runOnSwingEDT(task);
    }
}
