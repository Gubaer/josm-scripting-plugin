package org.openstreetmap.josm.plugins.scripting.graalvm.api

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory
import org.openstreetmap.josm.plugins.scripting.graalvm.IGraalVMFacade

class DataSetInputOutputTest extends AbstractGraalVMBasedTest{
    private IGraalVMFacade facade

    @BeforeEach
    void initGraalVMFacade() {
        facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
    }

    @AfterEach
    void resetGraalVMFacade() {
        if (facade != null) {
            facade.resetContext()
        }
    }

    @Test
    void "can load an osm file - default options"() {
        final file = new File(
            getProjectHome(),
            "src/test/resources/sample-data-files/test-josm-open.osm")

        final src = """
        const josm = require('josm')
        const {DataSetUtil} = require('josm/ds')
        
        DataSetUtil.load('$file.absolutePath')
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
        const file = new File('$file.absolutePath')
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
        
        DataSetUtil.load('$file.absolutePath', {format: 'osm'})
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
        
        DataSetUtil.load('$file.absolutePath', {format: 'osm.gz'})
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
        
        const loadedData = DataSetUtil.load('$file.absolutePath')
        loadedData.save('$tempOutputFile.absolutePath')
        const loadedAgain = DataSetUtil.load('$tempOutputFile.absolutePath', {format: 'osm'})
        """
        facade.eval(graalJSDescriptor, src)
    }

}
