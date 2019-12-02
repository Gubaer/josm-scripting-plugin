package org.openstreetmap.josm.plugins.scripting.graalvm

import com.oracle.truffle.api.interop.UnsupportedTypeException
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
        scriptProvider.addRepository(new File(projectDir,
                "${projectDirEnv}/src/main/javascript/v1").toURI().toURL());
        scriptProvider.addRepository(new File(projectDir,  "test/script-api/")
                .toURI().toURL());

        def repos = ModuleRepositories.instance
        repos.addUserDefinedRepository(new FileSystemJSModuleRepository(
            "${projectDirEnv}/src/main/javascript/v2"
        ))
        repos.addUserDefinedRepository(new FileSystemJSModuleRepository(
            "${projectDirEnv}/src/test/unit/javascript/v2"
        ))
        repos.addUserDefinedRepository(new FileSystemJSModuleRepository(
            "${projectDirEnv}/src/test/functional/javascript/v2"
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

    def runTestScript(String script) {
        def facade = GraalVMFacadeFactory.createGraalVMFacade()
        def desc = facade.getSupportedLanguages()
                .find { desc -> desc.engineId == "js" }
        try {
            facade.eval(desc, script)
        } catch(GraalVMEvalException e) {
            println("caught ${e} ...")
            e.printStackTrace()
            final e2 = (UnsupportedTypeException) e.getCause().getCause()
            if (e2 != null) {
                for (Object o : e2.getSuppliedValues()) {
                    println(o)
                }
            }
            throw e
        } catch(e) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    void "can run 'command-add-test'"() {
        def script = """
            const test = require('command-add-test')
            test.run()
        """
        runTestScript(script)
    }

    @Test
    void "can run 'command-delete-test'"() {
        def script = """
            const test = require('command-delete-test')
            test.run()
        """
        runTestScript(script)
    }

    @Test
    void "can run 'command-change-test'"() {
        def script = """
            const test = require('command-change-test')
            test.run()
        """
        runTestScript(script)
    }

    @Test
    void "can run 'command-undo-redo-test'"() {
        def script = """
            const test = require('command-undo-redo-test')
            test.run()
        """
        runTestScript(script)
    }

    @Test
    void "can run 'menu-bar-test'"() {
        def script = """
            const test = require('menu-bar-test')
            test.run()
        """
        runTestScript(script)
    }

    @Test
    void "can run 'menu-test'"() {
        def script = """
            const test = require('menu-test')
            test.run()
        """
        runTestScript(script)
    }

    @Test
    void "can run 'layer-test'"() {
        def script = """
            const test = require('layer-test')
            test.run()
        """
        runTestScript(script)
    }

    @Test
    void "can run 'ds-test'"() {
        def script = """
            const test = require('ds-test')
            test.fragileRun()
        """
        runTestScript(script)
    }

    @Test
    @Ignore
    void "test throwing error"() {
        def script = """
            const util = require('josm/util')
            //util.assert(false, 'a reason for the error')
            //throw new Error('a reason for the error')
            const error = new Error()
            error.message  = 'a reason for the error'
            throw error
        """
        def facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
        def desc = facade.getSupportedLanguages()
                .find { desc -> desc.engineId == "js" }
        try {
            facade.eval(desc, script)
        } catch(e) {
            e.printStackTrace()
            if (e.getCause() != null) {
                println(e.getCause())
                println(e.getCause().getMessage())
                e.getCause().printStackTrace()
            }
            fail()
        }
    }
}
