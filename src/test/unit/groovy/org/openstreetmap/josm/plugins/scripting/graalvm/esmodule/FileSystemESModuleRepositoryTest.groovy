package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase
import org.openstreetmap.josm.plugins.scripting.model.RelativePath

import java.nio.file.Files

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
        final path = repo.getUniquePathPrefix().append("josm")
        final resolved = repo.resolveModulePath(path)
        final expected = repo.getUniquePathPrefix().append("josm.mjs")
        assertNotNull(resolved)
        assertEquals(expected, resolved)
    }

    @Test
    void "rejects arbitrary absolute path which are not absolute module paths"() {
        final path = RelativePath.parse("foo/bar/baz")
        final resolved = repo.resolveModulePath(path)
        assertNull(resolved)
    }

    @Test
    void "rejects absolute module path into another repository"() {
        final repo2 = new FileSystemESModuleRepository(new File(getProjectHome(), "src/test/resources/es-modules"))
        final path = repo2.getUniquePathPrefix().append("josm")
        final resolved = repo.resolveModulePath(path)
        assertNull(resolved)
    }

    @Test
    void "can resolve relative paths"() {
        def path = RelativePath.of("josm")
        def resolved = repo.resolveModulePath(path)
        assertNotNull(resolved)
        def expected = repo.getUniquePathPrefix().append("josm.mjs")
        assertEquals(expected, resolved)


        path = RelativePath.parse("josm/foo")
        resolved = repo.resolveModulePath(path)
        assertNotNull(resolved)
        expected = repo.getUniquePathPrefix().append("josm", "foo.mjs")
        assertEquals(expected, resolved)

        path = RelativePath.parse("josm/bar")
        resolved = repo.resolveModulePath(path)
        assertNotNull(resolved)
        expected = repo.getUniquePathPrefix().append("josm", "bar.js")
        assertEquals(expected, resolved)
    }

    @Test
    void "can resolve relative paths with _DOT__SLASH_"() {
        def path = RelativePath.parse("./josm")
        def resolved = repo.resolveModulePath(path)
        assertNotNull(resolved)
        def expected =repo.getUniquePathPrefix().append("josm.mjs")
        assertEquals(expected, resolved)

        path = RelativePath.parse("josm/./foo")
        resolved = repo.resolveModulePath(path)
        assertNotNull(resolved)
        expected = repo.getUniquePathPrefix().append("josm", "foo.mjs")
        assertEquals(expected, resolved)

        path = RelativePath.parse("josm/./bar")
        resolved = repo.resolveModulePath(path)
        assertNotNull(resolved)
        expected = repo.getUniquePathPrefix().append("josm", "bar.js")
        assertEquals(expected, resolved)
    }

    @Test
    void "can resolve relative paths with _DOT__DOT_"() {
        def path = RelativePath.parse("josm/../josm")
        def resolved = repo.resolveModulePath(path)
        assertNotNull(resolved)
        def expected = repo.getUniquePathPrefix().append("josm.mjs")
        assertEquals(expected, resolved)

        path = RelativePath.parse("josm/foo/no-such-subdir/..")
        resolved = repo.resolveModulePath(path)
        assertNotNull(resolved)
        expected = repo.getUniquePathPrefix().append("josm", "foo.mjs")
        assertEquals(expected, resolved)

        path = RelativePath.parse("josm/../josm/bar")
        resolved = repo.resolveModulePath(path)
        assertNotNull(resolved)
        expected = repo.getUniquePathPrefix().append("josm", "bar.js")
        assertEquals(expected, resolved)
    }

    @Test
    void "rejects navigation outside of repo"() {
        def modulePath = repo.getUniquePathPrefix().append(RelativePath.parse("josm/../../josm"))
        def resolved = repo.resolveModulePath(modulePath)
        assertNull(resolved)

        modulePath = RelativePath.parse("josm/foo/../../../josm")
        resolved = repo.resolveModulePath(modulePath)
        assertNull(resolved)
    }
}
