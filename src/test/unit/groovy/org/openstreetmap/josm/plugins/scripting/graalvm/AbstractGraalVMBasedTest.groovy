package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.openstreetmap.josm.plugins.PluginException
import org.openstreetmap.josm.plugins.PluginInformation
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.CommonJSModuleRepositoryRegistry
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.FileSystemJSModuleRepository
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.ICommonJSModuleRepository
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.JarJSModuleRepository
import org.openstreetmap.josm.plugins.scripting.model.RelativePath
import org.openstreetmap.josm.tools.Logging

import java.util.logging.Level

import static org.junit.Assert.fail

class AbstractGraalVMBasedTest extends JOSMFixtureBasedTest {

    static String escapeWindowsPathDelimiter(String path) {
        return path.replace("\\", "\\\\")
    }

    static String escapeWindowsPathDelimiter(File path) {
        escapeWindowsPathDelimiter(path.toString())
    }

    static def graalJSDescriptor = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
        .getScriptEngineDescriptors().find {desc ->
            desc.getLanguageName().filter{it == "JavaScript"}
        }

    static ICommonJSModuleRepository moduleRepo

    @BeforeAll
    static void readEnvironmentVariables() {
        final moduleRepoPath = System.getenv("TEST_COMMONJS_MODULE_REPO")
        if (moduleRepoPath == null) {
            fail("environment variable TEST_COMMONJS_MODULE_REPO not set")
        }
        final dir = new File(moduleRepoPath)
        if (!dir.isDirectory() || !dir.canRead()) {
            fail("directory '$dir.absolutePath' with CommonJS modules doesn't exist or isn't readable")
        }
        moduleRepo = new FileSystemJSModuleRepository(moduleRepoPath)
    }

    @BeforeEach
    void resetRepositoryRegistry() {
        // Initialize the CommonJS module repositories
        def registry = CommonJSModuleRepositoryRegistry.getInstance()
        registry.setBuiltInRepository( new JarJSModuleRepository(scriptingJarFile, RelativePath.parse("js/v2")))
        registry.addUserDefinedRepository(
                new FileSystemJSModuleRepository(
                        new File(getProjectHome(), "src/test/unit/javascript/v2")
                )
        )
        CommonJSModuleRepositoryRegistry.instance.clear()
        CommonJSModuleRepositoryRegistry.instance.addUserDefinedRepository(moduleRepo)
    }

    @BeforeEach
    void setup() throws PluginException, IOException {
        //noinspection GroovyResultOfObjectAllocationIgnored
        new ScriptingPlugin(new PluginInformation(scriptingJarFile), true /* in test environment */)

        if (!GraalVMFacadeFactory.isGraalVMPresent()) {
            throw new IllegalStateException("GraalJS must be present on the classpath")
        }
        Logging.getLogger().setFilter(
        record -> record.getLevel().intValue() >= Level.WARNING.intValue())
    }

    protected IGraalVMFacade facade

    @BeforeEach
    void initGraalVMFacade() {
        facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
    }

    @AfterEach
    void resetGraalVMFacade() {
        if (facade != null) {
            facade.resetContext()
        }
    }
}
