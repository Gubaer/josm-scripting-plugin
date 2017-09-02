package org.openstreetmap.josm.plugins.scripting.js.api;

import static org.junit.Assert.*;
import org.junit.*;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation
import org.openstreetmap.josm.data.osm.Way
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.js.api.AddMultiCommand;

class ChangeMultiCommandTest {

    static def JOSMFixture fixture;

    @BeforeClass
    public static void init() {
        fixture = JOSMFixture.createUnitTestFixture();
        fixture.init();
    }

    def shouldFail = new GroovyTestCase().&shouldFail;

    def OsmDataLayer newLayer() {
        return new OsmDataLayer(new DataSet(), null, null);
    }

    @Test
    public void testCreate() {
        def n1 = new Node(new LatLon(0,0));
        def n2 = new Node(new LatLon(0,0));
        def change = new Change();
        change.withLatChange(12.22);
        def cmd = new ChangeMultiCommand(newLayer(), [n1,n2], change);
        assert cmd

        shouldFail(IllegalArgumentException) {
            cmd = new ChangeMultiCommand(null, [n1,n2], change);
        }

        shouldFail(IllegalArgumentException) {
            cmd = new ChangeMultiCommand(newLayer(), null, change);
        }
        shouldFail(IllegalArgumentException) {
            cmd = new ChangeMultiCommand(newLayer(), [n1,n2], null);
        }

    }

    @Test
    public void testExecuteAndUndo() {
        def n1 = new Node(new LatLon(0,0));
        def n2 = new Node(new LatLon(0,0));
        def w1  = new Way(1,1);
        w1.setNodes([n2,n2]);
        def r1 = new Relation(1,1);
        r1.put("name", "oldname");

        def OsmDataLayer layer = newLayer();
        layer.data.addPrimitive(n1);
        layer.data.addPrimitive(n2);
        layer.data.addPrimitive(w1);
        layer.data.addPrimitive(r1);

        def change = new Change().withLatChange(11.11).withTagsChange([name: "newvalue"]);
        def cmd = new ChangeMultiCommand(layer, [n1,w1,r1], change);
        assert cmd

        // execute
        def result = cmd.executeCommand();
        assert result, "should be true"

        assert n1.getCoor().lat() == 11.11;
        assert n2.getCoor().lat() == 0;
        assert n1.get("name") == "newvalue";
        assert w1.get("name") == "newvalue";
        assert r1.get("name") == "newvalue";

                // undo
        cmd.undoCommand();
        assert n1.getCoor().lat() == 0;
        assert n2.getCoor().lat() == 0;
        assert !n1.hasKey("name")
        assert !w1.hasKey("name")
        assert r1.get("name") == "oldname";

        // redo
        result = cmd.executeCommand();
        assert result, "should be true"

        assert n1.getCoor().lat() == 11.11;
        assert n2.getCoor().lat() == 0;
        assert n1.get("name") == "newvalue";
        assert w1.get("name") == "newvalue";
        assert r1.get("name") == "newvalue";
    }
}
