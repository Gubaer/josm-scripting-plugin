package org.openstreetmap.josm.plugins.scripting.context;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.beans.PropertyChangeListener;
import java.util.List;

public interface IContextRegistry {

    String PROP_NAMED_CONTEXTS = IContext.class.getName() + ".namedContexts";

    /**
     * Replies the default context for the scripting engine <code>engine</code>.
     *
     * @return the default context or null, if no such context exists
     * @throws NullPointerException - if <code>engine</code> is null
     */
    @Null
    IContext lookupDefaultContext(@NotNull final ScriptEngineDescriptor engine);

    /**
     * Replies the context with id <code>id</code> hosted by the
     * scripting engine <code>desc</code>.
     *
     * @param id the contexts id
     * @param engine the scripting engine
     * @return the named context. Null if no such context is available in
     *   the registry
     * @throws NullPointerException - if <code>name</code> is null
     * @throws NullPointerException - if <code>engine</code> is null
     */
    @Null
    IContext lookupContext(
        @NotNull final String id,
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
    @NotNull List<IContext> lookupContexts(@NotNull final ScriptEngineDescriptor engine);

    /**
     * Creates, initializes, registers, and replies a context hosted by the
     * engine <code>engine</code>.
     *
     * @param displayName the display name
     * @param engine the engine
     * @throws NullPointerException - if <code>displayName</code> is null
     * @throws NullPointerException - if <code>engine</code> is null
     * @throws IllegalArgumentException - if <code>displayName</code> is empty or blank
     */
    @NotNull IContext createContext(@NotNull final String displayName, @NotNull final ScriptEngineDescriptor engine);

    /**
     * Removes a named context from the registry.
     *
     * @param context the context to be removed. Ignored if null.
     */
    void removeContext(@Null final IContext context);

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
