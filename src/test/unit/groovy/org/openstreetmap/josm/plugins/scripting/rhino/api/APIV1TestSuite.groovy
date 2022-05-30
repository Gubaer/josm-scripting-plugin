package org.openstreetmap.josm.plugins.scripting.rhino.api


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.PluginException
import org.openstreetmap.josm.plugins.PluginHandler
import org.openstreetmap.josm.plugins.PluginInformation
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin
import org.openstreetmap.josm.plugins.scripting.js.JOSMModuleScriptProvider
import org.openstreetmap.josm.plugins.scripting.js.RhinoEngine
import org.openstreetmap.josm.tools.Logging

import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.util.logging.Level
import java.util.logging.Logger

class APIV1TestSuite extends JOSMFixtureBasedTest {
    static final Logger logger = Logger.getLogger(APIV1TestSuite.class.name)

    private RhinoEngine engine

    @BeforeEach
    void setup() throws PluginException, IOException {
        final File projectDir = getProjectHome()

        // JavaScript API V1 - source repository
        JOSMModuleScriptProvider.getInstance().addRepository(
            new File(projectDir, "src/main/javascript/v1").toURI().toURL())

        // JavaScript API V1 unit tests - source repository
        JOSMModuleScriptProvider.getInstance().addRepository(
            new File(projectDir, "src/test/unit/javascript/v1").toURI().toURL())

        // JavaScript API V1 functional tests- source repository
        JOSMModuleScriptProvider.getInstance().addRepository(
            new File(projectDir, "src/test/functional/javascript/v1").toURI().toURL())

        //noinspection GroovyResultOfObjectAllocationIgnored
        new ScriptingPlugin(new PluginInformation(scriptingJarFile), true /* in test environment */)

        engine = RhinoEngine.getInstance()
        engine.enterSwingThreadContext()

        Logging.getLogger().setFilter(
    record -> record.getLevel().intValue() >= Level.WARNING.intValue())
    }

    private static File localContourmergePluginJar() {
        return new File(
            new File(fixture.getJosmHome()),
            "plugins/contourmerge.jar")
    }

    static void downloadContourmergePluginForTesting() throws IOException {
        logger.fine("Downloading contourmerge plugin for testing ...")
        final URL downloadUrl = new URL(
            "https://github.com/Gubaer/josm-contourmerge-plugin/"
           + "releases/download/pickup-release/contourmerge.jar")
        final ReadableByteChannel rbc = Channels.newChannel(
                downloadUrl.openStream())
        try(final FileOutputStream os = new FileOutputStream(localContourmergePluginJar())) {
            os.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE)
        }
    }

    @Test
    void scriptApiTestSuite() {
        def source = """
            require('suite').fragileRun();
        """
        engine.evaluateOnSwingThread(source)
    }

    static def loadFunctionalTest(String fileName) {
        def sourceDir = new File(getProjectHome(), "src/test/functional/javascript/v1")
        def testSource = new File(sourceDir, fileName)
        if (! testSource.exists() || ! testSource.canRead()) {
            def message = "Can't read function javascript test '$testSource.absolutePath'"
            logger.severe(message)
            fail(message)
        }
        return testSource.text
    }

    @Test
    void commandAddTest() {
        final source = loadFunctionalTest("commandAddTest.js")
        engine.evaluateOnSwingThread(source)
    }

    @Test
    void commandChangeTest() {
        final source = loadFunctionalTest("commandDeleteTest.js")
        engine.evaluateOnSwingThread(source)
    }

    @Test
    void commandDeleteTest() {
        final source = loadFunctionalTest("commandChangeTest.js")
        engine.evaluateOnSwingThread(source)
    }

    @Test
    void commandUndoRedoTest() {
        final source = loadFunctionalTest("commandUndoRedo.js")
        engine.evaluateOnSwingThread(source)
    }

    @Test
    void menuBarTest() {
        final source = loadFunctionalTest("menuBarTest.js")
        engine.evaluateOnSwingThread(source)
    }

    @Test
    void menuTest() {
        final source = loadFunctionalTest("menuTest.js")
        engine.evaluateOnSwingThread(source)
    }

    @Test
    void loadClassFrom3dPartyPluginTest()
            throws IOException, PluginException {
        if (!localContourmergePluginJar().exists()) {
            downloadContourmergePluginForTesting()
        }

        final PluginInformation info = new PluginInformation(
                new File(new File(fixture.getJosmHome()),
                        "plugins/contourmerge.jar")
        )
        PluginHandler.loadPlugins(
            null,
            Collections.singleton(info),
            null)

        final source = loadFunctionalTest("loadClassFrom3dPartyPluginTest.js")
        engine.evaluateOnSwingThread(source)
    }
}
