package org.openstreetmap.josm.plugins.scripting.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledForJreRange
import org.junit.jupiter.api.condition.JRE
import org.openstreetmap.josm.data.Preferences
import org.openstreetmap.josm.plugins.scripting.BaseTestCase
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor.ScriptEngineType

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.*

class ScriptEngineDescriptorTest extends BaseTestCase {

    final oracleNashornId = "Oracle Nashorn"

    @Test
    // nashorn isn't available anymore in Java 17
    @DisabledForJreRange(min = JRE.JAVA_17)
    void "should create descriptor for plugged engine"() {
        def sd = new ScriptEngineDescriptor(oracleNashornId)
        assertEquals(oracleNashornId, sd.getEngineId())
        assertEquals(ScriptEngineType.PLUGGED, sd.getEngineType())
        assertTrue(sd.getLanguageName().isPresent())
        assertTrue(sd.getLanguageVersion().isPresent())
        assertTrue(sd.getEngineName().isPresent())
        assertFalse(sd.getContentMimeTypes().isEmpty())
        assertTrue(sd.getEngineVersion().isPresent())

        shouldFail(NullPointerException) {
            sd = new ScriptEngineDescriptor(null)
        }

        sd = new ScriptEngineDescriptor(ScriptEngineType.PLUGGED, oracleNashornId)
        assertEquals(oracleNashornId, sd.getEngineId())
        assertEquals(ScriptEngineType.PLUGGED, sd.getEngineType())
        assertTrue(sd.getLanguageName().isPresent())
        assertTrue(sd.getLanguageVersion().isPresent())
        assertTrue(sd.getEngineName().isPresent())
        assertFalse(sd.getContentMimeTypes().isEmpty())
        assertTrue(sd.getEngineVersion().isPresent())

        shouldFail(NullPointerException) {
            sd = new ScriptEngineDescriptor(null, oracleNashornId)
        }
    }

    @Test
    void "should build from preferences if missing preferences"() {
        def pref = new Preferences()
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assertNull(sd)
    }

    @Test
    void "should build from preferences if preference is legacy embedded _SLASH_ rhino"() {
        def pref = new Preferences()
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "embedded/rhino")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assertNull(sd)
    }

    @Test
    void "should build from preferences if scripting engine is unknown embedded engine"() {
        def pref = new Preferences()
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "embedded/noSuchEmbeddedEngine")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assertNull(sd)
    }

    @Test
    // nashorn isn't available anymore in Java 17
    @DisabledForJreRange(min = JRE.JAVA_17)
    void "should build from preferences if scripting engine is plugged _SLASH_ Oracle Nashorn"() {
        def provider = JSR223ScriptEngineProvider.getInstance()
        provider.getScriptEngineFactories().each {factory ->
            println(factory.getEngineName())
        }
        def pref = new Preferences()
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "plugged/${oracleNashornId}")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assertNotNull(sd)
        assertEquals(oracleNashornId, sd.getEngineId())
        assertEquals(ScriptEngineType.PLUGGED, sd.getEngineType())
        assertTrue(sd.getEngineName().isPresent())
        assertTrue(sd.getEngineVersion().isPresent())
        assertFalse(sd.getContentMimeTypes().isEmpty())
    }

    @Test
    void "should build from preferences if scripting engine is unknown plugged engine"() {
        def pref = new Preferences()
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "plugged/noSuchPluggedEngine")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assertNull(sd)
    }


    @Test
    void "should set and then get content mime types"() {
        def sd = new ScriptEngineDescriptor(ScriptEngineType.PLUGGED, "test-id",
                "Test Name", "JavaScript", "text/javascript",
                "v1.0.0", "v2.0.0")
        def mimeTypes = ["text/plain", "application/javascript"]
        sd.setContentMimeTypes(mimeTypes)
        def mt = sd.getContentMimeTypes()
        mimeTypes.each {t ->
            assert mt.contains(t)
        }
    }
}
