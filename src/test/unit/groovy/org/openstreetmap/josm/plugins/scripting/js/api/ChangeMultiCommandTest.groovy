package org.openstreetmap.josm.plugins.scripting.js.api

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.openstreetmap.josm.data.coor.LatLon
import org.openstreetmap.josm.data.osm.DataSet
import org.openstreetmap.josm.data.osm.Node
import org.openstreetmap.josm.data.osm.Relation
import org.openstreetmap.josm.data.osm.Way
import org.openstreetmap.josm.gui.layer.OsmDataLayer
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture

class ChangeMultiCommandTest extends GroovyTestCase {

    static JOSMFixture fixture

    @BeforeAll
    static void init() {
       fixture = new JOSMFixture(false)
    }

    static OsmDataLayer newLayer() {
        return new OsmDataLayer(new DataSet(), null, null)
    }

    @Test
    void testCreate() {
        def n1 = new Node(new LatLon(0,0))
        def n2 = new Node(new LatLon(0,0))
        def change = new Change()
        change.withLatChange(12.22)
        def cmd = new ChangeMultiCommand(newLayer(), [n1,n2], change)
        assert cmd

        shouldFail(NullPointerException) {
            cmd = new ChangeMultiCommand(null, [n1,n2], change)
        }

        shouldFail(NullPointerException) {
            cmd = new ChangeMultiCommand(newLayer(), null, change)
        }
        shouldFail(NullPointerException) {
            cmd = new ChangeMultiCommand(newLayer(), [n1,n2], null)
        }

    }

    @Test
    void testExecuteAndUndo() {
        def n1 = new Node(new LatLon(0,0))
        def n2 = new Node(new LatLon(0,0))
        def w1  = new Way(1,1)
        w1.setNodes([n2,n2])
        def r1 = new Relation(1,1)
        r1.put("name", "oldname")

        OsmDataLayer layer = newLayer()
        layer.data.addPrimitive(n1)
        layer.data.addPrimitive(n2)
        layer.data.addPrimitive(w1)
        layer.data.addPrimitive(r1)

        def change = new Change().withLatChange(11.11).withTagsChange([name: "newvalue"])
        def cmd = new ChangeMultiCommand(layer, [n1,w1,r1], change)
        assert cmd

        // execute
        def result = cmd.executeCommand()
        assert result, "should be true"

        assert n1.getCoor().lat() == 11.11d
        assert n2.getCoor().lat() == 0
        assert n1.get("name") == "newvalue"
        assert w1.get("name") == "newvalue"
        assert r1.get("name") == "newvalue"

        // undo
        cmd.undoCommand()
        assert n1.getCoor().lat() == 0
        assert n2.getCoor().lat() == 0
        assert !n1.hasKey("name")
        assert !w1.hasKey("name")
        assert r1.get("name") == "oldname"

        // redo
        result = cmd.executeCommand()
        assert result, "should be true"

        assert n1.getCoor().lat() == 11.11d
        assert n2.getCoor().lat() == 0
        assert n1.get("name") == "newvalue"
        assert w1.get("name") == "newvalue"
        assert r1.get("name") == "newvalue"
    }
}
