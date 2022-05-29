package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.*;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.preferences.graalvm.GraalVMPrivilegesModel;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;

public class GraalVMFacade  implements IGraalVMFacade {
    static private final Logger logger =
        Logger.getLogger(GraalVMFacade.class.getName());

    static final private Map<String, TypeResolveFunction> pluginObject =
        Collections.singletonMap("type", new TypeResolveFunction());

    private Context context;

    private void populateContext(final Context context) {
        final Value bindings = context.getBindings("js");

        // populate the context with the require function
        bindings.putMember("require", new RequireFunction());
        // populate the context with the object 'Plugin'. 'Plugin' offers
        // a member function 'type()' to access java classes from the plugin
        // which can't be accessed with 'Java.type()' because of class
        // loading issues.
        bindings.putMember("Plugin", pluginObject);
    }

    private void grantPrivilegesToContext(final Context.Builder builder) {
        // NOTE: allowAllAccess has to be true. If false, the require()
        // function can't be invoked from JavaScript scripts.
        builder
            .allowHostAccess(HostAccess.ALL)
            .allowHostClassLookup(className -> true);

        GraalVMPrivilegesModel.getInstance().prepareContextBuilder(builder);
    }

    private void setOptionsOnContext(final Context.Builder builder) {
        builder.option("js.strict", "true");
    }

    /**
     *
     * @throws IllegalStateException throw, if no language and polyglot
     *  implementation was found on the classpath
     */
    private void initContext() throws IllegalStateException{
        // currently GraalVM is only used for JavaScript
        final Context.Builder builder = Context.newBuilder("js");
        grantPrivilegesToContext(builder);
        setOptionsOnContext(builder);
        context = builder.build();
        populateContext(context);
        context.enter();
    }

    public GraalVMFacade() {
        initContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetContext() {
        if (context != null) {
            context.leave();
            context.close(true /* cancelIfExecuting */);
        }
        initContext();
    }

    private ScriptEngineDescriptor buildDescriptorForGraalVMBasedEngine(
            final Engine engine,
            final Language info) {

        final ScriptEngineDescriptor desc = new ScriptEngineDescriptor(
            ScriptEngineDescriptor.ScriptEngineType.GRAALVM,
            info.getId(),                    // engineId
            engine.getImplementationName(),  // engineName
            info.getName(),                  // languageName
            info.getDefaultMimeType(),       // contentType
            engine.getVersion(),             // engineVersion
            info.getVersion()                // languageVersion
        );
        desc.setContentMimeTypes(info.getMimeTypes());
        return desc;
    }

    /**
     * Replies descriptors for languages supported by the GraalVM engine
     *
     * @param engine the engine
     * @return descriptors for GraalVM based engines
     */
    public @NotNull List<ScriptEngineDescriptor> buildDescriptorsForGraalVMBasedEngines(
        @NotNull final Engine engine) {
        return engine.getLanguages().values().stream().map(value ->
            buildDescriptorForGraalVMBasedEngine(engine, value)
        ).collect(Collectors.toList());
    }

    /**
     * @inheritDoc
     */
    public @NotNull List<ScriptEngineDescriptor> getScriptEngineDescriptors() {
        return buildDescriptorsForGraalVMBasedEngines(context.getEngine());
    }

    private void ensureEngineIdPresent(String engineId) {
        if (engineId.trim().isEmpty()) {
            throw new IllegalArgumentException(tr(
                "script engine descriptor doesn''t provide an engine id "
              + "name, got {0}", engineId));
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object eval(@NotNull final ScriptEngineDescriptor desc,
                     @NotNull final String script)
                     throws GraalVMEvalException {
        Objects.requireNonNull(desc);
        Objects.requireNonNull(script);
        final String engineId = desc.getEngineId();
        ensureEngineIdPresent(engineId);
        try {
            return context.eval(engineId, script);
        } catch(PolyglotException e) {
            final String message = MessageFormat.format(
                tr("failed to eval script"), script
            );
            logger.log(Level.INFO, e.getMessage(), e);
            throw new GraalVMEvalException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object eval(@NotNull final ScriptEngineDescriptor desc,
                     @NotNull final File script)
                    throws IOException, GraalVMEvalException {
        final String engineId = desc.getEngineId();
        ensureEngineIdPresent(engineId);
        Source source = Source.newBuilder(engineId, script).build();
        try {
            return context.eval(source);
        } catch(PolyglotException e) {
            final String message = MessageFormat.format(
                tr("failed to eval script in file {0}"), script
            );
            throw new GraalVMEvalException(message, e);
        }
    }
}
