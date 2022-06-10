package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase

import java.nio.file.Path

class ESModuleResolverTest extends BaseTestCase{

    @Test
    void "can resolve an existing module"() {
        final resolver = ESModuleResolver.instance
        final repo = new FileSystemESModuleRepository(new File(
            getProjectHome(),
            "src/test/resources/es-modules"
        ))
        resolver.addRepository(repo)
        final resolvedPath = resolver.parsePath(Path.of("josm").toString())
        final expectedPath = Path.of(repo.getUniquePathPrefix().toString(), "josm.mjs")
        assertEquals(expectedPath.toString(), resolvedPath.toString())

        // test toAbsolutePath()
        assertEquals(expectedPath.toAbsolutePath().toString(), resolver.toAbsolutePath(resolvedPath).toString())

        final channel = resolver.newByteChannel(resolvedPath, null)
        assertNotNull(channel)
        channel.close()
    }
}
