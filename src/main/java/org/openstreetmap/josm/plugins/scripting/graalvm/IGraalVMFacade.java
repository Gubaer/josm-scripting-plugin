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
     *  supported
     */
     @NotNull List<ScriptEngineDescriptor> getSupportedLanguages();

    /**
     * Evaluate a script in language <code>desc.getLanguageName()</code>in
     * the GraalVM.
     *
     * @param desc the script engine descriptor
     * @param script the script
     */
     void eval(@NotNull ScriptEngineDescriptor desc, @NotNull String script);


    /**
     * Evaluate a script file in language <code>desc.getLanguageName()</code> in
     * the GraalVM.
     *
     * @param desc the script engine descriptor
     * @param script the script file
     */
    void eval(@NotNull final ScriptEngineDescriptor desc,
              @NotNull final File script) throws IOException;
}
