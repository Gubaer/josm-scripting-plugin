package org.openstreetmap.josm.plugins.scripting.ui;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMEvalException;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;
import org.openstreetmap.josm.plugins.scripting.graalvm.IGraalVMFacade;
import org.openstreetmap.josm.plugins.scripting.model.JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openstreetmap.josm.plugins.scripting.util.FileUtils.buildTextFileReader;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Behaviour to run a script file, factored out into a service class.
 */
public class RunScriptService {
    static final private Logger logger = Logger.getLogger(
        RunScriptService.class.getName());

    private void warnScriptFileDoesntExist(File f, Component parent){
        HelpAwareOptionPane.showOptionDialog(
            parent,
            tr("The script file ''{0}'' doesn''t exist.", f.toString()),
            tr("File not found"),
            JOptionPane.ERROR_MESSAGE,
            HelpUtil.ht("/Plugin/Scripting")
        );
    }

    private void warnEmptyFile(Component parent){
        HelpAwareOptionPane.showOptionDialog(
            parent,
            tr("Please enter a file name first."),
            tr("Empty file name"),
            JOptionPane.ERROR_MESSAGE,
            HelpUtil.ht("/Plugin/Scripting")
        );
    }

    private void warnScriptFileIsntReadable(File f, Component parent){
        HelpAwareOptionPane.showOptionDialog(
            parent,
            tr("The script file ''{0}'' isn''t readable.", f.toString()),
            tr("File not readable"),
            JOptionPane.ERROR_MESSAGE,
            HelpUtil.ht("/Plugin/Scripting")
        );
    }

    private void warnOpenScriptFileFailed(File f, Exception e,
            Component parent){
        HelpAwareOptionPane.showOptionDialog(
            parent,
            tr("Failed to read the script from the file ''{0}''.",
                f.toString()),
            tr("IO error"),
            JOptionPane.ERROR_MESSAGE,
            HelpUtil.ht("/Plugin/Scripting")
        );
        logger.log(Level.SEVERE,
            tr("Failed to read the script from the file ''{0}''.", f.toString()),
            e);
    }

    private Stream<ScriptEngineDescriptor> filterJSR223Engines(
            final String mimeType) {
        return JSR223ScriptEngineProvider.getInstance()
            .getScriptEngineFactories()
            .stream()
            .filter(factory -> factory.getMimeTypes().contains(mimeType))
            .map(ScriptEngineDescriptor::new);
    }

    private Stream<ScriptEngineDescriptor> filterGraalVMEngines(
            final String mimeType) {
        return  GraalVMFacadeFactory.isGraalVMPresent()
            ? GraalVMFacadeFactory.getOrCreateGraalVMFacade()
                .getScriptEngineDescriptors()
                .stream()
                .filter(desc ->
                        desc.getContentMimeTypes().contains(mimeType))
            : Stream.empty();
    }
    /**
     * Determines the script engine to run the script in file <tt>file</tt>.
     * Prompts the user with a selection dialog, if the engine can't be
     * derived from the file name suffix.
     *
     * @param fileName the script file name
     * @param parent the parent component relative to which dialogs are
     * displayed
     * @return the script engine descriptor or null
     */
    public ScriptEngineDescriptor deriveOrAskScriptEngineDescriptor(
            String fileName, Component parent) {
        File file = new File(fileName);
        JSR223ScriptEngineProvider provider =
            JSR223ScriptEngineProvider.getInstance();
        String mimeType = provider.getContentTypeForFile(file);

        // the default engine if the language is JavaScript
        Stream<ScriptEngineDescriptor> defaultEngine =
            mimeType.equals("application/javascript")
                ? Stream.of(ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE)
                : Stream.empty();

        // the stream of suitable engines for a given mime type
        java.util.List<ScriptEngineDescriptor> engines = Stream.of(
            defaultEngine,
            filterGraalVMEngines(mimeType),
            filterJSR223Engines(mimeType)
        ).flatMap (desc -> desc).collect(Collectors.toList());

        // exactly one suitable engine found. Use it without prompting
        // the user.
        if (engines.size() == 1) {
            return engines.get(0);
        }

        // no or more than one suitable engines found. Prompt the user
        // to select one.
        return ScriptEngineSelectionDialog.select(parent);
    }

    /**
     * Checks whether the script given by <tt>fileName</tt> can be run.
     * If not, prompts the user with an error message.
     *
     * @param fileName the file name
     * @param parent the parent component relative to which the prompt
     * with the error message is displayed
     * @return true, if the script can be run; false, otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canRunScript(String fileName, Component parent) {
        if (fileName.isEmpty()){
            warnEmptyFile(parent);
            return false;
        }
        final File f = new File(fileName);
        if (! f.exists() || !f.isFile()) {
            warnScriptFileDoesntExist(f, parent);
            return false;
        } else if (!f.canRead()) {
            warnScriptFileIsntReadable(f, parent);
            return false;
        }

        try(Reader ignored = buildTextFileReader(f)) {
            // just try to open the reader ...
            return true;
        } catch(IOException e){
            // ... and if it fails, warn about it
            warnOpenScriptFileFailed(f, e, parent);
            return false;
        }
    }

    /**
     * Runs the script in the file <tt>fileName</tt> using the scripting
     * engine <tt>engine</tt>.
     *
     * @param fileName the script file name
     * @param engine the script engine descriptor
     *
     * @throws NullPointerException thrown if fileName is null
     * @throws NullPointerException thrown if engine is null
     */
    public void runScript(@NotNull String fileName, @NotNull ScriptEngineDescriptor engine) {
        runScript(fileName, engine, null);
    }

    protected void runScriptWithGraalVM(
            final File script, final ScriptEngineDescriptor engine)
            throws IOException, GraalVMEvalException {
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
        facade.eval(engine, script);
    }

    /**
     * Runs the script in the file <tt>fileName</tt> using the scripting
     * engine <tt>engine</tt>. <tt>parent</tt> is the parent component relative
     * to which dialogs and option panes are displayed.
     *
     * @param fileName the script file name
     * @param engine the script engine descriptor
     * @param parent the parent component
     */
    public void runScript(@NotNull String fileName,
            @NotNull ScriptEngineDescriptor engine,
            @Null Component parent) {
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(engine);
        File f  = new File(fileName);

        MostRecentlyRunScriptsModel model = MostRecentlyRunScriptsModel
                .getInstance();
        model.remember(f.getAbsolutePath());
        model.saveToPreferences(Preferences.main());

        switch(engine.getEngineType()){
            case EMBEDDED:
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE,
                        "executing script with embedded engine ...");
                }
                new ScriptExecutor(parent).runScriptWithEmbeddedEngine(f);
                break;

            case PLUGGED:
                new ScriptExecutor(parent).runScriptWithPluggedEngine(engine, f);
                break;

            case GRAALVM:
                try {
                    runScriptWithGraalVM(f, engine);
                } catch(IOException |GraalVMEvalException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
                break;
        }
    }
}
