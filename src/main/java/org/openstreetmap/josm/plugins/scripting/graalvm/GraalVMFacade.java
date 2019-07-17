package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Language;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;


import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraalVMFacade  implements IGraalVMFacade {

    private Context context = null;

    public GraalVMFacade() {
        // TODO(karl): initialize context
        context = Context.create();
        context.enter();
    }

    protected ScriptEngineDescriptor buildLanguageInfo(
            final String name,
            final Language info) {
        ScriptEngineDescriptor desc = new ScriptEngineDescriptor(
                ScriptEngineDescriptor.ScriptEngineType.GRAALVM,
                info.getId(),
                info.getName(),
                info.getImplementationName(),
                info.getDefaultMimeType());
        return desc;
    }
    protected List<ScriptEngineDescriptor> buildSupportedLanguageInfos(
        @NotNull final Engine engine) {
        final Map<String, Language> languages = engine.getLanguages();
        return languages.entrySet().stream().map(entry ->
            buildLanguageInfo(entry.getKey(), entry.getValue())
        ).collect(Collectors.toList());
    }

    public @NotNull List<ScriptEngineDescriptor> getSupportedLanguages() {
        return buildSupportedLanguageInfos(context.getEngine());
    }
}
