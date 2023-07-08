package org.openstreetmap.josm.plugins.scripting.esmodules

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.openstreetmap.josm.plugins.scripting.BaseTestCase

import java.nio.file.Path
import java.util.jar.JarFile
import java.util.regex.Pattern

import static org.junit.Assert.*
import static org.junit.jupiter.api.condition.OS.WINDOWS

class PathTests extends BaseTestCase {

    @Test
    void "test subpaths"(){
        final path = Path.of("/foo/bar/baz/file.js")
        println(path.subpath(2, 4).toString())
    }

    @Test
    void "test root path"() {
        final path = Path.of("/")
        println("path: $path")
    }

    @Test
    void "absolute path"() {
        final path = Path.of("foo/bar/../baz").normalize().toAbsolutePath()
        println("path: $path")
    }

    @Test
    void "paths in jar files"() {
        final jar = new JarFile(new File(getProjectHome(), 'src/test/resources/es-modules/es-modules.jar'))
        def entry = jar.getEntry("josm")
        assertNotNull(entry)
        assertTrue(entry.isDirectory())

        entry = jar.getEntry("josm/bar.js")
        assertNotNull(entry)
        assertFalse(entry.isDirectory())

        entry = jar.getEntry("no/such/entry")
        assertNull(entry)
    }

    @Test
    // doesn't work on windows platform
    @DisabledOnOs(WINDOWS)
    void "remove leading _SLASH_"() {
        def path = Path.of("/foo/bar/baz")
        assertTrue(path.isAbsolute())
        // path = path.subpath(1, path.getNameCount())
        // assertFalse(path.isAbsolute())
        // assertEquals("foo/bar/baz", path.toString())

        def pattern = Pattern.compile("^/+")
        def fixed = pattern.matcher(path.toString()).replaceFirst("")
        assertEquals("foo/bar/baz", fixed)
    }

    @Test
    void "empty path"() {
        def path = Path.of("")
        // an empty path has one name segment
        assertEquals(1, path.getNameCount())
    }
}
