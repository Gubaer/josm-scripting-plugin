package org.openstreetmap.josm.plugins.scripting

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.BeforeAll
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture

import java.util.logging.Logger
import java.util.stream.Collectors

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
        def libsDir = new File(getProjectHome(), "build/libs")
        if (! libsDir.exists() || !libsDir.isDirectory()) {
            def message = "no libs directory '$libsDir.absolutePath' found. " +
                "Run ./gradlew build -x first."
            logger.severe(message)
            throw new IllegalStateException(message)
        }
        final String[] emptyList = []
        def jarFiles = Arrays.stream(libsDir.list() ?: emptyList).filter {
                it.startsWith("scripting") && it.endsWith(".jar")
            }
            .map {new File(libsDir, it)}
            .collect(Collectors.toList())
        switch(jarFiles.size()) {
            case 0:
                def message = "no scripting*.jar file found in lib dir '${libsDir.absolutePath}'. " +
                        "Run ./gradlew build first."
                logger.severe(message)
                throw new IllegalStateException(message)
            case 1:
                scriptingJarFile = jarFiles.get(0)
                break
            default:
                def message = "more than one scripting*.jar file found in lib dir '${libsDir.absolutePath}'. " +
                        "Run ./gradlew clean build first."
                logger.severe(message)
                throw new IllegalStateException(message)
        }
    }

    @BeforeAll
    static void initJOSMFixture() throws Exception {
        fixture = JOSMFixture.createFixture(true /* with gui */);
    }
}
