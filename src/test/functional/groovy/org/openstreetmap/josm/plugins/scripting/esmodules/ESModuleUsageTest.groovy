package org.openstreetmap.josm.plugins.scripting.esmodules

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacade
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.ESModuleResolver
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.FileSystemESModuleRepository

import static org.junit.Assert.assertEquals

class ESModuleUsageTest extends JOSMFixtureBasedTest {

    static private def buildSource(String source) {
        return Source.newBuilder("js", source, null)
            .mimeType("application/javascript+module")
            .build()
    }

    @Test
    void "should successfully import and use a function provided by 'module-a' in the file system"() {
        final resolver = ESModuleResolver.instance
        final repo = new FileSystemESModuleRepository(new File(
            getProjectHome(),
            "src/test/resources/sample-modules/module-a"
        ))
        resolver.setUserDefinedRepositories(List.of(repo))

        final script= """
        import {add} from 'module-a'
        add(1,1)
        """
        def context = Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL)
            .allowHostClassLookup(className -> true)
            .allowIO(true)
            .fileSystem(resolver)
            .build()
        GraalVMFacade.populateContext(context)
        final result = context.eval(buildSource(script))
        assertEquals(2, result.asInt())
    }
}
