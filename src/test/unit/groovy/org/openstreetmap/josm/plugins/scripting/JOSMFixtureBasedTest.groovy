package org.openstreetmap.josm.plugins.scripting

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.BeforeAll
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture

import java.util.logging.Logger

class JOSMFixtureBasedTest extends GroovyTestCase {
    static final def logger = Logger.getLogger(JOSMFixtureBasedTest.class.name)
    protected static JOSMFixture fixture
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
        scriptingJarFile = new File(getProjectHome(), "build/dist/scripting.jar")
        if (!scriptingJarFile.isFile() || !scriptingJarFile.exists() || !scriptingJarFile.canRead()) {
            throw new IllegalStateException(
                "Plugin jar file '$scriptingJarFile.absolutePath' not found.")
        }
    }

    @BeforeAll
    static void initJOSMFixture() throws Exception {
        fixture = JOSMFixture.createFixture(true /* with gui */)
    }
}
