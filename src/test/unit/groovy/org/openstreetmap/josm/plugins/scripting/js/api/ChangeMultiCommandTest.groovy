package org.openstreetmap.josm.plugins.scripting.js.api


import org.junit.jupiter.api.Test
import org.openstreetmap.josm.data.coor.LatLon
import org.openstreetmap.josm.data.osm.DataSet
import org.openstreetmap.josm.data.osm.Node
import org.openstreetmap.josm.data.osm.Relation
import org.openstreetmap.josm.data.osm.Way
import org.openstreetmap.josm.gui.layer.OsmDataLayer
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest

class ChangeMultiCommandTest extends JOSMFixtureBasedTest {

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
        assertNotNull(cmd)

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
        assertNotNull(cmd)

        // execute
        def result = cmd.executeCommand()
        assertTrue(result)

        assertEquals(11.11d, n1.getCoor().lat())
        assertEquals(0, n2.getCoor().lat())
        assertEquals("newvalue", n1.get("name"))
        assertEquals("newvalue", w1.get("name"))
        assertEquals("newvalue", r1.get("name"))

        // undo
        cmd.undoCommand()
        assertEquals(0, n1.getCoor().lat())
        assertEquals(0, n2.getCoor().lat())
        assertTrue(!n1.hasKey("name"))
        assertTrue( !w1.hasKey("name"))
        assertEquals("oldname", r1.get("name"))

        // redo
        result = cmd.executeCommand()
        assertTrue(result)

        assertEquals(11.11d, n1.getCoor().lat())
        assertEquals(0, n2.getCoor().lat())
        assertEquals("newvalue", n1.get("name"))
        assertEquals("newvalue", w1.get("name"))
        assertEquals("newvalue", r1.get("name"))
    }
}
