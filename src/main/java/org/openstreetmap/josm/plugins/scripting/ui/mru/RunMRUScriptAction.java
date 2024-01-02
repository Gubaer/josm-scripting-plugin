package org.openstreetmap.josm.plugins.scripting.ui.mru;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.RunScriptService;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

/**
 * Action to run a most recently run script.
 */
public class RunMRUScriptAction extends AbstractAction {
    static private final Logger logger = Logger.getLogger(RunMRUScriptAction.class.getName());

    // the path of the script file
    private final File script;
    // the ID of the scripting engine. null, if the engine isn't known
    private final String engineId;

    /**
     * Creates an action to run the <code>i</code>-th MRU {@link File script}.
     *
     * @param i      the position in the list of MRU scripts in the scripting menu.
     * @param script the script given by file
     */
    RunMRUScriptAction(final int i, @NotNull final File script) {
        this.script = script;
        this.engineId = null;
        initActionProperties(i);
    }

    /**
     * Creates an action to run the <code>i</code>-th MRU {@link Script script}.
     *
     * @param i      the position in the list of MRU scripts in the scripting menu.
     * @param script the script
     * @throws NullPointerException if <code>script</code> is null
     */
    RunMRUScriptAction(final int i, @NotNull final Script script) {
        Objects.requireNonNull(script);
        this.script = new File(script.scriptPath());
        this.engineId = script.engineId();
        initActionProperties(i);
    }

    private void initActionProperties(final int pos) {
        String engineName;
        if (engineId == null) {
            engineName = null;
        } else {
            engineName = ScriptEngineDescriptor.buildFromPreferences(engineId).getEngineName().orElse(null);
        }
        if (engineName == null) {
            putValue(NAME, String.format("%s %s", pos, this.script.getName()));
        } else {
            putValue(NAME, String.format("%s %s (%s)", pos, this.script.getName(), engineName));
        }
        putValue(SHORT_DESCRIPTION, this.script.getAbsolutePath());
        putValue(SMALL_ICON, ImageProvider.get("run", ImageProvider.ImageSizes.SMALLICON));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        final var service = new RunScriptService();
        if (!service.canRunScript(script.getAbsolutePath(), null /* parent */)) {
            return;
        }
        ScriptEngineDescriptor engine;
        if (engineId != null) {
            // We know the script engine. Check whether it is present. If not let the user
            // select one.
            engine = ScriptEngineDescriptor.buildFromPreferences(engineId);
            if (engine == null) {
                logger.info(format("no engine found for engine id ''{0}''", engineId));
                engine = service.deriveOrAskScriptEngineDescriptor(script.getAbsolutePath(), null /* parent */);
                if (engine == null) {
                    return;
                }
            }
        } else {
            // We don't know the script engine. Let the user select one.
            engine = service.deriveOrAskScriptEngineDescriptor(script.getAbsolutePath(), null /* parent */);
            if (engine == null) {
                return;
            }
        }
        service.runScript(script.getAbsolutePath(), engine);

        final var updatedScript = new Script(script.getAbsolutePath(), engine.getFullEngineId());
        final var model = MostRecentlyRunScriptsModel.getInstance();
        model.remember(updatedScript);
        model.saveToPreferences(Preferences.main());
    }

    /**
     * Replies the script file
     *
     * @return the script file
     */
    public @NotNull File getScript() {
        return script;
    }

    /**
     * Replies the engine ID for the engine to run the script.
     * <p>
     * Replies null if the engine to run this script isn't known (yet).
     *
     * @return the engine ID
     */
    public @Null String getEngineId() {
        return engineId;
    }
}