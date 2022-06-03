package org.openstreetmap.josm.plugins.scripting.graalvm


import org.junit.jupiter.api.BeforeEach
import org.openstreetmap.josm.plugins.PluginException
import org.openstreetmap.josm.plugins.PluginInformation
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin
import org.openstreetmap.josm.tools.Logging

import java.util.logging.Level

class AbstractGraalVMBasedTest extends JOSMFixtureBasedTest {

    static def graalJSDescriptor = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
        .getScriptEngineDescriptors().find {desc ->
            desc.getLanguageName().filter{it == "JavaScript"}
        }

    @BeforeEach
    void setup() throws PluginException, IOException {
        def projectDir = getProjectHome()

        // Initialize the CommonJS module repositories
        def registry = CommonJSModuleRepositoryRegistry.getInstance()
        registry.setBuiltInRepository( new JarJSModuleRepository(scriptingJarFile, "/js/v2"))
        registry.addUserDefinedRepository(
            new FileSystemJSModuleRepository(
                new File(projectDir, "src/test/unit/javascript/v2")
            )
        )
        //noinspection GroovyResultOfObjectAllocationIgnored
        new ScriptingPlugin(new PluginInformation(scriptingJarFile), true /* in test environment */)

        if (!GraalVMFacadeFactory.isGraalVMPresent()) {
            throw new IllegalStateException("GraalJS must be present on the classpath")
        }
        Logging.getLogger().setFilter(
        record -> record.getLevel().intValue() >= Level.WARNING.intValue())
    }
}
