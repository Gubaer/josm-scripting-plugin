package org.openstreetmap.josm.plugins.scripting.graalvm.with_graalvm


import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMEvalException

class GraalVMEmbeddedInJosmTest extends AbstractGraalVMBasedTest {

    @Test
    void shouldAccessJosmVersionClass() throws GraalVMEvalException {
        final script = """
            const Version = Java.type('org.openstreetmap.josm.data.Version')
            const version = Version.getInstance().getVersionString()
            console.log(`JOSM Version: \${version}`)
        """
        facade.eval(graalJSDescriptor, script)
    }
}
