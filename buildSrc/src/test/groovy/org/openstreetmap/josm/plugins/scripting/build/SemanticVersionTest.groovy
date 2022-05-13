package org.openstreetmap.josm.plugins.scripting.build

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test
class SemanticVersionTest extends GroovyTestCase {

    @Test
    def "OK case - should create valid version"() {
        new SemanticVersion("1.2.3")
    }

    @Test
    def "NOK - illegal versions should fail"() {
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
}
