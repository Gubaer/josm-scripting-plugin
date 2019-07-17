package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.junit.Test;
import static org.junit.Assert.assertFalse;

public class GraalVMNotPresentTest {

    @Test
    public void shouldDetectGraalVMNotPresent() {
        final boolean isPresent = GraalVMFacadeFactory.isGraalVMPresent();
        assertFalse(isPresent);
    }
}
