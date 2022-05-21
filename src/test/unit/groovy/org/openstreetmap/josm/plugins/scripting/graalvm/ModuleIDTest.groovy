package org.openstreetmap.josm.plugins.scripting.graalvm

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test

class ModuleIDTest extends GroovyTestCase {

    @Test
    void "ensureValid - accept valid module ID"() {
        ModuleID.ensureValid("valid")
    }

    @Test
    void "ensureValid - reject null"() {
        shouldFail(NullPointerException.class) {
            ModuleID.ensureValid(null)
        }
    }

    @Test
    void "ensureValid - reject leading '_slash_'"() {
        shouldFail(IllegalArgumentException.class) {
            ModuleID.ensureValid("/not-valid")
        }
    }

    @Test
    void "ensureValid - reject leading '_slash_', ignore leading blanks"() {
        shouldFail(IllegalArgumentException.class) {
            ModuleID.ensureValid("   /not-valid")
        }
    }

    @Test
    void "ensureValid - accept leading and trailing blanks"() {
        ModuleID.ensureValid("   not-valid   ")
    }

    @Test
    void "ensureValid - accept leading _dot__slash_"() {
        ModuleID.ensureValid("./valid")
    }

    @Test
    void "ensureValid - accept leading _dot__dot__slash_"() {
        ModuleID.ensureValid("../valid")
    }

    @Test
    void "isAbsolute or isRelative - for absolute module ID"() {
        def id = new ModuleID("foo/bar")
        assertTrue(id.isAbsolute())
        assertFalse(id.isRelative())
    }

    @Test
    void "isAbsolute - return false, for relative ID starting with _dot__slash_"() {
        def id = new ModuleID("./foo/bar")
        assertFalse(id.isAbsolute())
        assertTrue(id.isRelative())
    }

    @Test
    void "isAbsolute - return false, for relative ID starting with _dot__dot__slash_"() {
        def id = new ModuleID("../foo/bar")
        assertFalse(id.isAbsolute())
        assertTrue(id.isRelative())
    }

    @Test
    void "normalized - already normalized"() {
        def id = new ModuleID("foo/bar")
        def normalized = id.normalized()
        assertEquals(id.toString(), normalized.toString())
        assertEquals(id, normalized)
    }

    @Test
    void "normalized - remove trailing 'js'"() {
        def id = new ModuleID("foo/bar.js")
        def normalized = id.normalized()
        assertEquals("foo/bar", normalized.toString())
    }

    @Test
    void "normalized - collapse sequences of _slash_"() {
        def id = new ModuleID("foo////bar///baz//a.js")
        def normalized = id.normalized()
        assertEquals("foo/bar/baz/a", normalized.toString())
    }
}
