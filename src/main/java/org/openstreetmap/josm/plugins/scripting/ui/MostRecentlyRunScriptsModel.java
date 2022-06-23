package org.openstreetmap.josm.plugins.scripting.ui;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.beans.PropertyChangeListener;

/**
 * This model manages a list of most recently run scripts.
 *
 * Use {@link #remember(String)} to remember an entry.
 *
 * There is a unique instance of this model which can be connected to
 * either an {@link PropertyChangeListener} or a {@link javax.swing.ComboBoxEditor}.
 *
 */
public class MostRecentlyRunScriptsModel implements PreferenceKeys {

    /**
     * Property name for the list of most recently run scripts
     * managed by this model
     */
    static final public String PROP_SCRIPTS
        = MostRecentlyRunScriptsModel.class.getName() + ".scripts";

    static final private MostRecentlyRunScriptsModel instance =
        new MostRecentlyRunScriptsModel();

    /**
     * Unique instance of the model for the list of most recently
     * run scripts.
     *
     * @return the unique instance
     */
    static public MostRecentlyRunScriptsModel getInstance() {
        return instance;
    }

    private List<String> scripts = new ArrayList<>();

    private final PropertyChangeSupport propertyChangeSupport
            = new PropertyChangeSupport(this);

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
     * @param script the script to remember in the list
     */
    public void remember(String script) {
        final List<String> oldScripts = List.copyOf(scripts);
        switch(scripts.indexOf(script)) {
        case -1:
            scripts.add(0, script);
            break;
        case 0:
            /* do nothing */
            break;
        default:
            /* move script at first position */
            scripts.remove(script);
            scripts.add(0, script);
        }
        scripts = scripts.stream().limit(10).collect(Collectors.toList());
        if (hasChanged(oldScripts, scripts)) {
            propertyChangeSupport.firePropertyChange(
                PROP_SCRIPTS,
                oldScripts,
                scripts
            );
            comboBoxModel.fireContentChanged();
        }
    }

    /**
     * Saves the list of most recently run scripts to the preferences.
     *
     * @param prefs the preferences
     */
    public void saveToPreferences(Preferences prefs) {
        prefs.putList(PREF_KEY_FILE_HISTORY, scripts);
    }

    private boolean canRun(String script) {
        final File f = new File(script);
        return f.exists() && f.isFile() && f.canRead();
    }

    private boolean hasChanged(final List<String> oldScripts, final List<String> newScripts) {
        if (oldScripts.size() != newScripts.size()) {
            return true;
        }
        for (int i=0; i < oldScripts.size(); i++) {
            if (! oldScripts.get(i).equals(newScripts.get(i))) {
                return true;
            }
        }
        return false;
    }
    /**
     * Loads the list of the most recently run scripts from the
     * preferences.
     *
     * @param prefs the preferences
     */
    public void loadFromPreferences(Preferences prefs) {
        final List<String> oldScripts = scripts;
        scripts = prefs.getList(PREF_KEY_FILE_HISTORY).stream()
            .filter(this::canRun)
            .distinct()
            .limit(10)
            .collect(Collectors.toList());
        if (hasChanged(oldScripts, scripts)) {
            propertyChangeSupport.firePropertyChange(
                PROP_SCRIPTS,
                oldScripts,
                scripts
            );
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
            super.fireContentsChanged(MostRecentlyRunScriptsModel.this,
                    0, scripts.size());
        }
        @Override
        public String getElementAt(int idx) {
            return scripts.get(idx);
        }
        @Override
        public int getSize() {
            return scripts.size();
        }
    }

    static private class RunScriptAction extends AbstractAction {
        private final String script;
        RunScriptAction(int pos, String script) {
            File f = new File(script);
            putValue(NAME, String.format("%s %s", pos, f.getName()));
            putValue(SHORT_DESCRIPTION, f.getAbsolutePath());
            putValue(SMALL_ICON,ImageProvider.get("run", ImageProvider.ImageSizes.SMALLICON));
            this.script = script;
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            RunScriptService service = new RunScriptService();
            if (!service.canRunScript(script, null /* parent */)) {
                return;
            }
            ScriptEngineDescriptor engine =
                service.deriveOrAskScriptEngineDescriptor(
                    script, null /* parent */
                );
            if (engine == null) return;
            service.runScript(script, engine);
        }
    }

    /**
     * Replies a list of actions, one run action for each script
     * in the list of most recently run scripts.
     *
     * @return the list of actions
     */
    public List<Action> getRunScriptActions() {
        List<Action> actions = new ArrayList<>();
        for(int i=0; i< scripts.size(); i++) {
            actions.add(new RunScriptAction(i+1, scripts.get(i)));
        }
        return actions;
    }
}
