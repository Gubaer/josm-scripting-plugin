package org.openstreetmap.josm.plugins.scripting.jsr223;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JSR223CompatibleEnginePresentTest {

    @Test
    public void shouldProvideANonEmptyStreamOfPluggedEngines() {
        final List<ScriptEngineDescriptor> engines =
            ScriptEngineMetaDataProvider
                .getAvailablePluggedScriptEngines()
                .collect(Collectors.toList());
        assertFalse(engines.isEmpty());

        final boolean allEnginesArePluggedEngines =
            engines.stream().allMatch(engine ->
                   engine.getEngineType()
                == ScriptEngineDescriptor.ScriptEngineType.PLUGGED
            );
        assertTrue(allEnginesArePluggedEngines);
    }
}
