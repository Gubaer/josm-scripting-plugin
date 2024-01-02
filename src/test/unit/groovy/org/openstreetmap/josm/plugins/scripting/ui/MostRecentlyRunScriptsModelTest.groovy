package org.openstreetmap.josm.plugins.scripting.ui

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.data.Preferences
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys
import org.openstreetmap.josm.plugins.scripting.ui.mru.MostRecentlyRunScriptsModel

import static org.junit.Assert.*

class MostRecentlyRunScriptsModelTest extends JOSMFixtureBasedTest {

    @Test
    void "can get singleton instance"() {
        def model = MostRecentlyRunScriptsModel.getInstance()
        assertNotNull(model)
    }

    @Test
    void  "can load from v0-3-0 preferences"() {
        final helloWorldScript = new File(getProjectHome(), "src/main/resources/scripts/HelloWorld.GraalJS.js")
            .getAbsolutePath()
        final prefs = new Preferences()
        //noinspection GrDeprecatedAPIUsage
        prefs.putList(PreferenceKeys.PREF_KEY_FILE_HISTORY, [helloWorldScript])

        def model = MostRecentlyRunScriptsModel.getInstance()
        model.loadFromPreferences(prefs)

        final actions = model.getRunScriptActions()
        assertEquals(1, actions.size())
        final action = actions.get(0)
        assertEquals(new File(helloWorldScript), action.getScript())
        // until v0.3.0 the plugin we don't know the scripting engine
        assertNull(action.getEngineId())
    }

    @Test
    void  "ignore non-existing script file in v0-3-0 preferences"() {
        // This script doesn't exist.
        final nonExistingScript = new File(getProjectHome(), "no/such/script.js")
            .getAbsolutePath()
        final prefs = new Preferences()

        // preferences refer to a non-existing script
        //noinspection GrDeprecatedAPIUsage
        prefs.putList(PreferenceKeys.PREF_KEY_FILE_HISTORY, [nonExistingScript])

        def model = MostRecentlyRunScriptsModel.getInstance()
        model.loadFromPreferences(prefs)

        // the list of actions build for running script files must not include
        // the non-existing script file
        final actions = model.getRunScriptActions()
        assertTrue(actions.isEmpty())
    }

    @Test
    void  "can load from v0-3-1 preferences"() {
        final helloWorldScript = new File(getProjectHome(), "src/main/resources/scripts/HelloWorld.GraalJS.js")
            .getAbsolutePath()
        final mruEntry = new MostRecentlyRunScriptsModel.Script(helloWorldScript, "graalvm/js")
        assertEquals(helloWorldScript, mruEntry.scriptPath())
        assertEquals("graalvm/js", mruEntry.engineId())

        def map = mruEntry.toMap();
        assertEquals(helloWorldScript,map.get("scriptPath"))
        assertEquals("graalvm/js", map.get("engineId"))

        final prefs = new Preferences()
        //noinspection GrDeprecatedAPIUsage
        // Note: cannot use [mruEntry] to create the list. Triggers a NullPointerException in
        // deep down in JOSMs SPI layer at
        // https://github.com/JOSM/josm/blob/b6550fcdb34e94676e52d6936446e4cbbe017474/src/org/openstreetmap/josm/spi/preferences/MapListSetting.java#L41
        final mruEntries = new ArrayList<Map<String,String>>()
        mruEntries.add(map)
        prefs.putListOfMaps(PreferenceKeys.PREF_KEY_MOST_RECENTLY_USED_SCRIPTS, mruEntries)

        def model = MostRecentlyRunScriptsModel.getInstance()
        model.loadFromPreferences(prefs)

        final actions = model.getRunScriptActions()
        assertEquals(1, actions.size())
        final action = actions.get(0)
        assertEquals(new File(helloWorldScript), action.getScript())
        // starting from v0.3.1 we load an engine ID from the preferences
        assertEquals("graalvm/js", action.getEngineId())
    }
}
