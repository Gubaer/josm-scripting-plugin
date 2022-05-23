package org.openstreetmap.josm.plugins.scripting.graalvm

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider

import java.util.stream.Collectors

class GraalVMNotPresentTest extends GroovyTestCase {

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
