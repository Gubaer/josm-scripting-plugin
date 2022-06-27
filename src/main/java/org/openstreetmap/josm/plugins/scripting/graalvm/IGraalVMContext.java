package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.openstreetmap.josm.plugins.scripting.context.IContext;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;

public interface IGraalVMContext extends IContext {

    /**
     * Replies the {@link Context polyglot context}
     *
     * @return the polyglot context
     */
    @NotNull
    Context getPolyglotContext();

    /**
     * Evaluates a script in the {@link #getPolyglotContext() polyglot context}.
     *
     * @param script the script
     * @throws NullPointerException - if <code>script</code> is null
     * @throws GraalVMEvalException - if an error occurs when the script is executed.
     */
    Object eval(@NotNull final String script) throws GraalVMEvalException;

    /**
     * Evaluates the content of a script file in the {@link #getPolyglotContext()} polyglot context}.
     *
     * @param scriptFile the script file
     * @throws GraalVMEvalException - if an error occurs when the script is executed.
     * @throws IOException - if the file doesn't exist or isn't readable
     * @throws NullPointerException - if <code>scriptFile</code> is null
     */
    Object eval(@NotNull final File scriptFile) throws GraalVMEvalException, IOException;
}
