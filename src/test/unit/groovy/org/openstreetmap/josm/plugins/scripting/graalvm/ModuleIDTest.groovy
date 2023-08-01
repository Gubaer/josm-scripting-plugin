package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.model.RelativePath

import static org.junit.Assert.*

class ModuleIDTest {

    @Test
    void "isAbsolute or isRelative - for absolute module ID"() {
        def id = new ModuleID(RelativePath.parse("foo/bar"))
        assertTrue(id.isAbsolute())
        assertFalse(id.isRelative())
    }

    @Test
    void "isAbsolute - return false, for relative ID starting with _dot__slash_"() {
        def id = new ModuleID(RelativePath.parse("./foo/bar"))
        assertFalse(id.isAbsolute())
        assertTrue(id.isRelative())
    }

    @Test
    void "isAbsolute - return false, for relative ID starting with _dot__dot__slash_"() {
        def id = new ModuleID(RelativePath.parse("../foo/bar"))
        assertFalse(id.isAbsolute())
        assertTrue(id.isRelative())
    }

    @Test
    void "normalized - already normalized"() {
        def id = new ModuleID(RelativePath.parse("foo/bar"))
        def normalized = id.normalized()
        assertEquals(id.toString(), normalized.toString())
        assertEquals(id, normalized)
    }

    @Test
    void "normalized - remove trailing 'js'"() {
        def id = new ModuleID(RelativePath.parse("foo/bar.js"))
        def normalized = id.normalized()
        assertEquals("foo/bar", normalized.toString())
    }

    @Test
    void "normalized - collapse sequences of _slash_"() {
        def id = new ModuleID(RelativePath.parse("foo////bar///baz//a.js"))
        def normalized = id.normalized()
        assertEquals("foo/bar/baz/a", normalized.toString())
    }
}
