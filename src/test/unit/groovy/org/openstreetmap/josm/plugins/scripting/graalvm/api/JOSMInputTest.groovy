package org.openstreetmap.josm.plugins.scripting.graalvm.api


import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest

class JOSMInputTest extends AbstractGraalVMBasedTest {

    @Test
    void "can load _DOT_osm file"() {
        final file = new File(
            getProjectHome(),
            "src/test/resources/sample-data-files/test-josm-open.osm")

        final src = """
        const josm = require('josm')
        josm.open('${escapeWindowsPathDelimiter(file)}')
        """
        facade.eval(graalJSDescriptor, src)
    }

    @Test
    void "can load _DOT_gpx file"() {
        final file = new File(
            getProjectHome(),
            "src/test/resources/sample-data-files/test-josm-open.gpx")

        final src = """
        const josm = require('josm')
        josm.open('${escapeWindowsPathDelimiter(file)}')
        """
        facade.eval(graalJSDescriptor, src)
    }

    @Test
    void "can load _DOT_osm_DOT_gz file"() {
        final file = new File(
            getProjectHome(),
            "src/test/resources/sample-data-files/test-datasetutil-load.osm.gz")

        final src = """
        const josm = require('josm')
        josm.open('${escapeWindowsPathDelimiter(file)}')
        """
        facade.eval(graalJSDescriptor, src)
    }

    @Test
    void "can load _DOT_osc file"() {
        final file = new File(
            getProjectHome(),
            "src/test/resources/sample-data-files/test-datasetutil-load.osc")

        final src = """
        const josm = require('josm')
        josm.open('${escapeWindowsPathDelimiter(file)}')
        """
        facade.eval(graalJSDescriptor, src)
    }

    @Test
    void "can load multiple files"() {
        final files = [
            "src/test/resources/sample-data-files/test-datasetutil-load.osc",
            "src/test/resources/sample-data-files/test-josm-open.gpx",
            "src/test/resources/sample-data-files/test-josm-open.osm"
        ]
        .collect {new File(getProjectHome(), it)}
        .collect {escapeWindowsPathDelimiter(it)}

        final src = """
        const josm = require('josm')
        josm.open(
            '${files[0]}', 
            '${files[1]}', 
            '${files[2]}')
        """
        facade.eval(graalJSDescriptor, src)
    }

    @Test
    void "can open a file with a File object"() {
        final file = new File(
            getProjectHome(),
            "src/test/resources/sample-data-files/test-josm-open.osm")

        final src = """
        const josm = require('josm')
        const File = Java.type('java.io.File')
        const file = new File('${escapeWindowsPathDelimiter(file)}')
        josm.open(file)
        """
        facade.eval(graalJSDescriptor, src)
    }
}
