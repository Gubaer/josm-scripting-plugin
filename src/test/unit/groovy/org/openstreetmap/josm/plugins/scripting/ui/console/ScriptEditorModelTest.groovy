package org.openstreetmap.josm.plugins.scripting.ui.console

import org.junit.Test
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class ScriptEditorModelTest {

    @Test
    void create() {
        def model = new ScriptEditorModel()
        assert model.getScriptFile().empty
        assert model.getScriptEngineDescriptor() == ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
    }

    @Test
    void createWithDescriptor() {
        def model = new ScriptEditorModel(null)
        assert model.getScriptFile().empty
        assert model.getScriptEngineDescriptor() == ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE

        def desc = new ScriptEngineDescriptor("groovy")
        model = new ScriptEditorModel(desc)
        assert model.getScriptFile().empty
        assert model.getScriptEngineDescriptor() == desc
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

        assert model.getScriptFile().get() == scriptFile
        assert newValue == scriptFile, "Property change listener not notified "
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

        assert model.getScriptEngineDescriptor() == desc
        assert newValue == desc
        model.removePropertyChangeListener(listener)
    }

    @Test
    void setScriptEngineDescriptorWithNull() {
        def model = new ScriptEditorModel()
        model.setScriptEngineDescriptor(null)

        assert model.getScriptEngineDescriptor() == ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE
    }
}
