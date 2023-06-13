package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacade

import java.nio.file.Path

class ESModuleResolverTest extends BaseTestCase{

    @Test
    void "can resolve an existing module in the file system"() {
        final resolver = ESModuleResolver.instance
        final repo = new FileSystemESModuleRepository(new File(
            getProjectHome(),
            "src/test/resources/es-modules"
        ))
        resolver.setUserDefinedRepositories(List.of(repo))
        final resolvedPath = resolver.parsePath(Path.of("foo").toString())
        final expectedPath = Path.of(repo.getUniquePathPrefix().toString(), "foo.mjs")
        assertEquals(expectedPath.toString(), resolvedPath.toString())

        // test toAbsolutePath()
        assertEquals(expectedPath.toAbsolutePath().toString(), resolver.toAbsolutePath(resolvedPath).toString())

        final channel = resolver.newByteChannel(resolvedPath, null)
        assertNotNull(channel)
        channel.close()

        final context = Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL)
            .allowHostClassLookup{className -> true}
            .allowIO(true)
            .fileSystem(resolver)
            .build()
        GraalVMFacade.populateContext(context)

        final js = """
        import {name} from 'foo'
        name
        """
        final source = Source.newBuilder("js", js, null)
                .mimeType("application/javascript+module")
                .build()
        final result = context.eval(source)
        assertEquals("foo", result.asString())
    }

    @Test
    void "can import a module from a jar based ES Module repository"() {
        final repo = new JarESModuleRepository(new File(
            getProjectHome(),
            "src/test/resources/es-modules/es-modules.jar"
        ))
        final resolver = ESModuleResolver.instance
        resolver.setUserDefinedRepositories(List.of(repo))
        final context = Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL)
            .allowHostClassLookup{className -> true}
            .allowIO(true)
            .fileSystem(resolver)
            .build()
        GraalVMFacade.populateContext(context)

        final js = """
        import {name} from 'foo'
        name
        """
        final source = Source.newBuilder("js", js, null)
            .mimeType("application/javascript+module")
            .build()
        final result = context.eval(source)
        assertEquals("foo", result.asString())
    }

    @Test
    void "can import a module with a default export"() {
        final resolver = ESModuleResolver.instance
        final repo = new FileSystemESModuleRepository(new File(
            getProjectHome(),
            "src/test/resources/es-modules"
        ))
        resolver.setUserDefinedRepositories(List.of(repo))

        final context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup{className -> true}
                .allowIO(true)
                .fileSystem(resolver)
                .build()
        GraalVMFacade.populateContext(context)

        final js = """
        import value from 'default-export'
        value.name
        """
        final source = Source.newBuilder("js", js, null)
            .mimeType("application/javascript+module")
            .build()
        final result = context.eval(source)
        assertEquals("default-export", result.asString())
    }
}
