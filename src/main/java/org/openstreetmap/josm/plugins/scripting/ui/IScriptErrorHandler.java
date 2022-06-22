package org.openstreetmap.josm.plugins.scripting.ui;

import javax.validation.constraints.NotNull;

public interface IScriptErrorHandler {

    /**
     * Callback for {@link ScriptExecutor}. An implementer to log or
     * display the exception according to his context.
     *
     * @param exception the exception
     */
    void handleScriptExecutionError(@NotNull final Throwable exception);
}
