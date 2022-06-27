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
    public Context getPolyglotContext() {
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object eval(@NotNull final String script) throws GraalVMEvalException {
        Objects.requireNonNull(script);
        try {
            getPolyglotContext().enter();
            final var source = Source.newBuilder(getScriptEngine().getEngineId(), script, null)
                .mimeType("application/javascript+module")
                .build();
            return context.eval(source);
        } catch(IOException e) {
            // shouldn't happen because we don't load the script from a file,
            // but just in case
            final var message = tr("Failed to create ECMAScript source object");
            logger.log(Level.SEVERE, message, e);
            throw new GraalVMEvalException(message, e);
        } catch(PolyglotException e) {
            final String message = MessageFormat.format(
                tr("failed to eval script"), script
            );
            logger.log(Level.INFO, e.getMessage(), e);
            throw new GraalVMEvalException(message, e);
        } finally {
            getPolyglotContext().leave();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object eval(@NotNull final File scriptFile) throws GraalVMEvalException, IOException {
        Objects.requireNonNull(scriptFile);
        try {
            getPolyglotContext().enter();
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
            getPolyglotContext().leave();
        }
    }
}
