package org.openstreetmap.josm.plugins.scripting.model

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase

import java.nio.file.Path

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class RelativePathTest extends BaseTestCase {

    @Test
    void "of - reject an absolute file"() {
        shouldFail(IllegalArgumentException) {
            RelativePath.of(getProjectHome())
        }
    }

    @Test
    void "of - reject a null file"() {
        shouldFail(NullPointerException) {
            RelativePath.of(null as File)
        }
    }

    @Test
    void "of - accept a relative file"() {
        final path = RelativePath.of(new File("foo/bar"))
        final expected = RelativePath.of("foo", "bar")
        assertEquals(expected, path)
    }

    @Test
    void "of - reject null path"() {
        shouldFail(NullPointerException) {
            RelativePath.of(null as Path)
        }
    }

    @Test
    void "of - reject absolute path"() {
        shouldFail(IllegalArgumentException) {
            RelativePath.of(getProjectHome().toPath())
        }
    }

    @Test
    void "of - accept empty path"() {
        // empty path consists of the only name ""
        def path = Path.of("")
        assertEquals(1, path.getNameCount())
        assertEquals("", path.getName(0).toString())

        path = RelativePath.of(Path.of(""))
        assertEquals(RelativePath.EMPTY, path)
    }

    @Test
    void "of - reject null RelativePath"() {
        shouldFail(NullPointerException) {
            RelativePath.of(null as RelativePath)
        }
    }

    @Test
    void "of - accept other relative path"() {
        def expected = RelativePath.of("foo", "bar")
        def path = RelativePath.of(expected)
        assertEquals(expected, path)
    }

    @Test
    void "of - reject null segments"() {
        shouldFail(NullPointerException) {
            RelativePath.of(null as String)
        }
        shouldFail(NullPointerException) {
            RelativePath.of("foo", null, "bar")
        }
    }

    @Test
    void "of - reject segments with delimiter chars"() {
        shouldFail(IllegalArgumentException) {
            RelativePath.of("foo/bar")
        }
        shouldFail(IllegalArgumentException) {
            RelativePath.of("foo\\bar")
        }
    }

    @Test
    void "of - reject blank segments" () {
        shouldFail(IllegalArgumentException){
            RelativePath.of("foo", "    ", "bar")
        }
    }

    @Test
    void "parse - should accept empty path"() {
        def path = RelativePath.parse("")
        assertEquals(RelativePath.EMPTY, path)
    }

    @Test
    void "parse - should accept relative path"() {
        def path = RelativePath.parse("foo/bar")
        final expectedPath = RelativePath.of("foo", "bar")
        assertEquals(expectedPath, path)
    }

    @Test
    void "parse - should reject null path"() {
        shouldFail(NullPointerException) {
            RelativePath.parse(null)
        }
    }

    @Test
    void "parse - should reject path with '\\'"() {
        shouldFail(IllegalArgumentException) {
            RelativePath.parse("foo\\bar")
        }
    }

    @Test
    void "canonicalize - should properly canonicalize paths"() {
        def path = RelativePath.parse("./").canonical()
        def expected = RelativePath.EMPTY
        assertTrue(path.isPresent())
        assertEquals(expected, path.get())

        path = RelativePath.parse("./foo/./bar").canonical()
        expected = RelativePath.of("foo", "bar")
        assertTrue(path.isPresent())
        assertEquals(expected, path.get())

        path = RelativePath.parse("foo/..").canonical()
        assertTrue(path.isPresent())
        assertEquals(RelativePath.EMPTY, path.get())

        path = RelativePath.parse("foo/../bar/./baz/..").canonical()
        expected = RelativePath.of("bar")
        assertTrue(path.isPresent())
        assertEquals(expected, path.get())

        // canonicalize not possible
        path = RelativePath.parse("foo/../../../").canonical()
        assertTrue(path.isEmpty())
    }

    @Test
    void "getParent - should properly get parent path"() {
        assertTrue(RelativePath.EMPTY.getParent().isEmpty())

        def path = RelativePath.of("foo").getParent().get()
        assertEquals(RelativePath.EMPTY, path)

        path = RelativePath.of("foo", "bar").getParent().get()
        def expected = RelativePath.of("foo")
        assertEquals(expected, path)
    }

    @Test
    void "append - should properly append"() {
        shouldFail(NullPointerException) {
            RelativePath.of("foo").append(null as RelativePath)
        }
        shouldFail(NullPointerException) {
            RelativePath.of("foo").append("bar", null)
        }
        shouldFail(IllegalArgumentException) {
            RelativePath.of("foo").append("bar/baz")
        }
        def path = RelativePath.of("foo").append(RelativePath.EMPTY)
        assertEquals(RelativePath.of("foo"), path)

        path = RelativePath.EMPTY.append(RelativePath.of("foo"))
        assertEquals(RelativePath.of("foo"), path)

        path = RelativePath.of("foo").append(RelativePath.of("bar", "baz"))
        assertEquals(RelativePath.parse("foo/bar/baz"), path)
    }

    @Test
    void "toPath - should convert to path"() {
        def relPath = RelativePath.parse("foo/bar")

        def path = relPath.toPath()
        def expected = Path.of("foo", "bar")
        assertEquals(expected, path)
    }

    @Test
    void "resolveAgainstDirectoryContext - should properly resolve"() {
        def path = RelativePath.parse("foo/bar")
        def context = RelativePath.parse("my/context")
        def expected = RelativePath.parse("my/context/foo/bar")
        def resolved = path.resolveAgainstDirectoryContext(context)
        assertTrue(resolved.isPresent())
        assertEquals(expected, resolved.get())

        path = RelativePath.parse("foo/../../../../")
        context = RelativePath.parse("my/context")
        resolved = path.resolveAgainstDirectoryContext(context)
        assertTrue(resolved.isEmpty())
    }

    @Test
    void "resolveAgainstFileContext - should properly resolve"() {
        // source file 'foo/bar/my-source.mjs' includes and import statement
        //    import './my-module.mjs'
        // Resolution should succeeed
        def path = RelativePath.parse("./my-module.mjs")
        def context = RelativePath.parse("foo/bar/my-source.mjs")
        def expected = RelativePath.parse("foo/bar/my-module.mjs")
        def resolved = path.resolveAgainstFileContext(context)
        assertTrue(resolved.isPresent())
        assertEquals(expected, resolved.get())

        // source file 'foo/bar/my-source.mjs' includes and import statement
        //   import '../my-module.mjs'
        // Resolution should succeed
        path = RelativePath.parse("../my-module.mjs")
        context = RelativePath.parse("foo/bar/my-source.mjs")
        expected = RelativePath.parse("foo/my-module.mjs")
        resolved = path.resolveAgainstFileContext(context)
        assertTrue(resolved.isPresent())
        assertEquals(expected, resolved.get())

        // source file 'foo/bar/my-source.mjs' includes an import statement
        //   import '../../../../my-module.mjs'
        // Resolution should fail
        path = RelativePath.parse("foo/bar/my-source.mjs")
        context = RelativePath.parse("../../../../my-module.mjs")
        resolved = path.resolveAgainstFileContext(context)
        assertTrue(resolved.isEmpty())
    }
}
