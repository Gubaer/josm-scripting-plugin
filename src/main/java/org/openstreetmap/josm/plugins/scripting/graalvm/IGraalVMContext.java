package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.Context;
import org.openstreetmap.josm.plugins.scripting.context.IContext;

import javax.validation.constraints.NotNull;

public interface IGraalVMContext extends IContext {

    /**
     * Replies the {@link Context polyglot context}
     *
     * @return the polyglot context
     */
    @NotNull
    Context getPolyglotContext();
}
