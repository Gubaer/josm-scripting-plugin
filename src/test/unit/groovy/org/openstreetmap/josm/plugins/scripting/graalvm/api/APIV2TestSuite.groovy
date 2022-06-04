package org.openstreetmap.josm.plugins.scripting.graalvm.api


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.CommonJSModuleRepositoryRegistry
import org.openstreetmap.josm.plugins.scripting.graalvm.FileSystemJSModuleRepository

class APIV2TestSuite extends AbstractGraalVMBasedTest {

    @BeforeEach
    void addRepoForTestSuite() {
        final testSuiteRepo = new File(getProjectHome(), "src/test/unit/javascript/v2")
        if (!testSuiteRepo.isDirectory() || !testSuiteRepo.canRead()) {
            fail("Directory '$testSuiteRepo.absolutePath' with API V2 unit tests doesn't exist or isn't readable")
        }
        CommonJSModuleRepositoryRegistry.instance.addUserDefinedRepository(
            new FileSystemJSModuleRepository(testSuiteRepo)
        )
    }

    @Test
    void 'can run API V2 unit tests'() {
        def src = """
        const suite = require('suite')
        suite.fragileRun()
        """
        facade.eval(graalJSDescriptor, src)
    }

    @Test
    void 'can require all modules'() {
        final src = """
        require('josm')
        require('clipboard')
        require('josm/api')
        require('josm/builder')
        require('josm/command')
        require('josm/ds')
        require('josm/layers')
        require('josm/scriptingconsole')
        require('josm/unittest')
        require('josm/util')
        require('josm/ui/menu')
        """
        facade.eval(graalJSDescriptor, src)
    }
}
