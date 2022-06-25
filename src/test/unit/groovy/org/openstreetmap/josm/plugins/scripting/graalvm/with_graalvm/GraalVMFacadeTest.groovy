package org.openstreetmap.josm.plugins.scripting.graalvm.with_graalvm

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacade
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor

class GraalVMFacadeTest extends AbstractGraalVMBasedTest {

    @Test
    void "getOrCreateDefaultContent() - can create a default context"() {
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
    void "getOrCreateDefaultContent() - rejects null engine descriptor"() {
        final facade = new GraalVMFacade()
        shouldFail(NullPointerException) {
            facade.getOrCreateDefaultContext(null)
        }
    }

    @Test
    void "getOrCreateDefaultContent() - rejects non-GraalJS descriptor"() {
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
    void "can create and delete a named context"() {
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
}
