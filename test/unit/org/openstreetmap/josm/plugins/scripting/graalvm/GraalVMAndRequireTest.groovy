package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.openstreetmap.josm.plugins.PluginException
import org.openstreetmap.josm.plugins.PluginInformation
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture
import org.openstreetmap.josm.plugins.scripting.js.JOSMModuleScriptProvider
import org.openstreetmap.josm.tools.Logging

import java.util.logging.Level

import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

class GraalVMAndRequireTest {

    static JOSMFixture fixture

    private IGraalVMFacade facade

    @BeforeClass
    static void init() throws Exception {
        fixture = new JOSMFixture(true);
    }

    @Before
    void setup() throws PluginException, IOException {
        final projectDirEnv =
            System.getenv("JOSM_SCRIPTING_PLUGIN_HOME");
        final projectDir =
            new File(projectDirEnv ?: ".");

        JOSMModuleScriptProvider.getInstance().addRepository(
                new File(projectDir, "javascript/").toURI().toURL());
        JOSMModuleScriptProvider.getInstance().addRepository(
                new File(projectDir, "test/script-api/").toURI().toURL());
        new ScriptingPlugin(new PluginInformation(
                new File(projectDir, "dist/scripting.jar")));

        facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade();
        Logging.getLogger().setFilter({record ->
                record.getLevel().intValue() >= Level.WARNING.intValue()});
    }

    def getDescriptorForJavaScript() {
        assertTrue(GraalVMFacadeFactory.isGraalVMPresent());
        def descriptor =
                facade.getSupportedLanguages()
                .find {info -> info.getEngineId() == "js"}

        if (descriptor == null) {
            fail("no script engine descriptor for language 'js' found");
        }
        return descriptor;
    }

    //NOTE: doesn't work. require() isn't available in the Graal javascript
    // engine, not even, when the test is run with the GraalVM JDK
    @Test
    @Ignore
    void "should load a module with require()"() {
        def script = """
            const message = require('hello-world-message')
            console.log(`Message: \${message}`)
        """
        facade.eval(getDescriptorForJavaScript(), script)
    }
}
