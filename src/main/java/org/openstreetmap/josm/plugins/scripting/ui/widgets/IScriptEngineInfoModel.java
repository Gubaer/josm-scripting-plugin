package org.openstreetmap.josm.plugins.scripting.ui.widgets;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.Null;
import java.beans.PropertyChangeListener;

public interface IScriptEngineInfoModel {

    /**
     * The current script engine as described by a
     * {@link ScriptEngineDescriptor}. The value is a
     * {@link ScriptEngineDescriptor} or null. */
    String PROP_SCRIPT_ENGINE  = IScriptEngineInfoModel.class.getName() + ".scriptEngine";

    void addPropertyChangeListener(PropertyChangeListener listener);
    void removePropertyChangeListener(PropertyChangeListener listener);

    void setEngine(@Null final ScriptEngineDescriptor engine);

    @Null ScriptEngineDescriptor getEngine();
}
