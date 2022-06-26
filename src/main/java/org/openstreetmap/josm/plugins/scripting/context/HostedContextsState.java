package org.openstreetmap.josm.plugins.scripting.context;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HostedContextsState implements IHostedContextsState {
    final private ScriptEngineDescriptor engine;
    final private IContext defaultContext;
    final private List<IContext> userDefinedContexts;

    /**
     * Creates a state.
     *
     * @param engine the engine
     * @param defaultContext the default context
     * @param userDefinedContexts the user defined contexts
     * @throws NullPointerException - if <code>engine</code> is null
     */
    public HostedContextsState(@NotNull final ScriptEngineDescriptor engine,
                               @Null final IContext defaultContext,
                               @Null final List<IContext> userDefinedContexts) {
        Objects.requireNonNull(engine);
        this.engine = engine;
        this.defaultContext = defaultContext;
        this.userDefinedContexts = userDefinedContexts == null
            ? List.of()
            : Collections.unmodifiableList(userDefinedContexts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptEngineDescriptor getEngine() {
        return engine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IContext getDefaultContext() {
        return defaultContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IContext> getUserDefinedContexts() {
        return userDefinedContexts;
    }
}
