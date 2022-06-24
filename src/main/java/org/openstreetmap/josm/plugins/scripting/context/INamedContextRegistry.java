package org.openstreetmap.josm.plugins.scripting.context;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.beans.PropertyChangeListener;
import java.util.List;

public interface INamedContextRegistry {

    String PROP_NAMED_CONTEXTS = INamedContext.class.getName() + ".namedContexts";

    /**
     * Replies the named context with name <code>name</code> hosted by the
     * scripting engine <code>desc</code>.
     *
     * @param name the name
     * @param engine the scripting engine
     * @return the named context. Null if no such context is available in
     *   the registry
     * @throws NullPointerException - if <code>name</code> is null
     * @throws NullPointerException - if <code>engine</code> is null
     */
    @Null INamedContext lookupNamedContext(
        @NotNull final String name,
        @NotNull final ScriptEngineDescriptor engine);

    /**
     * Replies the list contexts hosted by the scripting engine
     * <code>engine</code>, including the default context and zero or more named
     * contexts.
     *
     * @param engine the scripting engine
     * @return the list of named context. An empty list if no such contexts
     *  are registered
     * @throws NullPointerException - if <code>engine</code> is null
     */
    @NotNull List<INamedContext> lookupContexts(@NotNull final ScriptEngineDescriptor engine);

    /**
     * Registers a named context in the registry.
     *
     * @param context the context
     * @throws NullPointerException - if <code>context</code> is null
     */
    void registerNamedContext(@NotNull final INamedContext context);

    /**
     * Removes a named context from the registry.
     *
     * @param context the context to be removed. Ignored if null.
     */
    void removeNamedContext(@Null final INamedContext context);

    /**
     * Adds a property change listener which is notified when the list
     * of registered named contexts changes.
     *
     * @param listener the listener. Ignored if null.
     */
    void addPropertyChangeListener(@Null final PropertyChangeListener listener);

    /**
     * Removes a property change listener.
     *
     * @param listener the listener. Ignored if null.
     */
    void removePropertyChangeListener(@Null final PropertyChangeListener listener);
}
