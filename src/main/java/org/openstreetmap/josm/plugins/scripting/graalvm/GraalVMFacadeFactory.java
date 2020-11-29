package org.openstreetmap.josm.plugins.scripting.graalvm;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

public class GraalVMFacadeFactory {
    static private final  Logger logger =
        Logger.getLogger(GraalVMFacadeFactory.class.getName());

    /**
     * Replies true, if GraalVM is present and if the GraalVM polyglot API
     * can be used
     *
     * @return true, if GraalVM is present
     */
    static public boolean isGraalVMPresent() {
        try {
            Class.forName("org.graalvm.polyglot.Context");
            return true;
        } catch(ClassNotFoundException e) {
            //TODO(karl): change to FINEST later, just for debugging
            logger.log(Level.SEVERE, "class not found", e);
            return false;
        }
    }

    private static IGraalVMFacade instance = null;

    /**
     * Replies a facade to the GraalVM or null, if the GraalVM isn't on
     * the classpath or if it can't be initialized
     *
     * @return a facade to the GraalVM
     */
    static public IGraalVMFacade createGraalVMFacade() {
        if (!isGraalVMPresent()) {
            logger.warning(tr("GraalVM polyglot API isn''t on the class path. "
                    + "Support for GraalVM is disabled."));
            return null;
        }
        final IGraalVMFacade facade = new GraalVMFacade();
        return facade;

//        try {
//            Class<?> facadeClass = Class.forName(
//                "org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacade"
//            );
//            final IGraalVMFacade facade =
//                    (IGraalVMFacade) facadeClass.getDeclaredConstructor()
//                            .newInstance();
//            logger.info(tr("Enabled support for GraalVM"));
//            return facade;
//        } catch(
//             ClassNotFoundException | InstantiationException
//           | IllegalAccessException | NoClassDefFoundError
//           | NoSuchMethodException | InvocationTargetException e
//        ) {
//            System.out.println(e);
//            e.printStackTrace();
//            return null;
//        }
    }

    /**
     * Creates a GraalVM, if it doesn't exist yet and replies the current
     * GraalVM instance
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
