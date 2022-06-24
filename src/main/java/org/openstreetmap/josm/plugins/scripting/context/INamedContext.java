package org.openstreetmap.josm.plugins.scripting.context;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;

public interface INamedContext {

    /**
     * Replies the context name.
     *
     * @return the context name
     */
    @NotNull String getName();

    /**
     * Replies the descriptor of the script engine which hosts the
     * context.
     *
     * @return the script engine descriptor
     */
    @NotNull ScriptEngineDescriptor getScriptEngine();
}
