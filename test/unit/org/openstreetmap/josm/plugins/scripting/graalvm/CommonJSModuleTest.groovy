package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.Test
import static org.junit.Assert.assertEquals

//TODO(karl): not necessary? delete?
class CommonJSModuleTest {

    @Test
    void "should create with module with valid id"() {
        def module = new CommonJSModule("josm/console")
        assertEquals("josm/console", module.id)
    }

    @Test(expected = NullPointerException)
    void "should reject module with null id"() {
        new CommonJSModule(null)
    }

    @Test(expected = IllegalArgumentException)
    void "should reject empty id"() {
        new CommonJSModule("   ")
    }

}
