package org.openstreetmap.josm.plugins.scripting.context;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;

/**
 * Represents a scripting context hosted by a scripting engine.
 */
public interface IContext {

    /**
     * Replies true if this is the default context for the scripting
     * engine {@link #getScriptEngine()}.
     *
     * @return true if this is the default context
     */
    boolean isDefault();

    /**
     * The contexts unique id.
     *
     * @return the unique id
     */

    @NotNull String getId();
    /**
     * Replies the contexts display name.
     *
     * @return the contexts display name
     */
    @NotNull String getDisplayName();

    /**
     * Replies the descriptor of the script engine which hosts the
     * context.
     *
     * @return the script engine descriptor
     */
    @NotNull ScriptEngineDescriptor getScriptEngine();
}
