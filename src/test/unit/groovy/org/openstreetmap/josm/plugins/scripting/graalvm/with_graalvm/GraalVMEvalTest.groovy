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

    @Test
    void "should eval a script file multiple times without context reset"() {
        def script = """
            let count = (globalThis.__count__ ?? 0) + 1
            globalThis.__count__ = count
            count
        """
        def f = File.createTempFile("js-script", ".mjs")
        f.deleteOnExit()
        f.write(script)

        def result1 = facade.eval(graalJSDescriptor, f)
        def result2 = facade.eval(graalJSDescriptor, f)
        def result3 = facade.eval(graalJSDescriptor, f)

        assert result1.asInt() == 1
        assert result2.asInt() == 2
        assert result3.asInt() == 3
    }
}
