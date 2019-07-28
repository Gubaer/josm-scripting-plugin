package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Language;
import org.graalvm.polyglot.Source;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;

public class GraalVMFacade  implements IGraalVMFacade {

    private Context context;

    public GraalVMFacade() {
        // TODO(karl): initialize context
        context = Context.create();
        context.enter();
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
                engineName, // engineName
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
     * Evaluate a script in language <code>desc.getLanguageName()</code> in
     * the GraalVM.
     *
     * @param desc the script engine descriptor
     * @param script the script
     */
    public void eval(@NotNull final ScriptEngineDescriptor desc,
                     @NotNull final String script) {
        Objects.requireNonNull(desc);
        Objects.requireNonNull(script);
        final String engineId = desc.getEngineId();
        ensureEngineIdPresent(engineId);
        context.eval(engineId, script);
    }

    /**
     * Evaluate a script file in language <code>desc.getLanguageName()</code> in
     * the GraalVM.
     *
     * @param desc the script engine descriptor
     * @param script the script file
     */
    public void eval(@NotNull final ScriptEngineDescriptor desc,
                     @NotNull final File script) throws IOException {
        final String engineId = desc.getEngineId();
        ensureEngineIdPresent(engineId);
        Source source = Source.newBuilder(engineId, script).build();
        context.eval(source);
    }
}
