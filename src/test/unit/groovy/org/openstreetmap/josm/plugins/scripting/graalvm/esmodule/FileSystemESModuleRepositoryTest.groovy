package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase

import java.nio.file.Files
import java.nio.file.Path

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.*

class FileSystemESModuleRepositoryTest extends BaseTestCase {

    @Test
    void "can create repository"() {
        var root = Files.createTempDirectory("esmodules")
        final repo = new FileSystemESModuleRepository(root.toFile())
        assertNotNull(repo)
    }

    @Test
    void "create repo with null root - should fail"() {
        shouldFail(NullPointerException) {
            new FileSystemESModuleRepository(null)
        }
    }

    @Test
    void "create repo with non-existing root - should fail"() {
        shouldFail(IllegalArgumentException) {
            new FileSystemESModuleRepository(new File("/no/such/directory"))
        }
    }

    @Test
    void "create repo with file as root instead of directory - should fail"() {
        final file = Files.createTempFile("fake-esmodules-root", "txt").toFile()
        shouldFail(IllegalArgumentException) {
            new FileSystemESModuleRepository(file)
        }
    }

    private FileSystemESModuleRepository repo

    @BeforeEach
    void createRepo() {
        repo = new FileSystemESModuleRepository(new File(getProjectHome(), "src/test/resources/es-modules"))
    }


    @Test
    void "can resolve absolute module path into this repository"() {
        final absolutePath = Path.of(repo.getUniquePathPrefix().toString(), "josm")
        final resolved = repo.resolveModulePath(absolutePath)
        assertNotNull(resolved)
        assertTrue(resolved.toString().startsWith(absolutePath.toString()))
    }

    @Test
    void "rejects arbitrary absolute path which are not absolute module paths"() {
        final absolutePath = "/foo/bar/baz"
        final resolved = repo.resolveModulePath(absolutePath)
        assertNull(resolved)
    }

    @Test
    void "rejects absolute module path into another repository"() {
        final repo2 = new FileSystemESModuleRepository(new File(getProjectHome(), "src/test/resources/es-modules"))
        final absolutePath = Path.of(repo2.getUniquePathPrefix().toString(), "josm")
        final resolved = repo.resolveModulePath(absolutePath)
        assertNull(resolved)
    }

    @Test
    void "can resolve relative paths"() {
        def relativePath = Path.of("josm")
        def resolved = repo.resolveModulePath(relativePath)
        assertNotNull(resolved)
        def expected = Path.of(repo.getUniquePathPrefix().toString(), "josm.mjs")
        assertEquals(expected, resolved)


        relativePath = Path.of("josm/foo")
        resolved = repo.resolveModulePath(relativePath)
        assertNotNull(resolved)
        expected = Path.of(repo.getUniquePathPrefix().toString(), "josm/foo.mjs")
        assertEquals(expected, resolved)

        relativePath = Path.of("josm/bar")
        resolved = repo.resolveModulePath(relativePath)
        assertNotNull(resolved)
        expected = Path.of(repo.getUniquePathPrefix().toString(), "josm/bar.js")
        assertEquals(expected, resolved)
    }

    @Test
    void "can resolve relative paths with _DOT__SLASH_"() {
        def relativePath = Path.of("./josm")
        def resolved = repo.resolveModulePath(relativePath)
        assertNotNull(resolved)
        def expected = Path.of(repo.getUniquePathPrefix().toString(), "josm.mjs")
        assertEquals(expected, resolved)

        relativePath = Path.of("josm/./foo")
        resolved = repo.resolveModulePath(relativePath)
        assertNotNull(resolved)
        expected = Path.of(repo.getUniquePathPrefix().toString(), "josm/foo.mjs")
        assertEquals(expected, resolved)

        relativePath = Path.of("josm/./bar")
        resolved = repo.resolveModulePath(relativePath)
        assertNotNull(resolved)
        expected = Path.of(repo.getUniquePathPrefix().toString(), "josm/bar.js")
        assertEquals(expected, resolved)
    }

    @Test
    void "can resolve relative paths with _DOT__DOT_"() {
        def relativePath = Path.of("josm/../josm")
        def resolved = repo.resolveModulePath(relativePath)
        assertNotNull(resolved)
        def expected = Path.of(repo.getUniquePathPrefix().toString(), "josm.mjs")
        assertEquals(expected, resolved)

        relativePath = Path.of("josm/foo/no-such-subdir/..")
        resolved = repo.resolveModulePath(relativePath)
        assertNotNull(resolved)
        expected = Path.of(repo.getUniquePathPrefix().toString(), "josm/foo.mjs")
        assertEquals(expected, resolved)

        relativePath = Path.of("josm/../josm/bar")
        resolved = repo.resolveModulePath(relativePath)
        assertNotNull(resolved)
        expected = Path.of(repo.getUniquePathPrefix().toString(), "josm/bar.js")
        assertEquals(expected, resolved)
    }

    @Test
    void "rejects navigation outside of repo"() {
        def modulePath = Path.of(repo.getUniquePathPrefix().toString(), "josm/../../josm")
        def resolved = repo.resolveModulePath(modulePath)
        assertNull(resolved)

        modulePath = "josm/foo/../../../josm"
        resolved = repo.resolveModulePath(modulePath)
        assertNull(resolved)
    }

}
