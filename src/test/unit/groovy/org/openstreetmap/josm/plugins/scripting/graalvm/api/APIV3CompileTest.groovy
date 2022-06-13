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

class APIV3CompileTest extends AbstractGraalVMBasedTest {

    private ESModuleResolver resolver
    private Context context;


    @BeforeEach
    void setupResolverAndContext() {
        resolver = ESModuleResolver.instance
        final repo = new FileSystemESModuleRepository(new File(
                getProjectHome(),
                "src/main/javascript/v3"
        ))
        resolver.setRepositories(List.of(repo))

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
    void "can import API V3 modules"() {

        final source = """
        import 'josm/util'
        import 'josm'
        import 'josm/api'
        import {Api, ChangesetApi,  ApiConfig} from 'josm/api'
        import 'josm/builder'
        """

        final result = context.eval(buildSource(source))
    }
}
