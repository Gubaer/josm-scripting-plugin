package org.openstreetmap.josm.plugins.scripting.graalvm.api

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest

import static groovy.test.GroovyAssert.shouldFail

class DataSetInputOutputTest extends AbstractGraalVMBasedTest{

    @Test
    void "can load an osm file - default options"() {
        final file = new File(
            getProjectHome(),
            "src/test/resources/sample-data-files/test-josm-open.osm")

        final src = """
        const josm = require('josm')
        const {DataSetUtil} = require('josm/ds')
        
        DataSetUtil.load('${escapeWindowsPathDelimiter(file)}')
        """
        facade.eval(graalJSDescriptor, src)
    }

    @Test
    void "can load an osm file, with File object - default options"() {
        final file = new File(
                getProjectHome(),
                "src/test/resources/sample-data-files/test-josm-open.osm")

        final src = """
        const josm = require('josm')
        const {DataSetUtil} = require('josm/ds')
        const File = Java.type('java.io.File')
        const file = new File('${escapeWindowsPathDelimiter(file)}')
        DataSetUtil.load(file)
        """
        facade.eval(graalJSDescriptor, src)
    }

    @Test
    void "can load an osm file - with format option"() {
        final file = new File(
                getProjectHome(),
                "src/test/resources/sample-data-files/test-josm-open.osm")

        final src = """
        const josm = require('josm')
        const {DataSetUtil} = require('josm/ds')
        
        DataSetUtil.load('${escapeWindowsPathDelimiter(file)}', {format: 'osm'})
        """
        facade.eval(graalJSDescriptor, src)
    }

    @Test
    void "should fail if format isn't matching with content"() {
        final file = new File(
            getProjectHome(),
            "src/test/resources/sample-data-files/test-josm-open.osm")

        final src = """
        const josm = require('josm')
        const {DataSetUtil} = require('josm/ds')
        
        DataSetUtil.load('${escapeWindowsPathDelimiter(file)}', {format: 'osm.gz'})
        """
        shouldFail(Throwable) {
            facade.eval(graalJSDescriptor, src)
        }
    }

    @Test
    void "can load a file, save it, and load it again"() {
        final file = new File(
            getProjectHome(),
            "src/test/resources/sample-data-files/test-josm-open.osm")
        final tempOutputFile = File.createTempFile("test-josm-open", ".tmp")

        final src = """
        const josm = require('josm')
        const {DataSetUtil} = require('josm/ds')
        
        const loadedData = DataSetUtil.load('${escapeWindowsPathDelimiter(file)}')
        loadedData.save('${escapeWindowsPathDelimiter(tempOutputFile)}')
        const loadedAgain = DataSetUtil.load('${escapeWindowsPathDelimiter(tempOutputFile)}', {format: 'osm'})
        """
        facade.eval(graalJSDescriptor, src)
    }
}
