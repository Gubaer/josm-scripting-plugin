package org.openstreetmap.josm.plugins.scripting.preferences;

import org.openstreetmap.josm.plugins.scripting.jsr223.JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.script.ScriptEngineFactory;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <strong>ScriptEngineComboBoxModel</strong> is an adapter for
 * {@link JSR223ScriptEngineProvider}. It provides a combo box model for the
 * script engines provided by {@link JSR223ScriptEngineProvider}.
 */
@SuppressWarnings("unused")
public class ScriptEngineComboBoxModel extends AbstractListModel<ScriptEngineDescriptor>
    implements ComboBoxModel<ScriptEngineDescriptor>{

    private ScriptEngineFactory selected;
    private final List<ScriptEngineDescriptor> descriptors = new ArrayList<>();

    public ScriptEngineComboBoxModel() {
        JSR223ScriptEngineProvider.getInstance().getScriptEngineFactories()
            .stream().map(ScriptEngineDescriptor::new)
            .collect(Collectors.toCollection(() -> descriptors));
    }

    /* ---------------------------------------------------------------------- */
    /* interface ListModel                                                    */
    /* ---------------------------------------------------------------------- */
    @Override
    public int getSize() {
        return descriptors.size();
    }

    @Override
    public ScriptEngineDescriptor getElementAt(int index) {
        return descriptors.get(index);
    }

    /* ---------------------------------------------------------------------- */
    /* interface ComboBoxModel                                                */
    /* ---------------------------------------------------------------------- */
    @Override
    public void setSelectedItem(Object anItem) {
        this.selected = (ScriptEngineFactory)anItem;
    }

    @Override
    public Object getSelectedItem() {
        return this.selected;
    }
}
