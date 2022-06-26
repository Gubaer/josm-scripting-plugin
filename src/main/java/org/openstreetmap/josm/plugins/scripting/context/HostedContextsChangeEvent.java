package org.openstreetmap.josm.plugins.scripting.context;

import java.beans.PropertyChangeEvent;
import java.util.Comparator;
import java.util.stream.Collectors;

public class HostedContextsChangeEvent extends PropertyChangeEvent {
    public HostedContextsChangeEvent(Object source, String propertyName, IHostedContextsState oldValue, IHostedContextsState newValue) {
        super(source, propertyName, oldValue, newValue);
    }

    public IHostedContextsState getOldHostedContextState() {
        return (IHostedContextsState)getOldValue();
    }

    public IHostedContextsState getNewHostedContextState() {
        return (IHostedContextsState)getNewValue();
    }

    /**
     * Replies true if the default context has changed.
     *
     * @return true if the default context has changed
     */
    public boolean isDefaultContextChanged() {
        final var oldContext = getOldHostedContextState().getDefaultContext();
        final var newContext = getNewHostedContextState().getDefaultContext();
        if (oldContext == null) {
            return newContext != null;
        } else if (newContext == null) {
            return true;
        } else {
            return !oldContext.equals(newContext);
        }
    }

    /**
     * Replies true if a user defined context has been added or removed.
     *
     * @return true if a user defined context has been added or removed
     */
    public boolean isUserDefinedContextsChanged() {
        final var oldContexts = getOldHostedContextState().getUserDefinedContexts()
            .stream()
            .sorted(Comparator.comparing(IContext::getId))
            .collect(Collectors.toList());
        final var newContexts = getNewHostedContextState().getUserDefinedContexts()
            .stream()
            .sorted(Comparator.comparing(IContext::getId))
            .collect(Collectors.toList());

        if (oldContexts.size() != newContexts.size()) {
            return true;
        }
        for (int i = 0; i < oldContexts.size(); i++) {
            if (! oldContexts.get(i).getId().equals(newContexts.get(i).getId())) {
                return true;
            }
        }
        return false;
    }
}
