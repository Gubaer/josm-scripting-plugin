package org.openstreetmap.josm.plugins.scripting.ui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.plugins.scripting.model.JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

/**
 * Behaviour to run a script file, factored out into a service class.
 *
 */
public class RunScriptService {
    static final private Logger logger = Logger.getLogger(
            RunScriptService.class.getName()
    );

    protected void warnMacroFileDoesntExist(File f, Component parent){
        HelpAwareOptionPane.showOptionDialog(
                parent,
                tr("The script file ''{0}'' doesn''t exist.", f.toString()),
                tr("File not found"),
                JOptionPane.ERROR_MESSAGE,
                HelpUtil.ht("/Plugin/Scripting")
        );
    }

    protected void warnEmptyFile(Component parent){
        HelpAwareOptionPane.showOptionDialog(
                parent,
                tr("Please enter a file name first."),
                tr("Empty file name"),
                JOptionPane.ERROR_MESSAGE,
                HelpUtil.ht("/Plugin/Scripting")
        );
    }

    protected void warnMacroFileIsntReadable(File f, Component parent){
        HelpAwareOptionPane.showOptionDialog(
                parent,
                tr("The script file ''{0}'' isn''t readable.", f.toString()),
                tr("File not readable"),
                JOptionPane.ERROR_MESSAGE,
                HelpUtil.ht("/Plugin/Scripting")
        );
    }

    protected void warnOpenScriptFileFailed(File f, Exception e, Component parent){
        HelpAwareOptionPane.showOptionDialog(
                parent,
                tr("Failed to read the script from the file ''{0}''.", f.toString()),
                tr("IO error"),
                JOptionPane.ERROR_MESSAGE,
                HelpUtil.ht("/Plugin/Scripting")
        );
        System.out.println(tr("Failed to read the script from the file ''{0}''.", f.toString()));
        e.printStackTrace();
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
        if (mimeType.equals("application/javascript")) {
            return ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE;
        }
        ScriptEngineDescriptor desc = JSR223ScriptEngineProvider.
                getInstance()
                .getEngineForFile(file);
        if (desc != null) return desc;
        return ScriptEngineSelectionDialog.select(parent);
    }

    /**
     * Checks whether the script given by <tt>fileName</tt> can be run.
     * If not, prompts the user with a error message.
     *
     * @param fileName the file name
     * @param parent the parent component relative to which the prompt
     * with the error message is displayed
     * @return true, if the script can be run; false, otherwise
     */
    public boolean canRunScript(String fileName, Component parent) {
        if (fileName.isEmpty()){
            warnEmptyFile(parent);
            return false;
        }
        final File f = new File(fileName);
        if (! f.exists() || !f.isFile()) {
            warnMacroFileDoesntExist(f, parent);
            return false;
        } else if (!f.canRead()) {
            warnMacroFileIsntReadable(f, parent);
            return false;
        }

        try {
            new FileReader(f);
        } catch(IOException e){
            warnOpenScriptFileFailed(f, e, parent);
            return false;
        }
        return true;
    }

    /**
     * Runs the script in the file <tt>fileName</tt> using the scripting
     * engine <tt>engine</tt>.
     *
     * @param fileName the script file name. Must not be null.
     * @param engine the script engine descriptor. Must not be null.
     *
     * @throws IllegalArgumentException thrown if fileName is null
     * @throws IllegalArgumentException thrown if engine is null
     */
    public void runScript(String fileName, ScriptEngineDescriptor engine) {
        runScript(fileName, engine, null);
    }

    /**
     * Runs the script in the file <tt>fileName</tt> using the scripting
     * engine <tt>engine</tt>. <tt>parent</tt> is the parent component relative
     * to which dialogs and option panes are displayed.
     *
     * @param fileName the script file name. Must not be null.
     * @param engine the script engine descriptor. Must not be null.
     * @param parent the parent component. May be null
     *
     * @throws IllegalArgumentException thrown if fileName is null
     * @throws IllegalArgumentException thrown if engine is null
     */
    public void runScript(String fileName, ScriptEngineDescriptor engine, Component parent) {
        Assert.assertArgNotNull(fileName);
        Assert.assertArgNotNull(engine);
        File f  = new File(fileName);

        MostRecentlyRunScriptsModel model = MostRecentlyRunScriptsModel.getInstance();
        model.remember(f.getAbsolutePath());
		model.saveToPreferences(Main.pref);
		
        switch(engine.getEngineType()){
        case EMBEDDED:
        	if (logger.isLoggable(Level.FINE)) {
        		logger.log(Level.FINE, "executing script with embedded engine ...");
        	}
            new ScriptExecutor(parent).runScriptWithEmbeddedEngine(f);
            break;
        case PLUGGED:
            new ScriptExecutor(parent).runScriptWithPluggedEngine(engine, f);
            break;
        }
    }
}
