package org.openstreetmap.josm.plugins.scripting.graalvm

import groovy.test.GroovyTestCase
import org.graalvm.polyglot.Value
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider

import java.util.stream.Collectors

class GraalVMPresentTest extends GroovyTestCase {

    @Test
    void shouldDetectGraalVMPresent() {
        final boolean isPresent = GraalVMFacadeFactory.isGraalVMPresent()
        assertTrue(isPresent)
    }

    @Test
    void shouldCreateAGraalVMFacade(){
        final IGraalVMFacade facade =
            GraalVMFacadeFactory.getOrCreateGraalVMFacade()
        assertNotNull(facade)
    }

    @Test
    void shouldDetectANonEmptyListOfLanguages() {
        def facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
        def infos = facade.getScriptEngineDescriptors()
        assertFalse(infos.isEmpty())
    }

    @Test
    void shouldProvideANonEmptyStreamOfGraalVMScriptEngines() {
        def engines =
            ScriptEngineMetaDataProvider.getAvailableGraalVMScriptEngines()
                .collect(Collectors.toList())
        assertFalse(engines.isEmpty())

        def allEnginesAreGraalVMEngines =
            engines.stream().allMatch() { engine ->
                engine.getEngineType() == ScriptEngineDescriptor.ScriptEngineType.GRAALVM
            }
        assertTrue(allEnginesAreGraalVMEngines)
    }

    static def getJavaScriptScriptEngineDescriptor() {
        return GraalVMFacadeFactory.getOrCreateGraalVMFacade()
            .getScriptEngineDescriptors()
        .find {desc ->
            desc.engineId == "js"
        }
    }

    @Test
    void "should evaluate a simple arithmetic expression"() {
        def script = "1 + 1"
        def js = getJavaScriptScriptEngineDescriptor()
        def facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()

        def result = facade.eval(js, script) as Value
        assertEquals(2, result?.asInt())
    }

    @Test
    void "should instantiate a class in the java namespace"() {
        def script = """
        const String = Java.type('java.lang.String')
        const value = new String('hello')
        value
        """
        def js = getJavaScriptScriptEngineDescriptor()
        def facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()

        def result = facade.eval(js, script) as Value
        assertEquals("hello", result?.asString())
    }

    @Test
    void "should access a class in the openstreetmap namespace"() {
        def script = """
        const ScriptingPlugin = Java.type(
            'org.openstreetmap.josm.plugins.scripting.ScriptingPlugin')    
        ScriptingPlugin
        """
        def js = getJavaScriptScriptEngineDescriptor()
        def facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()

        def result = facade.eval(js, script) as Value
        assertEquals(
            "class org.openstreetmap.josm.plugins.scripting.ScriptingPlugin",
            result?.asHostObject()?.toString())
    }

    @Test
    void "must not access a class in a foreign namespace"() {
        def script = """
        const DefaultMustacheFactory = Java.type(
            'com.github.spullara.mustache.java.DefaultMustacheFactory')
        DefaultMustacheFactory
        """
        def js = getJavaScriptScriptEngineDescriptor()
        def facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()

        shouldFail(GraalVMEvalException.class) {
            facade.eval(js, script) as Value
        }
    }

    @Test
    void "can reset the scripting context"() {
        def script = """const a = 1 + 1; a;"""
        def js = getJavaScriptScriptEngineDescriptor()
        def facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
        def value = facade.eval(js, script) as Value
        assertEquals(2, value?.asInt())
        facade.resetContext()
        script = "a"
        shouldFail(GraalVMEvalException.class) {
            facade.eval(js, script) as Value
        }
    }

}
