package org.openstreetmap.josm.plugins.scripting.build

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test
class SemanticVersionTest extends GroovyTestCase {

    @Test
    void "OK case - should create valid version"() {
        new SemanticVersion("1.2.3")
    }

    @Test
    void "NOK - illegal versions should fail"() {
        shouldFail(IllegalSemanticVersion) {
            new SemanticVersion("1.2")
        }

        shouldFail(IllegalSemanticVersion) {
            new SemanticVersion("1")
        }

        shouldFail(IllegalSemanticVersion) {
            new SemanticVersion("1.2.3.4")
        }

        shouldFail(IllegalSemanticVersion) {
            new SemanticVersion("foo.2.3")
        }
        shouldFail(IllegalSemanticVersion) {
            new SemanticVersion("123")
        }
    }

    @Test
    void "OK - should create from a label"() {
        def version = SemanticVersion.fromLabel("v1.2.3")
        assertEquals("1.2.3", version.toString())

        version = SemanticVersion.fromLabel("1.2.3")
        assertEquals("1.2.3", version.toString())
    }

    @Test
    void "NOK - illegal labels"() {
        shouldFail(IllegalSemanticVersion) {
            SemanticVersion.fromLabel("vfoobar")
        }
    }
}
