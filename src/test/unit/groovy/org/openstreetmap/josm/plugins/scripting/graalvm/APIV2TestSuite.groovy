package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.jupiter.api.Test


class APIV2TestSuite extends AbstractGraalVMBasedTest {

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
