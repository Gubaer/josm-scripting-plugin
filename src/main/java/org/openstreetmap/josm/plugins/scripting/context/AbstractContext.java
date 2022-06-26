package org.openstreetmap.josm.plugins.scripting.context;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract implementation of {@link org.openstreetmap.josm.plugins.scripting.context.IContext}.
 */


abstract public class AbstractContext implements IContext {

    final private String id;
    final private String displayName;
    final private ScriptEngineDescriptor engine;
    final private boolean isDefault;


    /**
     * Creates a new context.
     *
     * @param id the id
     * @param displayName the display name
     * @param engine the engine
     * @param isDefault true if this is the default context
     * @throws NullPointerException - if <code>id</code> is null
     * @throws NullPointerException - if <code>displayName</code> is null
     * @throws NullPointerException - if <code>engine</code> is null
     * @throws IllegalArgumentException - if <code>id</code> is blank
     * @throws IllegalArgumentException - if <code>displayName</code> is blank
     */
    public AbstractContext(@NotNull final String id,
                           @NotNull final String displayName,
                           @NotNull final ScriptEngineDescriptor engine,
                           final boolean isDefault) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(displayName);
        Objects.requireNonNull(engine);

        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        this.id = id;
        this.displayName = displayName;
        this.engine = engine;
        this.isDefault = isDefault;
    }

    /**
     * Creates a new non-default context with a new random {@link UUID} as id.
     *
     * @param displayName the display name
     * @param engine the engine
     * @throws NullPointerException - if <code>displayName</code> is null
     * @throws NullPointerException - if <code>engine</code> is null
     * @throws IllegalArgumentException - if <code>id</code> is blank
     * @throws IllegalArgumentException - if <code>displayName</code> is blank
     */
    public AbstractContext(@NotNull final String displayName,
                           @NotNull final ScriptEngineDescriptor engine) {
        this(UUID.randomUUID().toString(), displayName, engine, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptEngineDescriptor getScriptEngine() {
        return engine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractContext that = (AbstractContext) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
