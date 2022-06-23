package org.openstreetmap.josm.plugins.scripting.ui;

import javax.validation.constraints.Null;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ScriptErrorViewerModel {
    /**
     * The name of the error property
     */
    public static final String PROP_ERROR = ScriptErrorViewerModel.class.getName() + ".errorChanged";

    private Throwable error = null;

    final private PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * Add a property change listener
     *
     * @param listener the listener
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        support.addPropertyChangeListener(listener);
    }

    /**
     * Remove a property change listener
     *
     * @param listener the listener
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        support.removePropertyChangeListener(listener);
    }

    protected void fireErrorChanged(Throwable oldError, Throwable newError) {
        if (oldError == null && newError == null) {
            // nothing changed, don't fire an event
            return;
        } else if (oldError != null && newError != null) {
            if (oldError.equals(newError)) {
                // nothing changed, don't fire an event
                return;
            }
        }
        final var event = new PropertyChangeEvent(
            this,
            PROP_ERROR,
            oldError,
            newError
        );
        support.firePropertyChange(event);
    }

    public ScriptErrorViewerModel() {
    }

    /**
     * Sets the current error
     *
     * @param error the error
     */
    public void setError(@Null Throwable error) {
        final var oldError = this.error;
        this.error = error;
        fireErrorChanged(oldError, this.error);
    }

    /**
     * Clears the current error
     */
    public void clearError() {
        setError(null);
    }

    /**
     * Replies the currently set error or null
     *
     * @return the error
     */
    public @Null Throwable getError() {
        return error;
    }
}
