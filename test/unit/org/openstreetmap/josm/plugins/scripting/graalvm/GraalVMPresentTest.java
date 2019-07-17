package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.junit.Test;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class GraalVMPresentTest {

    @Test
    public void shouldDetectGraalVMPresent() {
        final boolean isPresent = GraalVMFacadeFactory.isGraalVMPresent();
        assertTrue(isPresent);
    }

    @Test
    public void shouldCreateAGraalVMFacade(){
        final IGraalVMFacade facade = GraalVMFacadeFactory.createGraalVMFacade();
        assertNotNull(facade);
    }

    @Test
    public void shouldDetectANonEmptyListOfLanguages() {
        final IGraalVMFacade facade = GraalVMFacadeFactory.createGraalVMFacade();
        final List<ScriptEngineDescriptor>
            infos = facade.getSupportedLanguages();
        assertFalse(infos.isEmpty());
    }
}
