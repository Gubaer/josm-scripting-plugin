package org.openstreetmap.josm.plugins.scripting.model

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.data.Preferences
import org.openstreetmap.josm.plugins.scripting.BaseTestCase
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor.ScriptEngineType

import static org.junit.Assert.assertNull

class ScriptEngineDescriptorTest extends BaseTestCase {

    @Test
    void "should build from preferences if missing preferences"() {
        def pref = new Preferences()
        def sd = ScriptEngineDescriptor.buildDefaultFromPreferences(pref)
        assertNull(sd)
    }

    @Test
    void "should build from preferences if preference is legacy embedded _SLASH_ rhino"() {
        def pref = new Preferences()
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "embedded/rhino")
        def sd = ScriptEngineDescriptor.buildDefaultFromPreferences(pref)
        assertNull(sd)
    }

    @Test
    void "should build from preferences if scripting engine is unknown embedded engine"() {
        def pref = new Preferences()
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "embedded/noSuchEmbeddedEngine")
        def sd = ScriptEngineDescriptor.buildDefaultFromPreferences(pref)
        assertNull(sd)
    }

    @Test
    void "should build from preferences if scripting engine is unknown plugged engine"() {
        def pref = new Preferences()
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "plugged/noSuchPluggedEngine")
        def sd = ScriptEngineDescriptor.buildDefaultFromPreferences(pref)
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
        mimeTypes.each { t ->
            assert mt.contains(t)
        }
    }
}
