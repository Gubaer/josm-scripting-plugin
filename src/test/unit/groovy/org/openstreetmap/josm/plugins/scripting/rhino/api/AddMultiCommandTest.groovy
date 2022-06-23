package org.openstreetmap.josm.plugins.scripting.rhino.api


import org.junit.jupiter.api.Test
import org.openstreetmap.josm.data.coor.LatLon
import org.openstreetmap.josm.data.osm.DataSet
import org.openstreetmap.josm.data.osm.Node
import org.openstreetmap.josm.data.osm.Way
import org.openstreetmap.josm.gui.layer.OsmDataLayer
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.js.api.AddMultiCommand

class AddMultiCommandTest extends JOSMFixtureBasedTest {

    static OsmDataLayer newLayer() {
        return new OsmDataLayer(new DataSet(), null, null)
    }

    @Test
    void testCreate() {
        def n1 = new Node(new LatLon(0,0))
        def n2 = new Node(new LatLon(0,0))
        def cmd = new AddMultiCommand(newLayer(), [n1, n2])
        assertNotNull(cmd)

        shouldFail(NullPointerException) {
            cmd = new AddMultiCommand(null, [n1,n2])
        }

        shouldFail(NullPointerException) {
            cmd = new AddMultiCommand(newLayer(), null)
        }
    }

    @Test
    void testExecuteAndUndo() {
        def n1 = new Node(new LatLon(0,0))
        def n2 = new Node(new LatLon(0,0))
        def w1  = new Way(1)
        w1.setNodes([n2,n2])

        OsmDataLayer layer = newLayer()
        def cmd = new AddMultiCommand(layer, [n1,w1,n2])
        assertNotNull(cmd)

        // execute
        def result = cmd.executeCommand()
        assertTrue(result)
        DataSet ds = layer.data
        assertEquals(3, ds.allPrimitives().size())

        // undo
        cmd.undoCommand()
        assertEquals(0, ds.allPrimitives().size())

        // redo
        result = cmd.executeCommand()
        assertTrue(result)
        assertEquals(3, ds.allPrimitives().size())
    }
}
