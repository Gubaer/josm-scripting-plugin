package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.junit.Test;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineMetaDataProvider;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class GraalVMPresentTest {

    @Test
    public void shouldDetectGraalVMPresent() {
        final boolean isPresent = GraalVMFacadeFactory.isGraalVMPresent();
        assertTrue(isPresent);
    }

    @Test
    public void shouldCreateAGraalVMFacade(){
        final IGraalVMFacade facade =
             GraalVMFacadeFactory.getOrCreateGraalVMFacade();
        assertNotNull(facade);
    }

    @Test
    public void shouldDetectANonEmptyListOfLanguages() {
        final IGraalVMFacade facade =
            GraalVMFacadeFactory.getOrCreateGraalVMFacade();
        final List<ScriptEngineDescriptor>
            infos = facade.getSupportedLanguages();
        assertFalse(infos.isEmpty());
    }

    @Test
    public void shouldProvideANonEmptyStreamOfGraalVMScriptEngines() {
        final List<ScriptEngineDescriptor> engines =
                ScriptEngineMetaDataProvider.getAvailableGraalVMScriptEngines()
                        .collect(Collectors.toList());
        assertFalse(engines.isEmpty());

        final boolean allEnginesAreGraalVMEngines =
            engines.stream().allMatch(engine ->
                   engine.getEngineType()
                == ScriptEngineDescriptor.ScriptEngineType.GRAALVM
            );
        assertTrue(allEnginesAreGraalVMEngines);
    }
}
