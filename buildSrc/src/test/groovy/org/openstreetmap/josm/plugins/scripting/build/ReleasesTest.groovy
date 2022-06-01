package org.openstreetmap.josm.plugins.scripting.build

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test

class ReleasesTest extends GroovyTestCase{

    def configuration01 = """
releases:
  - label: v0.2.0
    minJosmVersion: 18427
    description: |
     major rewrite and cleanup, now supports GraalJS, support JPython plugins
     removed

  - label: 30796
    minJosmVersion: 14256
    description: update and add i18n ressources

  - label: 30787
    minJosmVersion: 14155
    description: "fix #71 Access to plugin class loader"
        """


    @Test
    void "can parse correct releases file"() {
        new Releases(configuration01)
    }

    @Test
    void "can find highest josm version"() {
        def releases = new Releases(configuration01)
        assertEquals("18427", releases.highestJosmVersion)
    }

    @Test
    void "can find current plugin version version"() {
        def releases = new Releases(configuration01)
        assertEquals("v0.2.0", releases.currentPluginLabel)
    }

    @Test
    void "can find highest plugin version for josm version"() {
        def releases = new Releases(configuration01)
        assertEquals("v0.2.0", releases.highestPluginLabelForJosmVersion(18427))
        assertEquals("30796", releases.highestPluginLabelForJosmVersion(14256))
    }

    @Test
    void "can build list of JOSM versions"() {
        def releases = new Releases(configuration01)
        assertEquals(3, releases.josmVersions.size())
        [18427, 14256, 14155].eachWithIndex {release, i ->
            assertEquals(release, releases.josmVersions[i] )
        }
    }
}
