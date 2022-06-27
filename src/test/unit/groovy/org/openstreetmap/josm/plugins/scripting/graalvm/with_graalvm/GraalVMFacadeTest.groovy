package org.openstreetmap.josm.plugins.scripting.graalvm.with_graalvm

import org.graalvm.polyglot.Value
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacade
import org.openstreetmap.josm.plugins.scripting.graalvm.IGraalVMContext
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor

class GraalVMFacadeTest extends AbstractGraalVMBasedTest {

    @Test
    void "getOrCreateDefaultContext() - can create a default context"() {
        final facade = new GraalVMFacade()
        // should create a default context
        assertFalse(facade.existsDefaultContext(graalJSDescriptor))
        def context = facade.getOrCreateDefaultContext(graalJSDescriptor)
        assertNotNull(context)
        assertNotNull(context.getId())
        assertTrue(facade.existsDefaultContext(graalJSDescriptor))

        // try again
        def otherContext = facade.getOrCreateDefaultContext(graalJSDescriptor)
        assertNotNull(context)
        assertNotNull(context.getId())

        // the default context should be the same after the second invocation
        assertEquals(context, otherContext)
    }

    @Test
    void "getOrCreateDefaultContext() - rejects null engine descriptor"() {
        final facade = new GraalVMFacade()
        shouldFail(NullPointerException) {
            facade.getOrCreateDefaultContext(null)
        }
    }

    @Test
    void "getOrCreateDefaultContext() - rejects non-GraalJS descriptor"() {
        final engine = new ScriptEngineDescriptor(
            ScriptEngineDescriptor.ScriptEngineType.EMBEDDED,
            "nashorn"
        )
        final facade = new GraalVMFacade()
        shouldFail(IllegalArgumentException) {
            facade.getOrCreateDefaultContext(engine)
        }
    }

    @Test
    void "can create and delete a user defined context context"() {
        final facade = new GraalVMFacade()
        final context = facade.createContext("test", graalJSDescriptor)
        assertNotNull(context)
        assertNotNull(context.id)

        final context2 = facade.lookupContext(context.id, graalJSDescriptor)
        assertNotNull(context2)
        assertEquals(context, context2)

        facade.closeAndRemoveContext(context)
        final context3 = facade.lookupContext(context.id, graalJSDescriptor)
        assertNull(context3)
    }

    @Test
    void "can evaluate a script in a user defined context"() {
        final facade = new GraalVMFacade()
        final context = facade.createContext("test", graalJSDescriptor)
        assertTrue(context instanceof IGraalVMContext)

        final graalVMContext = (IGraalVMContext) context;
        final script = """
            let foo = "bar"
            foo
        """
        final object = graalVMContext.eval(script)
        assertTrue(object instanceof Value)
        final value = (Value) object
        assertEquals("bar", value.asString())
    }
}
