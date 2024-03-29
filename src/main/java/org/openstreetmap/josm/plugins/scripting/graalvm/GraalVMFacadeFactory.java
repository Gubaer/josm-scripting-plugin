package org.openstreetmap.josm.plugins.scripting.graalvm;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GraalVMFacadeFactory {
    static private final Logger logger = Logger.getLogger(GraalVMFacadeFactory.class.getName());
    private static IGraalVMFacade instance = null;

    /**
     * Replies true, if the GraalVM is present and if the GraalVM polyglot API can be used
     *
     * @return true, if GraalVM is present
     */
    static public boolean isGraalVMPresent() {
        try {
            Class.forName("org.graalvm.polyglot.Context");
            return true;
        } catch (ClassNotFoundException e) {
            logger.log(Level.FINEST, "class not found", e);
            return false;
        }
    }

    /**
     * Replies a facade to the GraalVM or null, if the GraalVM isn't on
     * the classpath or if it can't be initialized
     *
     * @return a facade to the GraalVM
     */
    static public IGraalVMFacade createGraalVMFacade() {
        if (!isGraalVMPresent()) {
            logger.warning("GraalVM polyglot API isn't on the class path. Support for GraalVM is disabled.");
            return null;
        }
        return new GraalVMFacade();
    }

    /**
     * Creates a GraalVM facade if it doesn't exist yet and replies the current
     * GraalVM facade
     *
     * @return the facade
     */
    static public IGraalVMFacade getOrCreateGraalVMFacade() {
        if (instance == null) {
            instance = createGraalVMFacade();
        }
        return instance;
    }
}
