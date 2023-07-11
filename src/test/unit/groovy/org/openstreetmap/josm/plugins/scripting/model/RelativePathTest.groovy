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
        def path = RelativePath.parse("./").canonicalize()
        def expected = RelativePath.EMPTY
        assertEquals(expected, path)

        path = RelativePath.parse("./foo/./bar").canonicalize()
        expected = RelativePath.of("foo", "bar")
        assertEquals(expected, path)

        path = RelativePath.parse("foo/..").canonicalize()
        assertEquals(RelativePath.EMPTY, path)

        path = RelativePath.parse("foo/../bar/baz/..").canonicalize()
        expected = RelativePath.of("bar")
        assertEquals(expected, path)

        path = RelativePath.parse("foo/../../../").canonicalize()
        assertEquals(RelativePath.EMPTY, path)
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
}
