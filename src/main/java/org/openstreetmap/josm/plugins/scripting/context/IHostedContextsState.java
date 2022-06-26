package org.openstreetmap.josm.plugins.scripting.context;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

/**
 * Information about the contexts a {@link #getEngine() scripting engine} hosts. It hosts
 * at most one {@link #getDefaultContext() default context} and zero or more
 * {@link #getUserDefinedContexts() user defined contexts}.
 */
public interface IHostedContextsState {
    /**
     * Replies the scripting engine whose hosted contexts are described.
     *
     * @return the engine
     */
    @NotNull
    ScriptEngineDescriptor getEngine();

    /**
     * Replies the default context hosted by the engine.
     *
     * @return the default context. null, if there is currently no initialized
     *   default context.
     */
    @Null
    IContext getDefaultContext();

    /**
     * Replies the list of user defined context hosted by the engine.
     *
     * @return the user defined contexts
     */
    @NotNull
    List<IContext> getUserDefinedContexts();
}