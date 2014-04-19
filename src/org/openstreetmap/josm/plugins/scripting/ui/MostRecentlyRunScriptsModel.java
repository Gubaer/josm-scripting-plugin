package org.openstreetmap.josm.plugins.scripting.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

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
    
    static private MostRecentlyRunScriptsModel instance;
    /**
     * Unique instance of the model for the list of most recently
     * run scripts.
     * 
     * @return the unique instance 
     */
    static public MostRecentlyRunScriptsModel getInstance() {
        if (instance == null) {
            instance = new MostRecentlyRunScriptsModel();
        }
        return instance;
    }
    
    final private List<String> scripts = new ArrayList<String>();
    
    /**
     * Remembers a script in the list of most recently run scripts 
     * 
     * @param script
     */
    public void remember(String script) {
        switch(scripts.indexOf(script)) {
        case -1: 
            System.out.println("remebering script: " + script);
            scripts.add(script);
            break;
        case 0:
            /* do nothing */
            break;
        default:
            /* move script at first position */
            scripts.remove(script);
            scripts.add(0, script);
        }      
        int s;
        while((s = scripts.size()) > 10) {
            scripts.remove(s-1);
        }
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
        prefs.putCollection(PREF_KEY_FILE_HISTORY, scripts);
    }
    
    protected boolean canRun(String script) {
        File f = new File(script);
        return f.exists() && f.isFile() && f.canRead();
    }
    
    /**
     * Loads the list of the most recently run scripts from the
     * preferences.
     * 
     * @param prefs the preferences 
     */
    public void loadFromPreferences(Preferences prefs) {
        Collection<String> entries = prefs.getCollection(
                PREF_KEY_FILE_HISTORY
        );
        scripts.clear();
        int n = Math.min(10, entries.size());
        int i = 0;
        for(String script: entries) {
           if (i >= n) break;
           if (!canRun(script)) continue;
           if (scripts.contains(script)) continue;
           scripts.add(script);
        }
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
    public DefaultComboBoxModel<?> getComboBoxModel() {
        return comboBoxModel;
    }
    private ComboBoxModel comboBoxModel = new ComboBoxModel();
    
    @SuppressWarnings("serial")
    private class ComboBoxModel extends DefaultComboBoxModel<Object> {
        public void fireContentChanged() {
            super.fireContentsChanged(MostRecentlyRunScriptsModel.this,
                    0, scripts.size());
        }
        @Override
        public Object getElementAt(int idx) {
            return scripts.get(idx);
        }
        @Override
        public int getSize() {
            return scripts.size();
        }        
    }
    
    @SuppressWarnings("serial")
    static private class RunScriptAction extends AbstractAction {
        private String script;
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
        List<Action> actions = new ArrayList<Action>();
        for(int i=0; i< scripts.size(); i++) {
            actions.add(new RunScriptAction(i+1, scripts.get(i)));
        }
        return actions;
    }
}
