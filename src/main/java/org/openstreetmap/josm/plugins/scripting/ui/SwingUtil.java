package org.openstreetmap.josm.plugins.scripting.ui;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.text.MessageFormat.format;

public class SwingUtil {
    static private final Logger logger = Logger.getLogger(SwingUtil.class.getName());

    /**
     * Executes a runnable on the Swing EDT
     *
     * @param r the runnable
     * @throws NullPointerException if <code>r</code> is null
     * @throws RuntimeException if <code>r</code> wraps and exception (wrapped in a RuntimeException)
     */
    static public void runOnSwingEDT(@NotNull final Runnable r){
        Objects.requireNonNull(r);
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch(InvocationTargetException e){
                Throwable throwable = e.getCause();
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                } else if(throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                // no other checked exceptions expected - log a warning
                logger.log(Level.WARNING, format(
                    "Unexpected exception wrapped in InvocationTargetException: {0}",
                    throwable.toString()
                ), throwable);
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
    }
}
