package org.openstreetmap.josm.plugins.scripting.rhino.api


import org.junit.jupiter.api.Test
import org.openstreetmap.josm.data.coor.LatLon
import org.openstreetmap.josm.data.osm.DataSet
import org.openstreetmap.josm.data.osm.Node
import org.openstreetmap.josm.data.osm.Relation
import org.openstreetmap.josm.data.osm.Way
import org.openstreetmap.josm.gui.layer.OsmDataLayer
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.js.api.DeleteMultiCommand

class DeleteMultiCommandTest extends JOSMFixtureBasedTest {

    static OsmDataLayer newLayer() {
        return new OsmDataLayer(new DataSet(), null, null)
    }

    @Test
    void testCreate() {
        def n1 = new Node(new LatLon(0,0))
        def n2 = new Node(new LatLon(0,0))
        def cmd = new DeleteMultiCommand(newLayer(), [n1, n2])
        assert cmd

        shouldFail(NullPointerException) {
            cmd = new DeleteMultiCommand(null, [n1,n2])
        }

        shouldFail(NullPointerException) {
            cmd = new DeleteMultiCommand(newLayer(), null)
        }
    }

    @Test
    void testExecuteAndUndo() {
        def n1 = new Node(new LatLon(0,0))
        def n2 = new Node(new LatLon(0,0))
        def r1 = new Relation()
        def w1  = new Way(1)

        OsmDataLayer layer = newLayer()
        layer.data.addPrimitive(n1)
        layer.data.addPrimitive(n2)
        layer.data.addPrimitive(w1)
        layer.data.addPrimitive(r1)
        r1.setDeleted(true)

        def cmd = new DeleteMultiCommand(layer, [n1,w1,n2,r1])
        assert cmd

        // execute
        def result = cmd.executeCommand()
        assert result, "should be true"
        DataSet ds = layer.data
        assert ds.allPrimitives().size() == 4
        assert ds.getPrimitiveById(n1).isDeleted()
        assert ds.getPrimitiveById(n2).isDeleted()
        assert ds.getPrimitiveById(w1).isDeleted()
        assert ds.getPrimitiveById(r1).isDeleted()

        // undo
        cmd.undoCommand()
        assert ds.allPrimitives().size() == 4
        assert !ds.getPrimitiveById(n1).isDeleted()
        assert !ds.getPrimitiveById(n2).isDeleted()
        assert !ds.getPrimitiveById(w1).isDeleted()
        // was deleted before, should still be deleted
        assert ds.getPrimitiveById(r1).isDeleted()

        // redo
        result = cmd.executeCommand()
        assert result, "should be true"
        assert ds.allPrimitives().size() == 4
        assert ds.getPrimitiveById(n1).isDeleted()
        assert ds.getPrimitiveById(n2).isDeleted()
        assert ds.getPrimitiveById(w1).isDeleted()
        assert ds.getPrimitiveById(r1).isDeleted()
    }
}
