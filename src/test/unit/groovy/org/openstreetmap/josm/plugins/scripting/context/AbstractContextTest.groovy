package org.openstreetmap.josm.plugins.scripting.context

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor

class AbstractContextTest extends BaseTestCase {
    static class MyContext extends AbstractContext {
        MyContext(String id, String displayName, ScriptEngineDescriptor engine, boolean isDefault) {
            super(id, displayName, engine, isDefault)
        }
        MyContext(String displayName, ScriptEngineDescriptor engine) {
            super(displayName, engine)
        }
    }

    @Test
    void "can create context"() {
        final engine = ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
        final context1 = new MyContext("a display name", engine)
        assertNotNull(context1)
        assertEquals("a display name", context1.getDisplayName())
        assertEquals(engine, context1.getScriptEngine())
        assertFalse(context1.isDefault())
        assertNotNull(context1.getId())

        final id = UUID.randomUUID().toString()
        final context2 = new MyContext(id, "a display name", engine, true /* is default */)
        assertNotNull(context2)
        assertEquals(id, context2.getId())
        assertEquals("a display name", context2.getDisplayName())
        assertEquals(engine, context2.getScriptEngine())
        assertTrue(context2.isDefault())
        assertNotSame(context1.getId(), context2.getId())
    }

    @Test
    void "rejects illegal arguments to constructor"() {
        final engine = ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
        shouldFail(NullPointerException) {
            new MyContext(null, engine)
        }
        shouldFail(NullPointerException) {
            new MyContext("a display name", null)
        }
        shouldFail(IllegalArgumentException) {
            new MyContext(" \t  ", engine)
        }

        shouldFail(NullPointerException) {
            new MyContext(null, "a display name", engine, false)
        }

        shouldFail(NullPointerException) {
            new MyContext("an id", null, engine, false)
        }

        shouldFail(NullPointerException) {
            new MyContext("an id", "a display name", null, false)
        }

        shouldFail(IllegalArgumentException) {
            // blank display name
            new MyContext("an id", "  \t  ", engine, false)
        }
    }

}
