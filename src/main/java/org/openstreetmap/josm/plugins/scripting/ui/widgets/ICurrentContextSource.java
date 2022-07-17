package org.openstreetmap.josm.plugins.scripting.ui.widgets;

import org.openstreetmap.josm.plugins.scripting.context.IContext;

import javax.validation.constraints.Null;
import java.beans.PropertyChangeListener;

public interface ICurrentContextSource {

    /**
     * Property name for the currently selected context.
     */
    String PROP_SELECTED_CONTEXT = ICurrentContextSource.class.getName() + ".selectedContext";

    /**
     * Replies the currently selected scripting context or null, if
     * no context is currently selected.
     *
     * @return the currently selected scripting context
     */
    @Null IContext getSelectedContext();

    /**
     * Add a property change listener for {@link #PROP_SELECTED_CONTEXT}
     *
     * @param listener the listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a property change listener for {@link #PROP_SELECTED_CONTEXT}
     *
     * @param listener the listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
