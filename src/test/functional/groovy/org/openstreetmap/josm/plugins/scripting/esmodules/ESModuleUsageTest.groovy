package org.openstreetmap.josm.plugins.scripting.esmodules

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacade
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.ESModuleResolver
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.FileSystemESModuleRepository
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.JarESModuleRepository
import org.openstreetmap.josm.plugins.scripting.model.RelativePath

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

    @Test
    void "should successfully import and use a function provided by 'module-a' in a jar repo"() {
        final resolver = ESModuleResolver.instance
        final repo = new JarESModuleRepository(
                new File(
                    getProjectHome(),
                "src/test/resources/sample-modules/sample-modules.jar"
                ),
                RelativePath.of("module-a")
            )

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

    @Test
    void "should successfully import and use a function provided by 'module-b' in the file system"() {
        final resolver = ESModuleResolver.instance
        final repo = new FileSystemESModuleRepository(new File(
                getProjectHome(),
                "src/test/resources/sample-modules/module-b"
        ))
        resolver.setUserDefinedRepositories(List.of(repo))

        final script= """
        import {add, mult} from 'module-b'
        const r1 = add(1,1)
        const r2 = mult(2,2)
        const r = [r1, r2]
        r
        """
        def context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup(className -> true)
                .allowIO(true)
                .fileSystem(resolver)
                .build()
        GraalVMFacade.populateContext(context)
        final result = context.eval(buildSource(script))
        assertEquals(2, result.getArrayElement(0).asInt())
        assertEquals(4, result.getArrayElement(1).asInt())
    }

    @Test
    void "should successfully import and use a function provided by 'module-b' in a jar file"() {
        final resolver = ESModuleResolver.instance
        final repo = new JarESModuleRepository(
                new File(
                        getProjectHome(),
                        "src/test/resources/sample-modules/sample-modules.jar"
                ),
                RelativePath.of("module-b")
        )
        resolver.setUserDefinedRepositories(List.of(repo))

        final script= """
        import {add, mult} from 'module-b'
        const r1 = add(1,1)
        const r2 = mult(2,2)
        const r = [r1, r2]
        r
        """
        def context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup(className -> true)
                .allowIO(true)
                .fileSystem(resolver)
                .build()
        GraalVMFacade.populateContext(context)
        final result = context.eval(buildSource(script))
        assertEquals(2, result.getArrayElement(0).asInt())
        assertEquals(4, result.getArrayElement(1).asInt())
    }
}
