package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

class ModuleIDTest {

    @Test
    void "ensureValid: accept valid module ID"() {
        ModuleID.ensureValid("valid")
    }

    @Test( expected = NullPointerException)
    void "ensureValid: reject null"() {
        ModuleID.ensureValid(null)
    }

    @Test(expected = IllegalArgumentException)
    void "ensureValid: reject leading '/'"() {
        ModuleID.ensureValid("/not-valid")
    }

    @Test(expected = IllegalArgumentException)
    void "ensureValid: reject leading '/', ignore leading blanks"() {
        ModuleID.ensureValid("   /not-valid")
    }

    @Test()
    void "ensureValid: accept leading and trailing blanks"() {
        ModuleID.ensureValid("   not-valid   ")
    }

    @Test()
    void "ensureValid: accept leading ./"() {
        ModuleID.ensureValid("./valid")
    }

    @Test()
    void "ensureValid: accept leading ../"() {
        ModuleID.ensureValid("../valid")
    }

    @Test()
    void "isAbsolute/isRelative: for absolute module ID"() {
        def id = new ModuleID("foo/bar")
        assertTrue(id.isAbsolute())
        assertFalse(id.isRelative())
    }

    @Test()
    void "isAbsolute: return false, for relative ID starting with ./"() {
        def id = new ModuleID("./foo/bar")
        assertFalse(id.isAbsolute())
        assertTrue(id.isRelative())
    }

    @Test()
    void "isAbsolute: return false, for relative ID startingwith ../"() {
        def id = new ModuleID("../foo/bar")
        assertFalse(id.isAbsolute())
        assertTrue(id.isRelative())
    }

    @Test()
    void "normalized: already normalized"() {
        def id = new ModuleID("foo/bar")
        def normalized = id.normalized()
        assertEquals(id.toString(), normalized.toString())
        assertEquals(id, normalized)
    }


    @Test()
    void "normalized: remove trailing 'js'"() {
        def id = new ModuleID("foo/bar.js")
        def normalized = id.normalized()
        assertEquals("foo/bar", normalized.toString())
    }

    @Test()
    void "normalized: collapse sequences of /"() {
        def id = new ModuleID("foo////bar///baz//a.js")
        def normalized = id.normalized()
        assertEquals("foo/bar/baz/a", normalized.toString())
    }

}
