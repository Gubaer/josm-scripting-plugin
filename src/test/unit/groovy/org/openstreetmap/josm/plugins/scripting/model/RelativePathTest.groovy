package org.openstreetmap.josm.plugins.scripting.model

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest

import static groovy.test.GroovyAssert.shouldFail

class RelativePathTest extends JOSMFixtureBasedTest {

    @Test
    void "of - accept null argument as 'create an empty path'"() {
        def path = new RelativePath(null)
    }

    @Test
    void "of - don't accept an absolute file"() {
        shouldFail(IllegalArgumentException) {
            def segments = RelativePath.of(getProjectHome())
        }
    }
}
