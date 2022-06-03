package org.openstreetmap.josm.plugins.scripting.graalvm.api

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory
import org.openstreetmap.josm.plugins.scripting.graalvm.IGraalVMFacade

class APIV2TestSuite extends AbstractGraalVMBasedTest {

    private IGraalVMFacade facade

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
