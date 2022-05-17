package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.Test

class GraalVMEmbeddedInJosmTest extends AbstractGraalVMBasedTest{

    @Test
    void shouldAccessJosmVersionClass() throws GraalVMEvalException {
        final script = """
            const Version = Java.type('org.openstreetmap.josm.data.Version')
            const version = Version.getInstance().getVersionString()
            console.log(`JOSM Version: \${version}`)
        """
        GraalVMFacadeFactory.createGraalVMFacade().eval(graalJSDescriptor, script)
    }
}
