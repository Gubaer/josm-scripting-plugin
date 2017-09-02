package org.openstreetmap.josm.plugins.scripting

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import org.junit.runners.Suite
import org.openstreetmap.josm.plugins.scripting.js.JOSMModuleScriptProviderTest
import org.openstreetmap.josm.plugins.scripting.js.api.AddMultiCommandTest
import org.openstreetmap.josm.plugins.scripting.js.api.ChangeMultiCommandTest
import org.openstreetmap.josm.plugins.scripting.js.api.ChangeTest
import org.openstreetmap.josm.plugins.scripting.js.api.DeleteMultiCommandTest
import org.openstreetmap.josm.plugins.scripting.model.CommonJSModuleRepositoryTest
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptorTest
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineTypeTest
import org.openstreetmap.josm.plugins.scripting.ui.console.ScriptEditorModelTest

@RunWith(Suite)
@SuiteClasses([
    JOSMModuleScriptProviderTest,
    AddMultiCommandTest,
    ChangeMultiCommandTest,
    ChangeTest,
    DeleteMultiCommandTest,
    CommonJSModuleRepositoryTest,
    ScriptEngineDescriptorTest,
    ScriptEngineTypeTest,
    ScriptEditorModelTest
])
class AllUnitTests {}
