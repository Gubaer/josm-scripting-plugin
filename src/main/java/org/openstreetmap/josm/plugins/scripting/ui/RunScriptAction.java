package org.openstreetmap.josm.plugins.scripting.ui;


import org.openstreetmap.josm.actions.ActionParameter;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.ParameterizedAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * An action for running a specific script using a specific scripting engine.
 */
public class RunScriptAction extends JosmAction implements ParameterizedAction {

    static private final Logger logger = Logger.getLogger(RunScriptAction.class.getName());

    public static final String PARA_SCRIPTING_FILE_NAME = "scriptingFilename";
    public static final String PARA_ENGINE_ID = "engineId";

    private static final List<ActionParameter<?>> ACTION_PARAMETERS = List.of(
        new ActionParameter.StringActionParameter(PARA_SCRIPTING_FILE_NAME),
        new ActionParameter.StringActionParameter(PARA_ENGINE_ID)
    );

    RunScriptAction() {
        super(
            tr("Run"),
            "run",
            tr("Run the script"),
            null, // no shortcut
            true, // do register in toolbar
            "scripting/run", // the toolbar id
            true // do install adapters
        );
    }

    @Override
    public List<ActionParameter<?>> getActionParameters() {
        return ACTION_PARAMETERS;
    }

    @Override
    public void actionPerformed(ActionEvent evt, Map<String, Object> parameters) {

        final String scriptFile = (String)parameters.getOrDefault(PARA_SCRIPTING_FILE_NAME, null);
        final String engineId = (String) parameters.getOrDefault(PARA_ENGINE_ID, null);
        if (scriptFile == null) {
            logger.warning(format("action doesn''t include parameter ''{0}''. Can''t execute script file.",
                PARA_SCRIPTING_FILE_NAME));
            return;
        }

        final var service = new RunScriptService();
        if (!service.canRunScript(scriptFile, MainApplication.getMainFrame())) {
            return;
        }

        ScriptEngineDescriptor engine = null;
        if (engineId != null) {
            engine = ScriptEngineDescriptor.buildFromPreferences(engineId);
            if (engine == null) {
                logger.warning(format("action includes unknown engine ID ''{0}''. Can''t execute script file.", engineId));
            }
        }
        if (engine == null) {
            engine = service.deriveOrAskScriptEngineDescriptor(scriptFile, MainApplication.getMainFrame());
        }
        if (engine == null) {
            // no engine selected,aborting execution
            return;
        }

        service.runScript(scriptFile, engine, MainApplication.getMainFrame());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionPerformed(e, Map.of());
    }
}