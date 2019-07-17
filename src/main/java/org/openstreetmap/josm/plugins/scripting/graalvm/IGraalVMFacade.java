package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;
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
}
