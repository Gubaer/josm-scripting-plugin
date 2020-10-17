package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IGraalVMFacade {

    /**
     * Replies information about the languages supported by the available
     * GraalVM.
     *
     * @return supported languages; empty list, if no scripting languages are
     * supported
     */
    @NotNull
    List<ScriptEngineDescriptor> getSupportedLanguages();

    /**
     * Evaluate a script in language <code>desc.getLanguageName()</code>in
     * the GraalVM.
     *
     * Replies a {@link org.graalvm.polyglot.Value} as result. We don't
     * declare it in the facade API because we want to load
     * <code></code>org.graalmv.*</code> dynamically.
     *
     * @param desc   the script engine descriptor
     * @param script the script
     * @return the evaluation result.
     * @throws GraalVMEvalException thrown, if the GraalVM fails to eval the
     *    script
     */
    Object eval(@NotNull ScriptEngineDescriptor desc, @NotNull String script)
        throws GraalVMEvalException;


    /**
     * Evaluate a script file in language <code>desc.getLanguageName()</code> in
     * the GraalVM.
     *
     * Replies a {@link org.graalvm.polyglot.Value} as result. We don't
     * declare it in the facade API because we want to load
     * <code></code>org.graalmv.*</code> dynamically.
     *
     * @param desc   the script engine descriptor
     * @param script the script file
     * @return the evaluation result.
     * @throws GraalVMEvalException thrown, if the GraalVM fails to eval the
     *    script
     * @throws IOException thrown, if the script file can't be read
     */
    Object eval(@NotNull final ScriptEngineDescriptor desc,
              @NotNull final File script)
        throws IOException, GraalVMEvalException;

    /**
     * Exits and discards the current context and initializes a
     * new context
     */
    void resetContext();
}
