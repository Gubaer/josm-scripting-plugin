package org.openstreetmap.josm.plugins.scripting.js.api;

import static org.junit.Assert.*;
import org.junit.*;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.js.api.AddMultiCommand;

class AddMultiCommandTest {

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
		def cmd = new AddMultiCommand(newLayer(), [n1,n2]);
		assert cmd 		
		
		shouldFail(IllegalArgumentException) {
			cmd = new AddMultiCommand(null, [n1,n2]);
		}
		
		shouldFail(IllegalArgumentException) {
			cmd = new AddMultiCommand(newLayer(), null);
		}
	}
	
	@Test
	public void testExecuteAndUndo() {
		def n1 = new Node(new LatLon(0,0));
		def n2 = new Node(new LatLon(0,0));
		def w1  = new Way(1);
		w1.setNodes([n2,n2]);
		
		def OsmDataLayer layer = newLayer();
		def cmd = new AddMultiCommand(layer, [n1,w1,n2]);
		assert cmd
		
		// execute 
		def result = cmd.executeCommand();
		assert result, "should be true"
		def DataSet ds = layer.data;
		assert ds.allPrimitives().size() == 3;
		
		// undo
		cmd.undoCommand();
		assert ds.allPrimitives().size() == 0;
		
		// redo
		result = cmd.executeCommand();
		assert result, "should be true"
		assert ds.allPrimitives().size() == 3;
	}
}
