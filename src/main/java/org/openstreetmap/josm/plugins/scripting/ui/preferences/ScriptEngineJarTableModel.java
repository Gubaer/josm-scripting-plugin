package org.openstreetmap.josm.plugins.scripting.ui.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.scripting.jsr223.JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;

/**
 * <p><strong>ScriptEngineJarTableModel</strong> is a table model for the table of script
 * engine jars in the preferences dialog.</p>
 *
 */
public class ScriptEngineJarTableModel extends AbstractTableModel implements PreferenceKeys{
    static private final Logger logger = Logger.getLogger(ScriptEngineJarTableModel.class.getName());

    private final List<ScriptEngineJarInfo> jars = new ArrayList<>();
    private DefaultListSelectionModel selectionModel;

    public ScriptEngineJarTableModel() {
        this(null);
    }

    /**
     * Creates the model
     *
     * @param selectionModel the selection model to be used. Internally creates a selection model,
     * if null
     */
    public ScriptEngineJarTableModel(DefaultListSelectionModel selectionModel) {
        this.selectionModel = selectionModel;
        if (this.selectionModel == null) this.selectionModel = new DefaultListSelectionModel();
    }

    /**
     * <p>Replies the selection model known to this table model.</p>
     *
     * <p>Make sure that a table using an instance of this model, is also using the
     * respective selection model.</p>
     * <pre>
     *    model = new ScriptEngineJarTableModel();
     *    JTable table = new JTable(model);
     *    table.setSelectionModel(model.getSelectionModel());
     * </pre>
     * @return the selection model
     */
    public ListSelectionModel getSelectionModel() {
        return selectionModel;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        return jars.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return jars.get(row);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 1){
            jars.get(row).setJarFilePath((String)value);
            fireTableDataChanged();
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col == 1;
    }

    /**
     * <p>Restores the configured jar files from preferences.</p>
     */
    public void restoreFromPreferences() {
        jars.clear();
        Collection<String> paths = Preferences.main()
                .getList(PREF_KEY_SCRIPTING_ENGINE_JARS);
        if (paths != null) {
            paths.stream()
                .map(String::trim)
                .filter(path -> ! path.isEmpty())
                .map(ScriptEngineJarInfo::new)
                .collect(Collectors.toCollection(() -> jars));
        }
        fireTableDataChanged();
    }


    /**
     * <p>Persists the jar paths to the preferences.</p>
     */
    public void persistToPreferences() {
        List<String> paths = jars.stream()
                .map(info -> info.getJarFilePath().trim())
                .filter(path -> ! path.isEmpty())
                .collect(Collectors.toList());
        Preferences.main().putList(PREF_KEY_SCRIPTING_ENGINE_JARS, paths);
    }

    public void deleteSelected(){
        boolean updated = false;
        for (int i = jars.size() -1; i >= 0; i--){
            if (selectionModel.isSelectedIndex(i)) {
                updated = true;
                jars.remove(i);
            }
        }
        if (updated){
            fireTableDataChanged();
        }
    }

    public void addNew(){
        jars.add(new ScriptEngineJarInfo(""));
        fireTableDataChanged();
    }

    @Override
    public void fireTableDataChanged() {
        super.fireTableDataChanged();

        // propagate the new list of script engine jars to the global script
        // engine provider.
        List<File> jarfiles = jars.stream()
            .filter(info -> ! info.getJarFilePath().trim().isEmpty())
            .filter(info -> info.getStatusMessage().equals(ScriptEngineJarInfo.OK_MESSAGE))
            .map(info -> new File(info.getJarFilePath().trim()))
            .collect(Collectors.toList());

        JSR223ScriptEngineProvider.getInstance().setScriptEngineJars(jarfiles);
    }
}
