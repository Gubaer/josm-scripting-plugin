var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var Node = org.openstreetmap.josm.data.osm.Node;
var LatLon = org.openstreetmap.josm.data.coor.LatLon;
var ArrayList = java.util.ArrayList;
var SimplePrimitiveId = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;


var expectError = function(f) {
	try {
		f();
		util.assert(false, "Expected an error, didn't get one.");
	} catch(e) {
		// OK 
	}
};

var expectAssertionError = function(f) {
	try {
		f();
		util.assert(false, "Expected an error, didn't get one.");
	} catch(e) {
		if (e.name != "AssertionError") {
			util.assert(false, "Expected AssertionError, got ", e.toSource());
		} 
	}
};

function newNode() {
	var n = new Node();
	n.setCoor(new LatLon(0,0));
	return n;
};

var suite = tu.suite(
		
	/* ------------------------------------------------------------------------------------ */
	/* add */
	/* ------------------------------------------------------------------------------------ */
		
	test("add - add a node", function() {
		var n = newNode();
		var ds = new DataSet();
		ds.add(n);
		util.assert(ds.getNodes().size() == 1, "Expected exactly one node in dataset, got {0}", ds.getNodes().size());		
	}),
	test("add - add two nodes", function() {
		var n1 = newNode();
		var n2 = newNode();
		var ds = new DataSet();
		ds.add(n1,n2);
		util.assert(ds.getNodes().size() == 2, "Expected exactly two nodes in dataset, got {0}", ds.getNodes().size());		
	}),
	test("add - add an array of nodes", function() {
		var n1 = newNode();
		var n2 = newNode();
		var ds = new DataSet();
		ds.add([n1,n2]);
		util.assert(ds.getNodes().size() == 2, "Expected exactly two nodes in dataset, got {0}", ds.getNodes().size());		
	}),
	test("add - add a list of nodes", function() {
		var n1 = newNode();
		var n2 = newNode();
		var l  = new ArrayList();
		l.add(n1); l.add(n2);
		var ds = new DataSet();
		ds.add(l);
		util.assert(ds.getNodes().size() == 2, "Expected exactly two nodes in dataset, got {0}", ds.getNodes().size());		
	}),
	test("add - add null (should be skipped)", function() {
		var ds = new DataSet();
		ds.add(null);
		util.assert(ds.getNodes().size() == 0, "Expected no nodes in dataset, got {0}", ds.getNodes().size());		
	}),
	test("add - add undefined (should be skipped)", function() {
		var ds = new DataSet();
		ds.add(undefined);
		util.assert(ds.getNodes().size() == 0, "Expected no nodes in dataset, got {0}", ds.getNodes().size());		
	}),
	test("add - add undefined (should be skipped)", function() {
		var ds = new DataSet();
		expectError(function() {
			ds.add("can't add a string");
		});
	}),
	
	/* ------------------------------------------------------------------------------------ */
	/* get */
	/* ------------------------------------------------------------------------------------ */

	test("get - get a node by id", function() {
		var n = new Node(1);
		n.setCoor(new LatLon(0,0));
		var ds = new DataSet();
		ds.add(n);
		var id = new SimplePrimitiveId(1, OsmPrimitiveType.NODE);
		var nn = ds.get(id);
		util.assert(util.isDef(nn), "Should have found the node");
	}),
	test("get - get a node by a node", function() {
		var n = new Node(1);
		n.setCoor(new LatLon(0,0));
		var ds = new DataSet();
		ds.add(n);
		var nn = ds.get(n);
		util.assert(util.isDef(nn), "Should have found the node");
	}),
	test("get - get by type and id", function() {
		var n = new Node(1);
		n.setCoor(new LatLon(0,0));
		var ds = new DataSet();
		ds.add(n);
		var nn = ds.get(OsmPrimitiveType.NODE, 1);
		util.assert(util.isDef(nn), "Should have found the node");
	}),
	test("get - get by type name and id", function() {
		var n = new Node(1);
		n.setCoor(new LatLon(0,0));
		var ds = new DataSet();
		ds.add(n);
		var nn = ds.get("node", 1);
		util.assert(util.isDef(nn), "Should have found the node");
	}),
	test("get - non existing primitive", function() {
		var n = new Node(1);
		n.setCoor(new LatLon(0,0));
		var ds = new DataSet();
		ds.add(n);
		var nn = ds.get("node", 1234);
		util.assert(nn == undefined, "Node shouldn't be defined");
	}),
	
	/* ------------------------------------------------------------------------------------ */
	/* remove  */
	/* ------------------------------------------------------------------------------------ */
	test("remove - remove a node by id", function() {
		var n = new Node(1);
		n.setCoor(new LatLon(0,0));
		var ds = new DataSet();
		ds.add(n);
		var id = new SimplePrimitiveId(1, OsmPrimitiveType.NODE);
		ds.remove(id);
		util.assert(ds.getNodes().size() == 0, "DataSet should be empty");
	}),
	test("remove - remove a node by a node", function() {
		var n = new Node(1);
		n.setCoor(new LatLon(0,0));
		var ds = new DataSet();
		ds.add(n);
		ds.remove(n);
		util.assert(ds.getNodes().size() == 0, "DataSet should be empty");
	}),
	test("remove - remove two nodes", function() {
		var n1 = newNode();
		var n2 = newNode();
		var ds = new DataSet();
		ds.add(n1,n2);
		ds.remove(n1,n2);
		util.assert(ds.getNodes().size() == 0, "dataset should be empty");		
	}),
	test("remove - remove an array of nodes", function() {
		var n1 = newNode();
		var n2 = newNode();
		var ds = new DataSet();
		ds.add([n1,n2]);
		ds.remove([n1,n2]);
		util.assert(ds.getNodes().size() == 0,  "dataset should be empty");		
	}),
	test("remove - remove a list of nodes", function() {
		var n1 = newNode();
		var n2 = newNode();
		var l  = new ArrayList();
		l.add(n1); l.add(n2);
		var ds = new DataSet();
		ds.add(l);
		ds.remove(l);
		util.assert(ds.getNodes().size() == 0, "dataset should be empty");		
	}),
	
	/* ------------------------------------------------------------------------------------ */
	/* nodeBuilder  */
	/* ------------------------------------------------------------------------------------ */
	test("nodeBuilder - create it", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		nb.local();
		util.assert(ds.getNodes().size() == 1, "Expected 1 node in dataset, got {0}", ds.getNodes().size());		
	}),
	
	/* ------------------------------------------------------------------------------------ */
	/* each  */
	/* ------------------------------------------------------------------------------------ */
	test("each - counting primitives", function() {
		var n1 = newNode();
		var n2 = newNode();
		var ds = new DataSet();
		ds.add(n1,n2);
		var count = 0;
		ds.each(function(p) {
			if (p instanceof Node) count++;
		});
		util.assert(count == 2);		
	})
);

suite.run();