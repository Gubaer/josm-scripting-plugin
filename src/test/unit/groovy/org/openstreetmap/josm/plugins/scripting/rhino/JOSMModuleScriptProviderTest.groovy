package org.openstreetmap.josm.plugins.scripting.rhino

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest

class JOSMModuleScriptProviderTest extends JOSMFixtureBasedTest {

    /**
     * the full path to the directory with the CommonJS test resources
     */
    static public final String ENV_TEST_COMMONJS_MODULE_REPO = "TEST_COMMONJS_MODULE_REPO"

    static File getCommonJSTestResourcesDirectory() {
        def dirEnv = System.getenv(ENV_TEST_COMMONJS_MODULE_REPO)
        def dir
        if (dirEnv == null) {
            dir = new File(getProjectHome(), "/src/test/resources/require")
            logger.warning(
                "environment variable '${ENV_TEST_COMMONJS_MODULE_REPO}' not set. " +
                "Assuming project home '${dir.absolutePath}'"
            )
        } else {
            dir = new File(dirEnv)
        }
        logger.info("using directory with CommonJS test sources: '${dir.absolutePath}'")
        return dir
    }

    @BeforeAll
    static void init() {
        final testResourcesDir = getCommonJSTestResourcesDirectory()
        JOSMModuleScriptProvider.getInstance().addRepository(
            new File(testResourcesDir, "modules").toURI().toURL()
        )
        final jarfile = new File(testResourcesDir, "jarmodules.jar")
            .toURI().toURL().toString()

        JOSMModuleScriptProvider.getInstance().addRepository(
            new URL("jar:$jarfile!/modules")
        )
    }

    private JOSMModuleScriptProvider provider

    @BeforeEach
    void setUp() {
        provider = JOSMModuleScriptProvider.getInstance()
    }

    @Test
    void lookupExistingModule() {
        def url = provider.lookup("module1")
        assertNotNull(url)

        // various module names which are normalized

        url = provider.lookup("   module1")
        assertNotNull(url)

        url = provider.lookup("module1  ")
        assertNotNull(url)

        url = provider.lookup("//module1//")
        assertNotNull(url)

        url = provider.lookup("\\module1//")
        assertNotNull(url)
    }

    @Test
    void lookupExistingSubModel() {
        assertTrue(provider.lookup("sub/module3").isPresent())
        assertTrue(provider.lookup("sub\\module3").isPresent())
        assertTrue(provider.lookup(" sub/module3").isPresent())
        assertTrue(provider.lookup(" \\\\sub/module3//   ").isPresent())
        assertTrue(provider.lookup("sub/module4").isPresent())
    }

    @Test
    void lookupExistingModuleInJar() {
        assertTrue(provider.lookup("module10").isPresent())
        assertTrue(provider.lookup(" module10  ").isPresent())
        assertTrue(provider.lookup("//module10  ").isPresent())
        assertTrue(provider.lookup("\\\\module10  ").isPresent())
        assertTrue(provider.lookup("module11").isPresent())
    }

    @Test
    void lookupExistingSubModuleInJar() {
        assertTrue(provider.lookup("sub/module12").isPresent())
        assertTrue(provider.lookup("sub\\module12").isPresent())
        assertTrue(provider.lookup(" sub/module12").isPresent())
        assertTrue(provider.lookup(" \\\\sub/module12//   ").isPresent())
        assertTrue(provider.lookup("sub/module13").isPresent())
    }

    @Test
    void lookupAModuleWithIdenticalDirectoryName() {
        // the jar contains a directory 'josm' and a module 'josm.js'.
        // The module should be found despite the directory with
        // the same name.
        assertTrue(provider.lookup("josm").isPresent())
    }
}
