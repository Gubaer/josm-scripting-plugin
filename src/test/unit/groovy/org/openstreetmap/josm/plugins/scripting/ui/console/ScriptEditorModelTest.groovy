package org.openstreetmap.josm.plugins.scripting.ui.console

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

import static org.junit.Assert.*

class ScriptEditorModelTest extends BaseTestCase {

    @Test
    void create() {
        def model = new ScriptEditorModel()
        assertTrue(model.getScriptFile().empty)
    }

    @Test
    void createWithDescriptor() {
        def model = new ScriptEditorModel(null)
        assertTrue(model.getScriptFile().empty)

        def desc = new ScriptEngineDescriptor("groovy")
        model = new ScriptEditorModel(desc)
        assertTrue(model.getScriptFile().empty)
        assertEquals(desc,model.getScriptEngineDescriptor())
    }

    @Test
    void setFileAndNotifyListeners() {
        def newValue
        def listener = [
            propertyChange: {PropertyChangeEvent evt ->
                newValue = evt.getNewValue()
            }
        ] as PropertyChangeListener

        def model = new ScriptEditorModel()
        model.addPropertyChangeListener(listener)
        def scriptFile = new File("myscript.js")
        model.setScriptFile(scriptFile)

        assertEquals(scriptFile, model.getScriptFile().get())
        assertEquals(scriptFile, newValue)
        model.removePropertyChangeListener(listener)
    }

    @Test
    void setScriptEngineDescriptorAndNotifyListeners() {
        def newValue
        def listener = [
            propertyChange: {PropertyChangeEvent evt ->
                newValue = evt.getNewValue()
            }
        ] as PropertyChangeListener

        def model = new ScriptEditorModel()
        model.addPropertyChangeListener(listener)
        def desc = new ScriptEngineDescriptor("groovy")
        model.setScriptEngineDescriptor(desc)

        assertEquals(desc, model.getScriptEngineDescriptor())
        assertEquals(desc, newValue)
        model.removePropertyChangeListener(listener)
    }

    @Test
    void setScriptEngineDescriptorWithNull() {
        def model = new ScriptEditorModel()
        model.setScriptEngineDescriptor(null)

        assertNull(ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE, model.getScriptEngineDescriptor())
    }
}
