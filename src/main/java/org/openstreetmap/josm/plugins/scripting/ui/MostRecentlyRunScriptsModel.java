package org.openstreetmap.josm.plugins.scripting.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * <p>This model manages a list of most recently run scripts.</p>
 *
 * <p>Use {@link #remember()} to remember an entry.</p>
 *
 * <p>There is a unique instance of this model which can be connected to
 * either an {@link Observer} or a {@link ComboBox}.</p>
 *
 */
public class MostRecentlyRunScriptsModel extends Observable
    implements PreferenceKeys{

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

    /**
     * Remembers a script in the list of most recently run scripts
     *
     * @param script
     */
    public void remember(String script) {
        switch(scripts.indexOf(script)) {
        case -1:
            //System.out.println("remembering script: " + script);
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
        setChanged();
        notifyObservers();
        comboBoxModel.fireContentChanged();
    }

    /**
     * Saves the list of most recently run scripts to the preferences.
     *
     * @param prefs the preferences
     */
    public void saveToPreferences(Preferences prefs) {
        prefs.getList(PREF_KEY_FILE_HISTORY, scripts);
    }

    protected boolean canRun(String script) {
        final File f = new File(script);
        return f.exists() && f.isFile() && f.canRead();
    }

    /**
     * Loads the list of the most recently run scripts from the
     * preferences.
     *
     * @param prefs the preferences
     */
    public void loadFromPreferences(Preferences prefs) {
        scripts = prefs.getList(PREF_KEY_FILE_HISTORY).stream()
            .filter(s->canRun(s))
            .distinct()
            .limit(10)
            .collect(Collectors.toList());
        setChanged();
        notifyObservers();
        comboBoxModel.fireContentChanged();
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

    @SuppressWarnings("serial")
    private class ComboBoxModel extends DefaultComboBoxModel<String> {
        public void fireContentChanged() {
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

    @SuppressWarnings("serial")
    static private class RunScriptAction extends AbstractAction {
        private final String script;
        public RunScriptAction(int pos, String script) {
            File f = new File(script);
            putValue(NAME, String.format("%s %s", pos, f.getName()));
            putValue(SHORT_DESCRIPTION, f.getAbsolutePath());
            putValue(SMALL_ICON,ImageProvider.get("run"));
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
