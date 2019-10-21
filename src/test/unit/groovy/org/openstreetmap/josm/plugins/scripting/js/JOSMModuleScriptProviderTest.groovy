package org.openstreetmap.josm.plugins.scripting.js

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.fail

class JOSMModuleScriptProviderTest {

    @BeforeClass
    static void init() {
        String home = System.getenv("JOSM_SCRIPTING_PLUGIN_HOME")
        if (home == null) {
            fail("Environment variable JOSM_SCRIPTING_PLUGIN_HOME not set. " +
                    "Check env.sh")
        }

        JOSMModuleScriptProvider.getInstance().addRepository(
            new File(new File(home), "test/data/require/modules")
                    .toURI().toURL()
        )

        def jarfile = new File(
                new File(home),
                "test/data/require/jarmodules.jar"
            ).toURI().toURL().toString()
        JOSMModuleScriptProvider.getInstance().addRepository(
            new URL("jar:" + jarfile + "!/modules")
        )
    }

    def provider

    @Before
    void setUp() {
        provider = JOSMModuleScriptProvider.getInstance()
    }

    @Test
    void lookupExistingModule() {
        def url = provider.lookup("module1")
        assert url != null

        // various module names which are normalized

        url = provider.lookup("   module1")
        assert url != null

        url = provider.lookup("module1  ")
        assert url != null

        url = provider.lookup("//module1//")
        assert url != null

        url = provider.lookup("\\module1//")
        assert url != null
    }

    @Test
    void lookupExistingSubModel() {
        assert provider.lookup("sub/module3")
        assert provider.lookup("sub\\module3")
        assert provider.lookup(" sub/module3")
        assert provider.lookup(" \\\\sub/module3//   ")
        assert provider.lookup("sub/module4")
    }

    @Test
    void lookupExistingModuleInJar() {
        assert provider.lookup("module10")
        assert provider.lookup(" module10  ")
        assert provider.lookup("//module10  ")
        assert provider.lookup("\\\\module10  ")

        assert provider.lookup("module11")
    }

    @Test
    void lookupExistingSubModuleInJar() {
        assert provider.lookup("sub/module12")
        assert provider.lookup("sub\\module12")
        assert provider.lookup(" sub/module12")
        assert provider.lookup(" \\\\sub/module12//   ")
        assert provider.lookup("sub/module13")
    }

    @Test
    void lookupAModuleWithIdenticalDirectoryName() {
        // the jar contains a directory 'josm' and a module 'josm.js'.
        // The module should be found despite the directory with
        // the same name.
        assert provider.lookup("josm")
    }
}
