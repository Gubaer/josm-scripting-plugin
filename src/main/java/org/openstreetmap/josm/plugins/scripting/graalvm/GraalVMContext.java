package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.openstreetmap.josm.plugins.scripting.context.AbstractContext;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Default implementation of {@link IGraalVMContext}.
 */
public class GraalVMContext extends AbstractContext implements IGraalVMContext {
    static private final Logger logger = Logger.getLogger(GraalVMContext.class.getName());

    final private Context context;

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
    public Object eval(@NotNull final String script) throws GraalVMEvalException {
        Objects.requireNonNull(script);
        try {
            context.enter();
            // build a unique name for this source. This forces GraalJS to parse and evaluate the source.
            // The source isn't cached, though, because we expect it to be evaluated only once.
            final var sourceName = MessageFormat.format("{0}-{1}", getDisplayName(), UUID.randomUUID());
            final var source = Source.newBuilder(getScriptEngine().getEngineId(), script, sourceName)
                .cached(false)
                .mimeType("application/javascript+module")
                .build();
            return context.eval(source);
        } catch(IOException e) {
            // shouldn't happen because we don't load the script from a file,
            // but just in case
            final var message = tr("Failed to create ECMAScript source object");
            throw new GraalVMEvalException(message, e);
        } catch(Throwable e) {
            // this includes PolyglotException
            final String message = tr("Failed to evaluate script");
            throw new GraalVMEvalException(message, e);
        } finally {
            context.leave();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object eval(@NotNull final File scriptFile) throws GraalVMEvalException, IOException {
        Objects.requireNonNull(scriptFile);
        try {
            context.enter();
            final Source source = Source.newBuilder(getScriptEngine().getEngineId(), scriptFile)
                .mimeType("application/javascript+module")
                .build();
            return context.eval(source);
        } catch(PolyglotException e) {
            final String message = MessageFormat.format(
                tr("Failed to eval script in file ''{0}''"), scriptFile
            );
            throw new GraalVMEvalException(message, e);
        } finally {
            context.leave();
        }
    }

    @Override
    public void close() {
        try {
            context.close(true);
        } catch(IllegalStateException e) {
            logger.log(Level.WARNING, MessageFormat.format(
                "Failed to close GraalVM context ''{0}/{1}''",
                getId(),
                getDisplayName()
            ),e);
        }
    }
}
