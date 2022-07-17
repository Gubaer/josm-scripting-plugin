package org.openstreetmap.josm.plugins.scripting.ui.runner;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.widgets.IScriptEngineInfoModel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class RunScriptDialogModel implements IScriptEngineInfoModel {

    private ScriptEngineDescriptor engine = null;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEngine(ScriptEngineDescriptor engine) {
        final var oldEngine = this.engine;
        this.engine = engine;
        support.firePropertyChange(IScriptEngineInfoModel.PROP_SCRIPT_ENGINE, oldEngine, this.engine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptEngineDescriptor getEngine() {
        return engine;
    }
}
