import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.plugins.PluginException;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin;
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.js.JOSMModuleScriptProvider;
import org.openstreetmap.josm.plugins.scripting.js.RhinoEngine;
import org.openstreetmap.josm.plugins.scripting.ui.console.ScriptingConsole;
import org.openstreetmap.josm.testutils.JOSMTestRules;
import org.openstreetmap.josm.tools.Logging;

public class ScriptApiTest {

    //@Rule
    //public JOSMTestRules rules = new JOSMTestRules().preferences().platform().projection().main();

    private RhinoEngine engine;

    static JOSMFixture fixture;

    @BeforeClass
    public static void init() throws Exception {
        fixture = new JOSMFixture(true);
    }

    @Before
    public void setup() throws PluginException, MalformedURLException {
        final String projectDirEnv = System.getenv("JOSM_SCRIPTING_PLUGIN_HOME");
        final File projectDir = new File(projectDirEnv == null ? "." : projectDirEnv);

        JOSMModuleScriptProvider.getInstance().addRepository(new File(projectDir,  "javascript/").toURI().toURL());
        JOSMModuleScriptProvider.getInstance().addRepository(new File(projectDir,  "test/script-api/").toURI().toURL());
        new ScriptingPlugin(new PluginInformation(new File(projectDir, "dist/scripting.jar")));

        engine = RhinoEngine.getInstance();
        engine.enterSwingThreadContext();

        Logging.getLogger().setFilter(record -> record.getLevel().intValue() >= Level.WARNING.intValue());
    }

    @Test
    public void scriptApiTestSuite() {
        engine.evaluateOnSwingThread("require(\"suite\").fragileRun()");
    }

    @Test
    public void commandAddTest() {
        engine.evaluateOnSwingThread("require(\"functional/commandAddTest.js\")");
    }

    @Test
    public void commandChangeTest() {
        engine.evaluateOnSwingThread("require(\"functional/commandDeleteTest.js\")");
    }

    @Test
    public void commandDeleteTest() {
        engine.evaluateOnSwingThread("require(\"functional/commandDeleteTest.js\")");
    }

    @Test
    public void commandUndoRedoTest() {
        engine.evaluateOnSwingThread("require(\"functional/commandUndoRedo.js\")");
    }

    @Test
    public void menuBarTest() {
        engine.evaluateOnSwingThread("require(\"functional/menuBarTest.js\")");
    }

    @Test
    public void menuTest() {
        engine.evaluateOnSwingThread("require(\"functional/menuTest.js\")");
    }
}
