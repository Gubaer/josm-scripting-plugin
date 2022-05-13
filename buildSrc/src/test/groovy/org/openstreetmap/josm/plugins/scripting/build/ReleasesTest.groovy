package org.openstreetmap.josm.plugins.scripting.build

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test

class ReleasesTest extends GroovyTestCase{

    def configuration01 = """
          releases = [

            [
               pluginVersion: "0.2.0",
               josmVersion: 14256,
             description: "update and add i18n ressources"
            ],
            [
                    pluginVersion: 30796,
                    josmVersion: 14256,
                    description: "update and add i18n ressources"
            ],
            [
                    pluginVersion: 30795,
                    josmVersion: 14000,
                    description: "merge #80: Add new 'add to toolbar' checkbox"
            ]
        """

    def configuration02 = """
          releases = [

            [
                    pluginVersion: 30796,
                    josmVersion: 14256,
                    description: "update and add i18n ressources"
            ],
            [
                    pluginVersion: 30795,
                    josmVersion: 14256,
                    description: "merge #80: Add new 'add to toolbar' checkbox"
            ]
        """

    @Test
    def "can parse correct releases file"() {
        new Releases(configuration01)
    }

    @Test
    def "can find highest josm version"() {
        def releases = new Releases(configuration01)
        assertEquals("14256", releases.highestJosmVersion())
    }

    @Test
    def "can find current plugin version version"() {
        def releases = new Releases(configuration01)
        assertEquals("0.2.0", releases.currentPluginVersion())

        releases = new Releases(configuration02)
        assertEquals("30796", releases.currentPluginVersion())
    }

    @Test
    def "can find highest plugin version for josm version"() {
        def releases = new Releases(configuration01)
        assertEquals("0.2.0", releases.highestPluginVersionForJosmVersion(14256))
        assertEquals("30795", releases.highestPluginVersionForJosmVersion(14000))
    }

    @Test
    def "can build list of JOSM versions"() {
        def releases = new Releases(configuration01)
        assertEquals([14256,14000], releases.josmVersions)
    }
}
