package org.openstreetmap.josm.plugins.scripting.jsr223


import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider

import java.util.stream.Collectors

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class JSR223CompatibleEnginePresentTest extends BaseTestCase {

    @Test
    void shouldProvideANonEmptyStreamOfPluggedEngines() {
        final engines = ScriptEngineMetaDataProvider
            .getAvailablePluggedScriptEngines()
            .collect(Collectors.toList())
        assertFalse(engines.isEmpty())

        final  allEnginesArePluggedEngines =
            engines.stream().allMatch(engine ->
                engine.getEngineType() == ScriptEngineDescriptor.ScriptEngineType.PLUGGED
            )
        assertTrue(allEnginesArePluggedEngines)
    }
}
