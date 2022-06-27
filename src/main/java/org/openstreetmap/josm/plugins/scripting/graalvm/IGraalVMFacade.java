package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
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
    List<ScriptEngineDescriptor> getScriptEngineDescriptors();

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


    /**
     * Creates a new context hosted by the engine <code>engine</code>.
     *
     * @param displayName the display name for the context
     * @param engine the engine
     * @throws NullPointerException - if <code>engine</code> is null
     * @throws NullPointerException - if <code>displayName</code> is null
     * @throws IllegalArgumentException  - if <code>displayName</code> is blank
     * @throws IllegalArgumentException - if <code>engine</code> doesn't describe a GraalVM
     *   engine or if the described engine is not available
     * @return the new context
     */
    @NotNull IGraalVMContext createContext(@NotNull final String displayName, @NotNull final ScriptEngineDescriptor engine);

    /**
     * Closes a context and removes it.
     *
     * @param context the context
     * @throws NullPointerException - if <code>context</code> is null
     */
    void closeAndRemoveContext(@NotNull final IGraalVMContext context);

    /**
     * Creates the default context for the engine <code>engine</code>. If the default
     * context already exists, replies the existing default context.
     *
     * @param engine the engine
     * @throws NullPointerException - if <code>engine</code> is null
     * @throws IllegalArgumentException - if <code>engine</code> doesn't describe a GraalVM
     *   engine or if the described engine is not available
     */
    IGraalVMContext getOrCreateDefaultContext(@NotNull final ScriptEngineDescriptor engine);

    /**
     * Replies the context with id <code>id</code> hosted by the engine <code>engine</code>.
     *
     * @param id the id
     * @param engine the engine
     * @return the context or null, if no such context exists
     * @throws NullPointerException - if <code>engine</code> is null
     * @throws NullPointerException - if <code>is</code> is null
     * @throws IllegalArgumentException - if <code>engine</code> doesn't describe a GraalVM
     *   engine or if the described engine is not available
     */
    public @Null IGraalVMContext lookupContext(@NotNull final String id, @NotNull final ScriptEngineDescriptor engine);
}
