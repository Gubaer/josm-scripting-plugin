package org.openstreetmap.josm.plugins.scripting.context


import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor

class HostedContextsStateTest extends BaseTestCase {

    static class MyContext extends AbstractContext {
        MyContext(String id, String displayName, ScriptEngineDescriptor engine, boolean isDefault) {
            super(id, displayName, engine, isDefault)
        }
        MyContext(String displayName, ScriptEngineDescriptor engine) {
            super(displayName, engine)
        }
    }

    @Test
    void "can create HostedContextsState"() {
        final engine = ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
        def state = new HostedContextsState(
            engine,
            new MyContext("context 01", engine),
            [new MyContext("context 02", engine)]
        )
        assertNotNull(state)
        assertEquals("context 01", state.getDefaultContext().getDisplayName())
        assertEquals(engine, state.getDefaultContext().getScriptEngine())
        assertEquals(1, state.getUserDefinedContexts().size())
        assertEquals("context 02", state.getUserDefinedContexts().get(0).getDisplayName())

        state = new HostedContextsState(
            engine,
            null,
            [new MyContext("context 02", engine)]
        )
        assertNotNull(state)
        assertNull(state.getDefaultContext())

        state = new HostedContextsState(
            engine,
            new MyContext("context 01", engine),
            null
        )
        assertNotNull(state)
        assertNotNull(state.getUserDefinedContexts())
        assertEquals(0, state.getUserDefinedContexts().size())
    }

    @Test
    void "rejects illegal arguments to constructor"() {

        final engine = ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
        shouldFail(NullPointerException) {
            // reject null engine
            new HostedContextsState(
                null,
                new MyContext("context 01", engine),
                [new MyContext("context 02", engine)]
            )
        }
    }
}
