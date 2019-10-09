package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.openstreetmap.josm.plugins.PluginException
import org.openstreetmap.josm.plugins.PluginInformation
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture
import org.openstreetmap.josm.plugins.scripting.js.JOSMModuleScriptProvider
import org.openstreetmap.josm.tools.Logging
import static org.junit.Assert.fail

import java.util.logging.Level

class ScriptingAPIV2Test {
    static JOSMFixture fixture;

    @BeforeClass
    static void init() throws Exception {
        fixture = new JOSMFixture(true);
    }

    @Before
    void setup() throws PluginException, MalformedURLException, IOException {
        def projectDirEnv = System.getenv("JOSM_SCRIPTING_PLUGIN_HOME");
        def projectDir = new File(projectDirEnv == null ? "." : projectDirEnv);

        def scriptProvider = JOSMModuleScriptProvider.getInstance()
        scriptProvider.addRepository(new File(projectDir,  "javascript/")
                .toURI().toURL());
        scriptProvider.addRepository(new File(projectDir,  "test/script-api/")
                .toURI().toURL());

        def repos = ModuleRepositories.instance
        repos.add(new FileSystemJSModuleRepository(
            "${projectDirEnv}/src/main/javascript/v2"
        ))
        repos.add(new FileSystemJSModuleRepository(
            "${projectDirEnv}/src/test/unit/javascript/v2"
        ))

        new ScriptingPlugin(new PluginInformation(
            new File(projectDir, "dist/scripting.jar")));

        Logging.getLogger().setFilter({record ->
            record.getLevel().intValue() >= Level.WARNING.intValue()
        });
    }

    @Test
    void "can run script API test suite"() {
        def script = """
            const suite = require('suite')
            suite.fragileRun()
        """
        def facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
        def desc = facade.getSupportedLanguages()
                .find { desc -> desc.engineId == "js" }
        try {
            facade.eval(desc, script)
        } catch(e) {
            e.printStackTrace()
            fail()
        }
    }

}
