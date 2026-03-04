package org.openstreetmap.josm.plugins.scripting

import org.junit.jupiter.api.BeforeAll

import java.util.logging.Logger

class BaseTestCase {
    static final def logger = Logger.getLogger(BaseTestCase.class.name)

    protected static File scriptingJarFile

    static public final String ENV_JOSM_SCRIPTING_PLUGIN_HOME = "JOSM_SCRIPTING_PLUGIN_HOME"

    static File getProjectHome() {
        def projectDirEnv = System.getenv(ENV_JOSM_SCRIPTING_PLUGIN_HOME)
        def projectDir
        if (projectDirEnv == null) {
            projectDir = new File(".")
            logger.warning(
                    "environment variable '${ENV_JOSM_SCRIPTING_PLUGIN_HOME}' not set. " +
                            "Assuming project home '${projectDir.absolutePath}'"
            )
        } else {
            projectDir = new File(projectDirEnv)
        }
        logger.info("using project home '${projectDir.absolutePath}'")
        return projectDir
    }

    @BeforeAll
    static void lookupScriptingJarFile() {
        final libsDir = new File(getProjectHome(), "build/libs")
        final libs = new File(getProjectHome(), "build/libs")
            .listFiles( {_, name -> name.startsWith("scripting") && name.endsWith(".jar") } as FilenameFilter)
        if (libs == null || libs.length == 0) {
            throw new IllegalStateException("No scripting jar file in '${getProjectHome()}'.")
        } else if (libs.length > 1) {
            throw new IllegalStateException("More than one scripting jar file in '${getProjectHome()}'.")
        }
        scriptingJarFile = libs[0]
        if (!scriptingJarFile.isFile() || !scriptingJarFile.exists() || !scriptingJarFile.canRead()) {
            throw new IllegalStateException("Plugin jar file '$scriptingJarFile.absolutePath' not found.")
        }
    }
}
