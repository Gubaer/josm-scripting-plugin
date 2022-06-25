package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.Context;
import org.openstreetmap.josm.plugins.scripting.context.AbstractContext;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

public class GraalVMContext extends AbstractContext implements IGraalVMContext {

    private Context context;

    /**
     * Creates a new GraalVM context.
     *
     * @param displayName the display name
     * @param engine the engine
     * @param context the context
     * @param isDefault true if this is the default context
     * @throws NullPointerException - if <code>displayName</code> is null
     * @throws NullPointerException - if <code>engine</code> is null
     * @throws NullPointerException - if <code>context</code> is null
     * @throws IllegalArgumentException  - if <code>displayName</code> is blank
     * @throws IllegalArgumentException - if <code>engine</code> doesn't describe a GraalVM
     *   engine or if the described engine is not available
     */
    public GraalVMContext(@NotNull final String displayName,
                          @NotNull final ScriptEngineDescriptor engine,
                          @NotNull final Context context,
                          boolean isDefault
                          ) {
        super(UUID.randomUUID().toString(), displayName, engine, isDefault);
        Objects.requireNonNull(context);
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context getPolyglotContext() {
        return context;
    }
}
