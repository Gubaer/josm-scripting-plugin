package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.Test

class GraalVMEvalTest {

    @Test
    void "should eval a hello world script"() {
        def facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
        def desc = facade.getScriptEngineDescriptors()
            .find {desc -> desc.engineId == "js"}
        assert desc != null
        def script = """
                    console.log('Hello world!');
                """
        facade.eval(desc, script)
    }

    @Test
    void "should eval javascript script file()"() {
        def facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
        def desc = facade.getScriptEngineDescriptors()
            .find {desc -> desc.engineId == "js"}
        assert desc != null
        def script = """
                    console.log('Hello world!');
                """
        File.createTempFile("js-script", ".tmp").with {
            deleteOnExit()
            write script
            def f = new File(absolutePath)
            facade.eval(desc, f)
        }
    }
}
