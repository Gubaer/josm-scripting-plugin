package org.openstreetmap.josm.plugins.scripting.model

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor.ScriptEngineType
import static org.junit.Assert.*

class ScriptEngineTypeTest {

    @Test
    void "should initialize engine type from supported engine preference value"() {
        def type

        type = ScriptEngineType.fromPreferencesValue("plugged/rhino")
        assertEquals(ScriptEngineType.PLUGGED, type)

        type = ScriptEngineType.fromPreferencesValue("graalvm/s")
        assertEquals(ScriptEngineType.GRAALVM, type)

        type = ScriptEngineType.fromPreferencesValue("plugged")
        assertEquals(ScriptEngineType.PLUGGED, type)

        type = ScriptEngineType.fromPreferencesValue("graalvm")
        assertEquals(ScriptEngineType.GRAALVM, type)
    }

    @Test
    void "should normalize preference values for scripting engines"() {
        def type

        type = ScriptEngineType.fromPreferencesValue("  GraalVM/JS  ")
        assertEquals(ScriptEngineType.GRAALVM, type)

        type = ScriptEngineType.fromPreferencesValue("\t plugged/rhino  \t")
        assertEquals(ScriptEngineType.PLUGGED, type)

        type = ScriptEngineType.fromPreferencesValue("   plugged  ")
        assertEquals(ScriptEngineType.PLUGGED, type)

        type = ScriptEngineType.fromPreferencesValue("   GrAaLVM  ")
        assertEquals(ScriptEngineType.GRAALVM, type)
    }
}
