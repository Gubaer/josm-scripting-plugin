package org.openstreetmap.josm.plugins.scripting.jsr223

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider

import java.util.stream.Collectors

class JSR223CompatibleEngineAbsentTest extends GroovyTestCase {

    @Test
    void shouldOnlyProvideNashornAsScriptEngine() {
        final engines = ScriptEngineMetaDataProvider
            .getAvailablePluggedScriptEngines()
            .collect(Collectors.toList())

        assertEquals(1, engines.size())
        assertEquals("nashorn", engines.get(0).getEngineId())
    }
}
