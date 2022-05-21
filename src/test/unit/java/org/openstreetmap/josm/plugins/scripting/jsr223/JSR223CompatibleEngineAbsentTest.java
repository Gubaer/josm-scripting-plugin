package org.openstreetmap.josm.plugins.scripting.jsr223;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class JSR223CompatibleEngineAbsentTest {

    @Test
    public void shouldOnlyProvideNashornAsScriptEngine() {
        final List<ScriptEngineDescriptor> engines =
            ScriptEngineMetaDataProvider
                .getAvailablePluggedScriptEngines()
                .collect(Collectors.toList());

        assertEquals(1, engines.size());
        assertEquals("nashorn", engines.get(0).getEngineId());
    }
}
