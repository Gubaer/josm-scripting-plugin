package org.openstreetmap.josm.plugins.scripting.graalvm.with_graalvm

import org.graalvm.polyglot.Value
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.*

class GraalVMAndRequireTest extends AbstractGraalVMBasedTest{

    //TODO(gubaer): fix later
    @Test
    void "should load a module with require()"() {
        def script = """
            const {message} = require('hello-world-message')
            console.log(`Message: \${message}`)
            message
        """
        Value value = facade.eval(graalJSDescriptor, script) as Value
        assertEquals("hello-world", value?.asString())
    }
}
