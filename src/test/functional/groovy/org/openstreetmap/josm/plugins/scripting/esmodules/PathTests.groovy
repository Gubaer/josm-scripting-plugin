package org.openstreetmap.josm.plugins.scripting.esmodules

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test
import java.nio.file.Path

class PathTests extends GroovyTestCase {

    @Test
    void "test subpaths"(){
        final path = Path.of("/foo/bar/baz/file.js")
        println(path.subpath(2, 4).toString())
    }
}
