package org.openstreetmap.josm.plugins.scripting.ui.mru;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

/**
 * This model manages a list of most recently run scripts.
 * <p>
 * Use {@link #remember(Script)} to remember an entry.
 * <p>
 * There is a unique instance of this model which can be connected to
 * either an {@link PropertyChangeListener} or a {@link javax.swing.ComboBoxEditor}.
 */
@SuppressWarnings("deprecation")
public class MostRecentlyRunScriptsModel implements PreferenceKeys {

    static private final Logger logger = Logger.getLogger(MostRecentlyRunScriptsModel.class.getName());

    /**
     * Property name for the list of most recently run scripts
     * managed by this model
     */
    static final public String PROP_SCRIPTS = MostRecentlyRunScriptsModel.class.getName() + ".scripts";

    static final private MostRecentlyRunScriptsModel instance = new MostRecentlyRunScriptsModel();

    /**
     * Unique instance of the model for the list of most recently
     * run scripts.
     *
     * @return the unique instance
     */
    static public MostRecentlyRunScriptsModel getInstance() {
        return instance;
    }

    private List<Script> scripts = new ArrayList<>();

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Replies the {@link PropertyChangeSupport property change support}
     * to register property change listeners
     *
     * @return the property change support
     */
    public @NotNull PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    /**
     * Remembers a script in the list of most recently run scripts
     *
     * @param script the script to remember in the list. Must not be null.
     * @throws NullPointerException if <code>script</code> is null
     */
    public void remember(@NotNull final Script script) {
        Objects.requireNonNull(script);
        final List<Script> oldScripts = List.copyOf(scripts);
        int idx = -1;
        for (int i = 0; i < scripts.size(); i++) {
            if (scripts.get(i).scriptPath().equals(script.scriptPath())) {
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            // not yet in list - add it at position 0
            scripts.add(0, script);
        } else {
            // move script at first position
            scripts.remove(idx);
            scripts.add(0, script);
        }
        // remember the most recently run 9 scripts only
        scripts = scripts.stream().limit(9).collect(Collectors.toList());
        if (hasChanged(oldScripts, scripts)) {
            propertyChangeSupport.firePropertyChange(PROP_SCRIPTS, oldScripts, scripts);
            comboBoxModel.fireContentChanged();
        }
    }

    /**
     * Saves the list of most recently run scripts to the preferences.
     *
     * @param prefs the preferences. Must not be null.
     */
    public void saveToPreferences(@NotNull final Preferences prefs) {
        final var mruPrefs = scripts.stream().map(Script::toMap).collect(Collectors.toList());
        // @since 0.3.1
        // We save the MRU list to the new preference key PREF_KEY_MOST_RECENTLY_USED_SCRIPTS.
        // The preferences now remember the script file path and the engine ID.
        prefs.putListOfMaps(PREF_KEY_MOST_RECENTLY_USED_SCRIPTS, mruPrefs);

        // @since 0.3.1
        // Remove the old preferences for the file history.
        prefs.put(PREF_KEY_FILE_HISTORY, null);
    }

    private boolean canRun(final Script script) {
        // Only check whether the script file exists and is readable.
        // Ignore the value of the script engine. It will be checked before the script is
        // executed. If it is null or unknown the user will interactively select a scripting
        // engine from the available scripting engines.
        final var f = new File(script.scriptPath());
        return f.exists() && f.isFile() && f.canRead();
    }

    private boolean hasChanged(final List<Script> oldScripts, final List<Script> newScripts) {
        if (oldScripts.size() != newScripts.size()) {
            return true;
        }
        for (int i = 0; i < oldScripts.size(); i++) {
            final var oldScriptPath = oldScripts.get(i).scriptPath();
            final var newScriptPath = newScripts.get(i).scriptPath();
            final var oldEngineId = oldScripts.get(i).engineId();
            final var newEngineId = newScripts.get(i).engineId();
            if (!oldScriptPath.equals(newScriptPath)) {
                return true;
            }
            if (oldEngineId == null && newEngineId != null) {
                return true;
            } else if (oldEngineId != null && !oldEngineId.equals(newEngineId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads the list of the most recently run scripts from the preferences.
     *
     * @param prefs the preferences. Must not be null.
     * @throws NullPointerException if <code>prefs</code> is null
     */
    public void loadFromPreferences(@NotNull final Preferences prefs) {
        Objects.requireNonNull(prefs);
        final List<Script> oldScripts = List.copyOf(scripts);
        scripts = new ArrayList<>();
        final var oldFileHistory = prefs.getList(PREF_KEY_FILE_HISTORY);
        final var currentMruList = prefs.getListOfMaps(PREF_KEY_MOST_RECENTLY_USED_SCRIPTS);
        if (!oldFileHistory.isEmpty()) {
            // preferences stored by plugin until version 0.3.0: Load the list of recently run scripts
            // without information about the script engine
            for (int i = 0; i < oldFileHistory.size(); i++) {
                final var scriptPath = oldFileHistory.get(i);
                if (scriptPath == null || scriptPath.isBlank()) {
                    logger.warning(format("Unsupported script file path ''{0}'' in preference list ''{1}'' "
                        + "at position {2}. Ignoring the entry.",
                        scriptPath, PREF_KEY_FILE_HISTORY, i
                    ));
                    continue;
                }
                final var script = new Script(scriptPath, null /* unknown engine ID */);
                if (!canRun(script)) {
                    logger.warning(format("Script file ''{0}'' in preference list ''{1}'' "
                        + "at position {2} doesn''t exist or isn''t readable. Ignoring the entry.",
                        scriptPath, PREF_KEY_FILE_HISTORY, i
                    ));
                    continue;
                }
                this.scripts.add(script);
            }
        } else {
            // preferences stored by plugin up to version 0.3.1: Load the list of recently run scripts,
            // including information about the script engine to execute the script
            for (int i = 0; i < currentMruList.size(); i++) {
                final var map = currentMruList.get(i);
                try {
                    final var script = Script.from(map);
                    if (!canRun(script)) {
                        logger.warning(format("Script file ''{0}'' in preference list ''{1}'' "
                            + "at position {2} doesn''t exist or isn''t readable. Ignoring the entry.",
                            map.get(Script.ATTR_SCRIPT_PATH), PREF_KEY_MOST_RECENTLY_USED_SCRIPTS, i
                        ));
                        continue;
                    }
                    scripts.add(script);
                } catch (IllegalArgumentException e) {
                    logger.warning(format("Illegal script entry in preference list ''{0}'' at position {1}."
                        + "scriptPath=''{2}'', engineId=''{3}''. Ignoring the entry.",
                        PREF_KEY_MOST_RECENTLY_USED_SCRIPTS, i,
                        map.get(Script.ATTR_SCRIPT_PATH), map.get(Script.ATTR_ENGINE_ID)
                    ));
                }
            }
        }

        if (hasChanged(oldScripts, scripts)) {
            propertyChangeSupport.firePropertyChange(PROP_SCRIPTS, oldScripts, scripts);
            comboBoxModel.fireContentChanged();
        }
    }

    /**
     * Replies a combo box model which can be set for an
     * {@link JComboBox}.
     *
     * @return the combo box model
     */
    public DefaultComboBoxModel<String> getComboBoxModel() {
        return comboBoxModel;
    }

    private final ComboBoxModel comboBoxModel = new ComboBoxModel();

    private class ComboBoxModel extends DefaultComboBoxModel<String> {
        void fireContentChanged() {
            super.fireContentsChanged(MostRecentlyRunScriptsModel.this, 0, scripts.size());
        }

        @Override
        public String getElementAt(int idx) {
            return scripts.get(idx).scriptPath();
        }

        @Override
        public int getSize() {
            return scripts.size();
        }
    }

    /**
     * Replies a list of actions, one run action for each script
     * in the list of most recently run scripts.
     *
     * @return the list of actions
     */
    public @NotNull List<RunMRUScriptAction> getRunScriptActions() {
        final var actions = new ArrayList<RunMRUScriptAction>();
        for (int i = 0; i < scripts.size(); i++) {
            actions.add(new RunMRUScriptAction(i + 1, scripts.get(i)));
        }
        return actions;
    }
}
