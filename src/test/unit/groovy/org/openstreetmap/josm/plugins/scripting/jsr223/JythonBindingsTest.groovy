package org.openstreetmap.josm.plugins.scripting.jsr223

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.gui.MainApplication
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider
import org.openstreetmap.josm.plugins.scripting.ui.ScriptExecutor

import static org.junit.Assert.assertEquals

class JythonBindingsTest extends JOSMFixtureBasedTest {

    public static String __file__holder = null

    @Test
    void ensureDefaultBindingsAreSet() {
        final jythonDesc = ScriptEngineMetaDataProvider
            .getAvailablePluggedScriptEngines()
            .filter(desc -> desc.isJython())
            .findAny()
            .orElseThrow(() -> new IllegalStateException("No Jython scripting engine found"))

        final executor = new ScriptExecutor(MainApplication.getMainFrame())

        // generate temporary script file
        final scriptFile = File.createTempFile("test-script", "")
        scriptFile.write("""
from org.openstreetmap.josm.plugins.scripting.jsr223 import JythonBindingsTest
JythonBindingsTest.__file__holder = __file__
        """)
        executor.runScriptWithPluggedEngine(jythonDesc, scriptFile)
        assertEquals(scriptFile.getPath(), __file__holder)
        scriptFile.delete()
    }
}
