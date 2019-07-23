package org.openstreetmap.josm.plugins.scripting.model

import org.junit.*
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor.ScriptEngineType
import org.openstreetmap.josm.data.Preferences
import org.openstreetmap.josm.spi.preferences.Config

class ScriptEngineDescriptorTest {


    def shouldFail = new GroovyTestCase().&shouldFail

    @Test
    void createDescriptorForPluggedEngine() {
        def sd = new ScriptEngineDescriptor("rhino")
        assert sd.getEngineId() == "rhino"
        assert sd.getEngineType() == ScriptEngineType.PLUGGED
        assert sd.getLanguageName() != null
        assert sd.getEngineName() != null
        assert sd.getContentMimeTypes() == []

        shouldFail(NullPointerException) {
            sd = new ScriptEngineDescriptor(null)
        }

        sd = new ScriptEngineDescriptor(ScriptEngineType.PLUGGED, "rhino")
        assert sd.getEngineId() == "rhino"
        assert sd.getEngineType() == ScriptEngineType.PLUGGED
        assert sd.getLanguageName() != null
        assert sd.getEngineName() != null

        shouldFail(NullPointerException) {
            sd = new ScriptEngineDescriptor(null, "rhino")
        }
    }

    @Test
    void createDescriptorForEmbeddedEngine() {
        def sd = new ScriptEngineDescriptor(ScriptEngineType.EMBEDDED, "rhino")
        assert sd.getEngineId() == "rhino"
        assert sd.getEngineType() == ScriptEngineType.EMBEDDED
        assert sd.getLanguageName().empty()
        assert sd.getEngineName().empty()
        assert sd.getContentMimeTypes() == []

        sd = new ScriptEngineDescriptor(ScriptEngineType.EMBEDDED, "rhino",
                "JavaScript", "Mozilla Rhino", "text/javascript")
        assert sd.getEngineId() == "rhino"
        assert sd.getEngineType() == ScriptEngineType.EMBEDDED
        assert sd.getLanguageName().get() == "JavaScript"
        assert sd.getEngineName().get() == "Mozilla Rhino"
        assert sd.getContentMimeTypes() == ["text/javascript"]
    }

    @Test
    void buildFromPreferencs_MissingPreference() {
        def pref = new Preferences()
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assert sd == ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
    }

    @Test
    void buildFromPreferencs_EmbeddedScriptingEngine() {
        def pref = new Preferences(Config.getPref())
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "embedded/rhino")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assert sd != null
        assert sd == ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
    }

    @Test
    void buildFromPreferencs_UnknownEmbeddedScriptingEngine() {
        def pref = new Preferences(Config.getPref())
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE,
                "embedded/noSuchEmbeddedEngine")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assert sd != null
        assert sd == ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
    }

    @Test
    void buildFromPreferencs_PluggedScriptingEngine() {
        def pref = new Preferences(Config.getPref())
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE, "plugged/javascript")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assert sd != null
        assert sd != ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
        assert sd.getEngineType() == ScriptEngineType.PLUGGED
        assert sd.getEngineId() == "javascript"
    }

    @Test
    void buildFromPreferencs_UnknownPluggedScriptingEngine() {
        def pref = new Preferences(Config.getPref());
        pref.put(PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE,
                "plugged/noSuchPluggedEngine")
        def sd = ScriptEngineDescriptor.buildFromPreferences(pref)
        assert sd != null
        assert sd == ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
    }

    @Test
    void isDefault() {
        def sd = ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
        assert sd.isDefault()
        sd = new ScriptEngineDescriptor(ScriptEngineType.PLUGGED, "groovy")
        assert ! sd.isDefault()
    }
}
