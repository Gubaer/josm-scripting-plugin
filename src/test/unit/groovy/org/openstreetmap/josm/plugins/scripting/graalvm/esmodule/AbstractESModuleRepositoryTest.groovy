package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase

import java.nio.file.Path

class AbstractESModuleRepositoryTest extends BaseTestCase{

    @Test
    void "accepts ES Module path with correct prefix"() {
        final path = Path.of("/" + IESModuleRepository.ES_MODULE_REPO_PATH_PREFIX, UUID.randomUUID().toString());
        assertTrue(AbstractESModuleRepository.startsWithESModuleRepoPathPrefix(path));
    }

    @Test
    void "rejects illegal ES Module paths with incorrect prefix"() {
        def path

        // path is null
        assertFalse(AbstractESModuleRepository.startsWithESModuleRepoPathPrefix(null))

        // path is not absolute
        path = Path.of("foo/bar")
        assertFalse(AbstractESModuleRepository.startsWithESModuleRepoPathPrefix(path))

        // path is too short
        path = Path.of("/foo")
        assertFalse(AbstractESModuleRepository.startsWithESModuleRepoPathPrefix(path))

        // path doesn't start with 'es-module-repo'
        path = Path.of("/foo/bar/baz")
        assertFalse(AbstractESModuleRepository.startsWithESModuleRepoPathPrefix(path))

        // second component isn't an UUID
        path = Path.of("/${IESModuleRepository.ES_MODULE_REPO_PATH_PREFIX}/foo/baz")
        assertFalse(AbstractESModuleRepository.startsWithESModuleRepoPathPrefix(path))
    }
}
