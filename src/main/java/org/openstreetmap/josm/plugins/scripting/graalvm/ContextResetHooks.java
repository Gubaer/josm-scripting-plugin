package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.Value;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages JavaScript callbacks to be invoked when the GraalJS context is reset.
 * <p>
 * Scripts register cleanup functions via the {@code josm/context} API module;
 * {@link GraalVMFacade#resetContext()} calls {@link #invokeAll()} before
 * closing the current context.
 * </p>
 * <p>
 * An instance of this class is exposed in the GraalJS context as the
 * global binding {@code __josmContextResetHooks__}.
 * </p>
 */
public class ContextResetHooks {
    private static final Logger logger =
        Logger.getLogger(ContextResetHooks.class.getName());

    private final List<Value> callbacks = new ArrayList<>();

    /**
     * Registers a JavaScript function to be called when the GraalJS context is reset.
     *
     * @param callback a GraalVM {@link Value} that {@link Value#canExecute() can be executed}
     * @throws NullPointerException     if callback is null
     * @throws IllegalArgumentException if callback is not an executable value
     */
    public void register(@NotNull final Value callback) {
        Objects.requireNonNull(callback, "callback must not be null");
        if (!callback.canExecute()) {
            throw new IllegalArgumentException(
                "callback must be an executable value (function)");
        }
        callbacks.add(callback);
    }

    /**
     * Invokes all registered callbacks in registration order, then clears the list.
     * Individual callback failures are logged but do not prevent remaining callbacks
     * from running.
     *
     * <p><b>Note:</b> the caller must have entered the associated GraalVM context
     * before calling this method.</p>
     */
    public void invokeAll() {
        final List<Value> toInvoke = new ArrayList<>(callbacks);
        callbacks.clear();
        for (final Value callback : toInvoke) {
            try {
                callback.execute();
            } catch (Exception e) {
                logger.log(Level.WARNING,
                    "Failed to invoke context reset callback", e);
            }
        }
    }
}
