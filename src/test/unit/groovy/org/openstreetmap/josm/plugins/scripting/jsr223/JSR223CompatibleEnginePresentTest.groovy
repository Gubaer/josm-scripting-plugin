package org.openstreetmap.josm.plugins.scripting.jsr223

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider

import java.util.stream.Collectors

class JSR223CompatibleEnginePresentTest extends GroovyTestCase {

    @Test
    void shouldProvideANonEmptyStreamOfPluggedEngines() {
        final engines = ScriptEngineMetaDataProvider
            .getAvailablePluggedScriptEngines()
            .collect(Collectors.toList())
        assertFalse(engines.isEmpty())

        final  allEnginesArePluggedEngines =
            engines.stream().allMatch{engine ->
                engine.getEngineType() == ScriptEngineDescriptor.ScriptEngineType.PLUGGED
        }
        assertTrue(allEnginesArePluggedEngines)
    }
}
