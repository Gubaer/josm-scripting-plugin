package org.openstreetmap.josm.plugins.scripting.graalvm.with_graalvm

import org.graalvm.polyglot.Value
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest

import static org.junit.Assert.assertEquals

class GraalVMAndRequireTest extends AbstractGraalVMBasedTest{
    
    @Test
    void "should load a module with require()"() {
        def script = """
            const {message} = require('hello-world-message')
            console.log(`Message: \${message}`)
            message
        """
        facade.resetContext()
        Value value = facade.eval(graalJSDescriptor, script) as Value
        assertEquals("hello-world", value?.asString())
    }
}
