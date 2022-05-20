package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.Ignore
import org.junit.Test

class GraalVMAndRequireTest extends AbstractGraalVMBasedTest{

    //TODO(gubaer): fix this
    @Test
    @Ignore
    void "should load a module with require()"() {
        def script = """
            const {message} = require('hello-world-message')
            console.log(`Message: \${message}`)
        """
        GraalVMFacadeFactory.createGraalVMFacade()
            .eval(graalJSDescriptor, script)
    }
}
