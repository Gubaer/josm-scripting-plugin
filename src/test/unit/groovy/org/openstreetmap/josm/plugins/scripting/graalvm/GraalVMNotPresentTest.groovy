package org.openstreetmap.josm.plugins.scripting.graalvm


import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider

import java.util.stream.Collectors

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class GraalVMNotPresentTest  {

    @Test
    void shouldDetectGraalVMNotPresent() {
        final isPresent = GraalVMFacadeFactory.isGraalVMPresent()
        assertFalse(isPresent)
    }

    @Test
    void shouldProvideAnEmptyStreamOfGraalVMScriptEngines() {
        final engines =
            ScriptEngineMetaDataProvider.getAvailableGraalVMScriptEngines()
            .collect(Collectors.toList())
        assertTrue(engines.isEmpty())
    }
}
