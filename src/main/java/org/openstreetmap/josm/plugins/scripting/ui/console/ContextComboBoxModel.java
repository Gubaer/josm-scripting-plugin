package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.openstreetmap.josm.plugins.scripting.context.ContextRegistry;
import org.openstreetmap.josm.plugins.scripting.context.IContext;
import org.openstreetmap.josm.plugins.scripting.context.IHostedContextsState;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.swing.*;
import javax.validation.constraints.Null;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ContextComboBoxModel extends DefaultComboBoxModel<IContext> implements PropertyChangeListener {

    private IContext defaultContext;
    private List<IContext> userDefinedContexts;
    private ScriptEngineDescriptor engine;

    /**
     * Creates a combo box model which is not yet parametrized with
     * a scripting engine.
     *
     * Use {@link #setEngine(ScriptEngineDescriptor)} to load the contexts
     * hosted by a scripting engine into the model.
     *
     * @see #setEngine(ScriptEngineDescriptor)
     * @see #ContextComboBoxModel(ScriptEngineDescriptor)
     */
    public ContextComboBoxModel(){}

    /**
     * Creates a combo box model for the contexts hosted by the scripting
     * engine <code>engine</code>.
     *
     * @param engine the engine
     */
    public ContextComboBoxModel(@Null final ScriptEngineDescriptor engine) {
        setEngine(engine);
    }

    /**
     * Replies the scripting engine.
     *
     * @return the engine
     */
    public @Null ScriptEngineDescriptor getEngine() {
        return engine;
    }

    /**
     * Sets the scripting engine whose hosted contexts are provided
     * by this combo box model.
     *
     * @param engine the engine
     */
    public void setEngine(@Null final ScriptEngineDescriptor engine) {
        this.engine = engine;
        if (this.engine != null) {
            final var registry = ContextRegistry.getInstance();
            this.defaultContext = registry.getOrCreateDefaultContext(engine);
            this.userDefinedContexts = registry.lookupUserDefinedContexts(engine);
            if (getSize() > 0) {
                this.setSelectedItem(this.defaultContext);
            }
        } else {
            this.defaultContext = null;
            this.userDefinedContexts = null;
        }
        fireContentsChanged(this, 0, getSize()-1);
    }

    /* ------------------------------------------------------------------------------- */
    /* ComboBoxModel                                                                   */
    /* ------------------------------------------------------------------------------- */
    @Override
    public int getSize() {
        if (defaultContext == null && userDefinedContexts == null) {
            return 0;
        }
        int size = 0;
        if (defaultContext != null) {
            size++;
        }
        if (userDefinedContexts != null && ! userDefinedContexts.isEmpty()) {
            // add one entry for the separator
            size++;
            size += userDefinedContexts.size();
        }
        return size;
    }

    @Override
    public IContext getElementAt(int i) {
        switch(i) {
            case 0: return defaultContext;
            case 1: return null;  // the separator
            default:
                return userDefinedContexts.get(i-2);
        }
    }

    /* ------------------------------------------------------------------------------- */
    /* PropertyChangeListener                                                          */
    /* ------------------------------------------------------------------------------- */

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!ContextRegistry.PROP_HOSTED_CONTEXTS.equals(event.getPropertyName())) {
            return;
        }
        final var newState = (IHostedContextsState)event.getNewValue();
        this.defaultContext = newState.getDefaultContext();
        this.userDefinedContexts = newState.getUserDefinedContexts();
        fireContentsChanged(this, 0, getSize()-1);
    }
}
