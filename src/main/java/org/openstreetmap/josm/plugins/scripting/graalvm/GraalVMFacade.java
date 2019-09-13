package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.*;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;

public class GraalVMFacade  implements IGraalVMFacade {

    private Context context;


    private void populateContext(final Context context) {
        // populate the context with the require function
        final RequireFunction require = new RequireFunction();
        context.getBindings("js").putMember("require", require);
    }

    private void grantPrivilegesToContext(final Context.Builder builder) {
        //TODO(karl): let users configure the privileges in the JOSM
        //preferences
        builder
            // default: allow everything
            .allowAllAccess(true)
            // in particular, grant the privilege to access JOSMs
            // public methods and fields and grant it the right to
            // lookup and instantiate classes provided by OpenStreetMap
           .allowHostAccess(HostAccess.ALL)
           .allowHostClassLookup(className ->
                  className.startsWith("org.openstreetmap.")
               || className.startsWith("java.")
               || className.startsWith("javax.swing")
            )
            // exclude native access
            .allowNativeAccess(false)
            // exclude launching external processes
            .allowCreateProcess(false);
    }

    private void setOptionsOnContext(final Context.Builder builder) {
        //TODO(karl): set options, i.e. js.strict, see
        // https://www.graalvm.org/docs/reference-manual/languages/js/
    }

    private void initContext() {
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

    protected ScriptEngineDescriptor buildLanguageInfo(
            final Engine engine,
            final Language info) {

        //WORKAROUND: implementation name is sometimes empty. Replace
        // with a default name in this cases
        String engineName = info.getImplementationName();
        if (engineName == null || engineName.trim().isEmpty()) {
            engineName = "GraalVM";
        }
        final ScriptEngineDescriptor desc = new ScriptEngineDescriptor(
                ScriptEngineDescriptor.ScriptEngineType.GRAALVM,
                info.getId(),                 // engineId
                engineName,                   // engineName
                info.getName(),               // languageName
                info.getDefaultMimeType(),    // contentType
                engine.getVersion(),          // engineVersion
                info.getVersion()             // languageVersion
        );
        desc.setContentMimeTypes(info.getMimeTypes());
        return desc;
    }

    protected List<ScriptEngineDescriptor> buildSupportedLanguageInfos(
        @NotNull final Engine engine) {
        return engine.getLanguages().values().stream().map(value ->
            buildLanguageInfo(engine, value)
        ).collect(Collectors.toList());
    }

    public @NotNull List<ScriptEngineDescriptor> getSupportedLanguages() {
        return buildSupportedLanguageInfos(context.getEngine());
    }

    protected void ensureEngineIdPresent(String engineId) {
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
