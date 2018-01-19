package org.openstreetmap.josm.plugins.scripting.preferences;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngineFactory;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import org.openstreetmap.josm.plugins.scripting.model.JSR223ScriptEngineProvider;

/**
 * <strong>ScriptEngineComboBoxModel</strong> is an adapter for {@link JSR223ScriptEngineProvider}.
 * It provides a combo box model for the script engines provided by {@link JSR223ScriptEngineProvider}.
 */
public class ScriptEngineComboBoxModel extends AbstractListModel<ScriptEngineFactory>
    implements ComboBoxModel<ScriptEngineFactory>{

    private ScriptEngineFactory selected;
    private final List<ScriptEngineFactory> factories = new ArrayList<ScriptEngineFactory>();

    public ScriptEngineComboBoxModel() {
        factories.addAll(JSR223ScriptEngineProvider.getInstance().getScriptEngineFactories());
    }

    /* ---------------------------------------------------------------------------- */
    /* interface ListModel                                                          */
    /* ---------------------------------------------------------------------------- */
    @Override
    public int getSize() {
        return factories.size();
    }

    @Override
    public ScriptEngineFactory getElementAt(int index) {
        return factories.get(index);
    }

    /* ---------------------------------------------------------------------------- */
    /* interface ComboBoxModel                                                      */
    /* ---------------------------------------------------------------------------- */
    @Override
    public void setSelectedItem(Object anItem) {
        this.selected = (ScriptEngineFactory)anItem;
    }

    @Override
    public Object getSelectedItem() {
        return this.selected;
    }
}
