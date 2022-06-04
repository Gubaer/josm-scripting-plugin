package org.openstreetmap.josm.plugins.scripting.graalvm.with_graalvm

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest

class GraalVMEvalTest extends AbstractGraalVMBasedTest {

    @Test
    void "should eval a hello world script"() {
        def script = """
                    console.log('Hello world!')
                """
        facade.eval(graalJSDescriptor, script)
    }

    @Test
    void "should eval javascript script file()"() {
        def script = """
                    console.log('Hello world!')
                """
        File.createTempFile("js-script", ".tmp").with {
            deleteOnExit()
            write script
            def f = new File(absolutePath)
            facade.eval(graalJSDescriptor, f)
        }
    }
}
