package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.junit.Test;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GraalVMNotPresentTest {

    @Test
    public void shouldDetectGraalVMNotPresent() {
        final boolean isPresent = GraalVMFacadeFactory.isGraalVMPresent();
        assertFalse(isPresent);
    }

    @Test
    public void shouldProvideAnEmptyStreamOfGraalVMScriptEngines() {
        final List<ScriptEngineDescriptor> engines =
                ScriptEngineMetaDataProvider.getAvailableGraalVMScriptEngines()
                .collect(Collectors.toList());
        assertTrue(engines.isEmpty());
    }
}
