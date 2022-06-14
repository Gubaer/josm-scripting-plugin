package org.openstreetmap.josm.plugins.scripting.graalvm.api

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacade
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.ESModuleResolver
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.FileSystemESModuleRepository

class APIV3TestSuite extends AbstractGraalVMBasedTest {

    private ESModuleResolver resolver
    private Context context

    @BeforeEach
    void setupResolverAndContext() {
        resolver = ESModuleResolver.instance
        final repo1 = new FileSystemESModuleRepository(new File(
            getProjectHome(),
            "src/main/javascript/v3"
        ))
        final repo2 = new FileSystemESModuleRepository(new File(
            getProjectHome(),
            "src/test/unit/javascript/v3"
        ))
        resolver.setRepositories(List.of(repo1, repo2))

        context = Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL)
            .allowHostClassLookup(className -> true)
            .allowIO(true)
            .fileSystem(resolver)
            .build()
        GraalVMFacade.populateContext(context)
    }

    @AfterEach
    void tearDownContext() {
        context?.close()
        context = null
    }

    static private def buildSource(String source) {
        return Source.newBuilder("js", source, null)
            .mimeType("application/javascript+module")
            .build()
    }

    @Test
    void "can run API V3 unit tests"() {

        final source = """
        import * as suite from 'suite'
        suite.fragileRun()
        """

        final result = context.eval(buildSource(source))
    }
}
