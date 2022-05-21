package org.openstreetmap.josm.plugins.scripting.v1;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.plugins.PluginException;
import org.openstreetmap.josm.plugins.PluginHandler;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin;
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.js.JOSMModuleScriptProvider;
import org.openstreetmap.josm.plugins.scripting.js.RhinoEngine;
import org.openstreetmap.josm.tools.Logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.logging.Level;

public class ScriptApiTest {

    private RhinoEngine engine;
    private static JOSMFixture fixture;

    @BeforeAll
    public static void init() throws Exception {
        fixture = new JOSMFixture(true);
    }

    @BeforeEach
    public void setup() throws PluginException, IOException {
        final String projectDirEnv =
            System.getenv("JOSM_SCRIPTING_PLUGIN_HOME");
        final File projectDir =
            new File(projectDirEnv == null ? "." : projectDirEnv);

        // JavaScript API V1 - source repository
        JOSMModuleScriptProvider.getInstance().addRepository(
            new File(projectDir,  "src/main/javascript/v1").toURI().toURL());

        // JavaScript API V1 unit tests- source repository
        JOSMModuleScriptProvider.getInstance().addRepository(
            new File(projectDir,  "src/test/unit/javascript/v1").toURI().toURL());

        new ScriptingPlugin(new PluginInformation
            (new File(projectDir, "dist/scripting.jar")));

        engine = RhinoEngine.getInstance();
        engine.enterSwingThreadContext();

        Logging.getLogger().setFilter(
            record -> record.getLevel().intValue() >= Level.WARNING.intValue());
    }

    private File localContourmergePluginJar() {
        return new File(new File(fixture.getJosmHome()),
            "plugins/contourmerge.jar");
    }

    private void downlaodContourmergePluginForTesting() throws IOException {
        System.out.println("Downloading contourmerge plugin for testing ...");
        final URL downloadUrl = new URL(
                "https://github.com/Gubaer/josm-contourmerge-plugin/"
              + "releases/download/pickup-release/contourmerge.jar");
        final ReadableByteChannel rbc = Channels.newChannel(
                downloadUrl.openStream());
        try(final FileOutputStream os = new FileOutputStream(localContourmergePluginJar())) {
            os.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    @Test
    public void scriptApiTestSuite() {
        engine.evaluateOnSwingThread("require(\"suite\").fragileRun()");
    }

    @Test
    public void commandAddTest() {
        engine.evaluateOnSwingThread(
            "require(\"functional/commandAddTest.js\")");
    }

    @Test
    public void commandChangeTest() {
        engine.evaluateOnSwingThread(
            "require(\"functional/commandDeleteTest.js\")");
    }

    @Test
    public void commandDeleteTest() {
        engine.evaluateOnSwingThread(
            "require(\"functional/commandDeleteTest.js\")");
    }

    @Test
    public void commandUndoRedoTest() {
        engine.evaluateOnSwingThread(
            "require(\"functional/commandUndoRedo.js\")");
    }

    @Test
    public void menuBarTest() {
        engine.evaluateOnSwingThread(
            "require(\"functional/menuBarTest.js\")");
    }

    @Test
    public void menuTest() {
        engine.evaluateOnSwingThread(
            "require(\"functional/menuTest.js\")");
    }

    @Test
    public void loadClassFrom3dPartyPluginTest()
            throws IOException, PluginException {
        if (!localContourmergePluginJar().exists()) {
            downlaodContourmergePluginForTesting();
        }

        final PluginInformation info = new PluginInformation(
            new File(new File(fixture.getJosmHome()),
                    "plugins/contourmerge.jar")
        );
        PluginHandler.loadPlugins(null,
            Collections.singleton(info),
            null);

        engine.evaluateOnSwingThread(
            "require(\"functional/loadClassFrom3dPartyPluginTest.js\")");
    }
}
