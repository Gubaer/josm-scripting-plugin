package org.openstreetmap.josm.plugins.scripting.context;

import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;
import org.openstreetmap.josm.plugins.scripting.graalvm.IGraalVMContext;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor.ScriptEngineType.GRAALVM;

public class ContextRegistry implements IContextRegistry {
    static private final Logger logger = Logger.getLogger(ContextRegistry.class.getName());

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final Map<ScriptEngineDescriptor, IContext> defaultContexts = new HashMap<>();
    private final Map<ScriptEngineDescriptor, List<IContext>> userDefinedContexts = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public @Null IContext lookupDefaultContext(@NotNull final ScriptEngineDescriptor engine) {
        Objects.requireNonNull(engine);
        return defaultContexts.get(engine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Null IContext lookupUserDefinedContext(@NotNull final String id, @NotNull final ScriptEngineDescriptor engine) {
        Objects.requireNonNull(id);
        ensureValidEngine(engine);
        return userDefinedContexts.getOrDefault(engine, List.of()).stream()
            .filter(context -> context.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<IContext> lookupUserDefinedContexts(@NotNull final ScriptEngineDescriptor engine) {
        Objects.requireNonNull(engine);
        final var l = userDefinedContexts.get(engine);
        return l == null ? List.of() : List.copyOf(l);
    }

    private void ensureValidDisplayName(String displayName) {
        Objects.requireNonNull(displayName);
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
    }

    private void rememberContext(ScriptEngineDescriptor engine, IContext context) {
        userDefinedContexts.putIfAbsent(engine, new ArrayList<>());
        userDefinedContexts.get(engine).add(context);
    }

    private IHostedContextsState captureHostedContextsState(ScriptEngineDescriptor engine) {
        return new HostedContextsState(
            engine,
            defaultContexts.get(engine),
            this.userDefinedContexts.get(engine)
        );
    }

    private void ensureValidEngine(ScriptEngineDescriptor engine) {
        Objects.requireNonNull(engine);
        switch(engine.getEngineType()) {
            case GRAALVM:
                if (!"js".equals(engine.getEngineId())) {
                    throw new IllegalArgumentException(MessageFormat.format(
                        "Unsupported GraalVM engine. Currently only supports ''js'', got ''{0}''",
                        engine.getEngineId()
                    ));
                }
                break;
            case PLUGGED: //TODO(gubaer): fix later when JSR223 is supported too
            case EMBEDDED:
                throw new IllegalArgumentException(MessageFormat.format(
                    "Unsupported engine type. Currently only supports ''{0}'', got ''{1}''",
                    GRAALVM,
                    engine.getEngineType()
                ));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull IContext createUserDefinedContext(@NotNull final String displayName, @NotNull final ScriptEngineDescriptor engine) {
        ensureValidDisplayName(displayName);
        ensureValidEngine(engine);
        var oldState = captureHostedContextsState(engine);
        final IContext context;
        switch(engine.getEngineType()) {
            case GRAALVM:
                context = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
                    .createContext(displayName, engine);
                break;
            case PLUGGED: //TODO(gubaer): implement later
            default:
                // should not happen. ensureValidEngine() already makes sure we don't land
                // here.
                throw new UnsupportedOperationException();
        }
        rememberContext(engine, context);
        var newState = captureHostedContextsState(engine);
        final var event = new HostedContextsChangeEvent(
            this,
            PROP_HOSTED_CONTEXTS,
            oldState,
            newState
        );
        support.firePropertyChange(event);
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeContext(final IContext context) {
        Objects.requireNonNull(context);
        final var oldState = captureHostedContextsState(context.getScriptEngine());
        final var engine = context.getScriptEngine();
        switch(engine.getEngineType()) {
            case GRAALVM:
                final IGraalVMContext graalVMContext = (IGraalVMContext) context;
                GraalVMFacadeFactory.getOrCreateGraalVMFacade()
                    .closeAndRemoveContext(graalVMContext);
                break;
            case PLUGGED: //TODO(gubaer): implement later
            default:
                // should not happen. ensureValidEngine() already makes sure we don't land
                // here.
                throw new UnsupportedOperationException();
        }

        // remove the context from the user defined contexts
        userDefinedContexts.getOrDefault(engine, new ArrayList<>()).remove(context);
        if (logger.isLoggable(Level.FINE)) {
            var ctx = lookupUserDefinedContext(context.getId(), engine);
            logger.fine(MessageFormat.format(
                    "Lookup of context with id ''{0}'': ''{1}''",
                context.getId(),
                ctx
            ));
        }

        // if it is a default context, remove it from the list of default
        // contexts
        defaultContexts.values().stream()
            .filter(ctx -> ctx.getId().equals(context.getId()))
            .findFirst()
            .ifPresent(ctx -> {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, MessageFormat.format(
                        "Removing default context ''{0}/{1}'' for engine ''{2}''",
                        ctx.getId(),
                        ctx.getDisplayName(),
                        ctx.getScriptEngine()
                    ));
                }
                defaultContexts.remove(ctx.getScriptEngine());
            });

        // notify listeners
        final var newState = captureHostedContextsState(context.getScriptEngine());
        final var event = new HostedContextsChangeEvent(
            this,
            PROP_HOSTED_CONTEXTS,
            oldState,
            newState
        );
        support.firePropertyChange(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        support.addPropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        support.removePropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Null IContext getOrCreateDefaultContext(@NotNull final ScriptEngineDescriptor engine) {
        ensureValidEngine(engine);
        var oldState = captureHostedContextsState(engine);
        var context = defaultContexts.get(engine);
        if (context != null) {
            return context;
        }
        switch(engine.getEngineType()) {
            case GRAALVM:
                context = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
                        .getOrCreateDefaultContext(engine);
                break;
            case PLUGGED: //TODO(gubaer): implement later
            default:
                // should not happen. ensureValidEngine() ensures that we don't end up
                // here
        }
        defaultContexts.put(engine, context);
        var newState = captureHostedContextsState(engine);
        final var event = new HostedContextsChangeEvent(
            this,
            PROP_HOSTED_CONTEXTS,
            oldState,
            newState
        );
        support.firePropertyChange(event);
        return context;
    }
}
