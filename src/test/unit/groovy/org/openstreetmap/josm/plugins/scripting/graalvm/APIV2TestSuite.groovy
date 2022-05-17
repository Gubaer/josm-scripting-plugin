package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.openstreetmap.josm.plugins.PluginException
import org.openstreetmap.josm.plugins.PluginInformation
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture
import org.openstreetmap.josm.tools.Logging

import java.util.logging.Level

class APIV2TestSuite {
    private static JOSMFixture fixture

    @BeforeClass
    static void init() throws Exception {
        fixture = new JOSMFixture(true)
    }

    @Before
    void setup() throws PluginException, IOException {
        def projectDirEnv = System.getenv("JOSM_SCRIPTING_PLUGIN_HOME")
        def projectDir = new File(projectDirEnv == null ? "." : projectDirEnv)
        def libsDir = new File(projectDir, "build/libs")
        def jarFile = libsDir.list().findAll {name ->
            name.startsWith("scripting") && name.endsWith(".jar")
        }.first()
        jarFile =  new File(libsDir, jarFile)
        if (jarFile == null) {
            throw new IllegalStateException("Didn't find scripting jar file in build/libs")
        }

        // Initialize the CommonJS module repositories
        def registry = CommonJSModuleRepositoryRegistry.getInstance()
        registry.setBuiltInRepository( new JarJSModuleRepository(jarFile, "/js/v2"))
        registry.addUserDefinedRepository(
            new FileSystemJSModuleRepository(
                new File(projectDirEnv, "src/test/unit/javascript/v2")
            )
        )

        //noinspection GroovyResultOfObjectAllocationIgnored
        new ScriptingPlugin(new PluginInformation(jarFile))

        if (!GraalVMFacadeFactory.isGraalVMPresent()) {
            throw new IllegalStateException("GraalJS must be present on the classpath")
        }
        Logging.getLogger().setFilter(
    record -> record.getLevel().intValue() >= Level.WARNING.intValue())
    }

    @Test
    void 'can run API V2 unit tests'() {
        def src = """
        const suite = require('suite')
        suite.fragileRun()
        """
        def facade= GraalVMFacadeFactory.getOrCreateGraalVMFacade()
        def graalJSDescriptor = facade.getScriptEngineDescriptors().find {desc ->
            desc.getLanguageName().filter{it == "JavaScript"}
        }
        if (graalJSDescriptor == null) {
            throw new IllegalStateException("Failed to lookup script engine descriptor for GraalJS")
        }
        facade.eval(graalJSDescriptor, src)
    }
}
