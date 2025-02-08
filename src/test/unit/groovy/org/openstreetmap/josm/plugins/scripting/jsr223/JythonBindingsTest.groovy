package org.openstreetmap.josm.plugins.scripting.jsr223

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.gui.MainApplication
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider
import org.openstreetmap.josm.plugins.scripting.ui.ScriptExecutor

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class JythonBindingsTest extends JOSMFixtureBasedTest {

    public static String __file__holder = null
    public static boolean scriptPathInSysPath = false
    public static int sum = 0

    @Test
    void ensure__file__IsSet() {
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

    @Test
    void ensureSysPathIsSet() {
        final jythonDesc = ScriptEngineMetaDataProvider
                .getAvailablePluggedScriptEngines()
                .filter(desc -> desc.isJython())
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No Jython scripting engine found"))

        final executor = new ScriptExecutor(MainApplication.getMainFrame())

        // generate temporary script file
        final scriptFile = File.createTempFile("test-script", "")
        scriptFile.write("""
import sys
from org.openstreetmap.josm.plugins.scripting.jsr223 import JythonBindingsTest 
for path in sys.path:
    if path == '${scriptFile.parent}':
        JythonBindingsTest.scriptPathInSysPath = True 
        """)
        executor.runScriptWithPluggedEngine(jythonDesc, scriptFile)
        assertTrue(scriptPathInSysPath)
        scriptFile.delete()
    }

    @Test
    void ensureImportFromSysPathWorks() {
        final jythonDesc = ScriptEngineMetaDataProvider
                .getAvailablePluggedScriptEngines()
                .filter(desc -> desc.isJython())
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No Jython scripting engine found"))

        final executor = new ScriptExecutor(MainApplication.getMainFrame())

        // generate temporary script file
        final scriptDir = File.createTempDir()
        final moduleFile = new File(scriptDir, "my_module.py")
        final scriptFile = new File(scriptDir, "my_script.py")


        moduleFile.write("""
def add(a, b):
    return a + b
        """)
        scriptFile.write("""
from org.openstreetmap.josm.plugins.scripting.jsr223 import JythonBindingsTest 
from my_module import add
JythonBindingsTest.sum = add(5,3)
""")
        executor.runScriptWithPluggedEngine(jythonDesc, scriptFile)
        assertEquals(8, sum)
        scriptFile.delete()
        moduleFile.delete()

    }
}
