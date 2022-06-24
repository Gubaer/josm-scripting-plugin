package org.openstreetmap.josm.plugins.scripting.model


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledForJreRange
import org.junit.jupiter.api.condition.JRE
import org.openstreetmap.josm.data.Preferences
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.jsr223.JSR223ScriptEngineProvider
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor.ScriptEngineType

class ScriptEngineDescriptorTest extends JOSMFixtureBasedTest {

    final oracleNashornId = "Oracle Nashorn"

    @Test
    // nashorn isn't available anymore in Java 17
    @DisabledForJreRange(min = JRE.JAVA_17)
    void createDescriptorForPluggedEngine() {
        def sd = new ScriptEngineDescriptor(oracleNashornId)
        assert sd.getEngineId() == oracleNashornId
        assert sd.getEngineType() == ScriptEngineType.PLUGGED
        assert sd.getLanguageName().isPresent()
        assert sd.getLanguageVersion().isPresent()
        assert sd.getEngineName().isPresent()
        assert ! sd.getContentMimeTypes().isEmpty()
        assert sd.getEngineVersion().isPresent()

        shouldFail(NullPointerException) {
            sd = new ScriptEngineDescriptor(null)
        }

        sd = new ScriptEngineDescriptor(ScriptEngineType.PLUGGED, oracleNashornId)
        assert sd.getEngineId() == oracleNashornId
        assert sd.getEngineType() == ScriptEngineType.PLUGGED
        assert sd.getLanguageName().isPresent()
        assert sd.getLanguageVersion().isPresent()
        assert sd.getEngineName().isPresent()
        assert ! sd.getContentMimeTypes().isEmpty()
        assert sd.getEngineVersion().isPresent()

        shouldFail(NullPointerException) {
            sd = new ScriptEngineDescriptor(null, oracleNashornId)
        }
    }

    @Test
    void createDescriptorForEmbeddedEngine() {
        def sd = new ScriptEngineDescriptor(ScriptEngineType.EMBEDDED, "rhino")
        assert sd.getEngineId() == "rhino"
        assert sd.getEngineType() == ScriptEngineType.EMBEDDED
        println("sd.getLanguageName() -> " + sd.getLanguageName())
        assert sd.getLanguageName().empty
        assert sd.getLanguageVersion().empty
        assert sd.getEngineName().empty
        assert sd.getContentMimeTypes() == [] as Set
        assert sd.getEngineVersion().empty

        sd = new ScriptEngineDescriptor(ScriptEngineType.EMBEDDED, "rhino",
                "Mozilla Rhino", "JavaScript","text/javascript")
        assert sd.getEngineId() == "rhino"
        assert sd.getEngineType() == ScriptEngineType.EMBEDDED
        assert sd.getLanguageName().get() == "JavaScript"
        assert sd.getEngineName().get() == "Mozilla Rhino"
        assert sd.getContentMimeTypes() == ["text/javascript"] as Set
        assert sd.getEngineVersion().empty

        sd = new ScriptEngineDescriptor(ScriptEngineType.EMBEDDED, "rhino",
                "Mozilla Rhino", "JavaScript", "text/javascript",
                "v1.0.0", "v2.0.0")
        assert sd.getEngineId() == "rhino"
        assert sd.getEngineType() == ScriptEngineType.EMBEDDED
        assert sd.getLanguageName().get() == "JavaScript"
        assert sd.getEngineName().get() == "Mozilla Rhino"
        assert sd.getContentMimeTypes() == ["text/javascript"] as Set
        assert sd.getEngineVersion().get() == "v1.0.0"
        assert sd.getLanguageVersion().get() == "v2.0.0"
    }

    @Test
    void buildFromPreferences_MissingPreference() {
        def pref = Preferences.main()
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assert sd == ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
    }

    @Test
    void buildFromPreferences_EmbeddedScriptingEngine() {
        def pref = Preferences.main()
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "embedded/rhino")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assert sd != null
        assert sd.isDefault()
    }

    @Test
    void buildFromPreferences_UnknownEmbeddedScriptingEngine() {
        def pref = Preferences.main()
        assertNotNull(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE)
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "embedded/noSuchEmbeddedEngine")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assert sd != null
        assert sd.isDefault()
    }

    @Test
    // nashorn isn't available anymore in Java 17
    @DisabledForJreRange(min = JRE.JAVA_17)
    void buildFromPreferences_PluggedScriptingEngine() {
        def provider = JSR223ScriptEngineProvider.getInstance()
        provider.getScriptEngineFactories().each {factory ->
            println(factory.getEngineName())
        }
        def pref = Preferences.main()
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "plugged/${oracleNashornId}")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assert sd != null
        assert !sd.isDefault()
        assert sd.getEngineType() == ScriptEngineType.PLUGGED
        assert sd.getEngineId() == oracleNashornId
        assert sd.getEngineName().isPresent()
        assert sd.getEngineVersion().isPresent()
        assert !sd.getContentMimeTypes().isEmpty()
    }

    @Test
    void buildFromPreferences_UnknownPluggedScriptingEngine() {
        def pref = Preferences.main()
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE,
                "plugged/noSuchPluggedEngine")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assert sd != null
        assert sd.isDefault()
    }

    @Test
    void isDefault() {
        def sd = ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
        assert sd.isDefault()

        sd = new ScriptEngineDescriptor(ScriptEngineType.PLUGGED, "groovy")
        assert !sd.isDefault()
    }

    @Test
    void "should set and then get content mime types"() {
        def sd = new ScriptEngineDescriptor(ScriptEngineType.EMBEDDED, "rhino",
                "Mozilla Rhino", "JavaScript", "text/javascript",
                "v1.0.0", "v2.0.0")
        def mimeTypes = ["text/plain", "application/javascript"]
        sd.setContentMimeTypes(mimeTypes)
        def mt = sd.getContentMimeTypes()
        mimeTypes.each {t ->
            assert mt.contains(t)
        }
    }
}
