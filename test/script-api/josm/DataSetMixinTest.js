var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var nb = require("josm/builder").NodeBuilder;
var wb = require("josm/builder").WayBuilder;
var rb = require("josm/builder").RelationBuilder;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
var SimplePrimitiveId = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
var HashSet = java.util.HashSet;
var ArrayList = java.util.ArrayList;

tu.suite("adding objects",
	test("add a node", function() {
		var ds = new DataSet();
		var n = nb.create();
		ds.add(n);		
		util.assert(ds.has(n), "should be in ds");
		util.assert(n.dataSet == ds, "should have dataset");
	}),
	test("add a way ", function() {
		var ds = new DataSet();
		var w = wb.create();
		ds.add(w);
		util.assert(ds.has(w), "should be in ds");
		util.assert(w.dataSet == ds, "should have dataset");
	}),
	test("add null - should be skipped ", function() {
		var ds = new DataSet();
		ds.add(null);
	}),
	test("add undefined - should be skipped ", function() {
		var ds = new DataSet();
		ds.add(undefined);
	}),
	test("add a relation ", function() {
		var ds = new DataSet();
		var r = rb.create();
		ds.add(r);
		util.assert(ds.has(r), "should be in ds");
		util.assert(r.dataSet == ds, "should have dataset");
	}),
	test("add an array of objects", function() {
		var ds = new DataSet();
		var n = nb.create();
		var w = wb.create();
		var r = rb.create();
		ds.add([n,w,r]);
		util.assert(ds.has(n), "1 - should be in ds");
		util.assert(ds.has(w), "2 - should be in ds");
		util.assert(ds.has(r), "3 - should be in ds");
	}),
	test("add an array of objects, including null and undefined", function() {
		var ds = new DataSet();
		var n = nb.create();
		var w = wb.create();
		var r = rb.create();
		ds.add([n,null,w,undefined,r]);
		util.assert(ds.has(n), "1 - should be in ds");
		util.assert(ds.has(w), "2 - should be in ds");
		util.assert(ds.has(r), "3 - should be in ds");
	}),

	test("add a set  of objects", function() {
		var ds = new DataSet();
		var n = nb.create();
		var w = wb.create();
		var r = rb.create();
		var set = new HashSet();
		set.add(n); set.add(w); set.add(r);
		ds.add(set);
		util.assert(ds.has(n), "1 - should be in ds");
		util.assert(ds.has(w), "2 - should be in ds");
		util.assert(ds.has(r), "3 - should be in ds");
	}),
	test("add a list  of objects", function() {
		var ds = new DataSet();
		var n = nb.create();
		var w = wb.create();
		var r = rb.create();
		var set = new ArrayList();
		set.add(n); set.add(w); set.add(r);
		ds.add(set);
		util.assert(ds.has(n), "1 - should be in ds");
		util.assert(ds.has(w), "2 - should be in ds");
		util.assert(ds.has(r), "3 - should be in ds");
	}),
	test("add with builder", function() {
		var ds = new DataSet();
		var n = ds.nodeBuilder.create();
		var w = ds.wayBuilder.create();
		var r = ds.relationBuilder.create();
		util.assert(ds.has(n), "1 - should be in ds");
		util.assert(ds.has(w), "2 - should be in ds");
		util.assert(ds.has(r), "3 - should be in ds");
	})
	
).run();

tu.suite("getting objects",
		
	// --- nodes
	test("get a node - id, 'node'", function() {
		var ds = new DataSet();
		ds.nodeBuilder.create(1234);
		var n = ds.get(1234, "node");
		util.assert(n, "1 - should have found object");
	}),
	test("get a node - id, OsmPrimitiveType.NODE", function() {
		var ds = new DataSet();
		ds.nodeBuilder.create(1234);
		var n = ds.get(1234, OsmPrimitiveType.NODE);
		util.assert(n, "1 - should have found object");
	}),
	test("get a node - simpleprimitiveid", function() {
		var ds = new DataSet();
		ds.nodeBuilder.create(1234);
		var id = new SimplePrimitiveId(1234, OsmPrimitiveType.NODE);
		var n = ds.get(id);
		util.assert(n, "1 - should have found object");
	}),
	test("get a node - id object - 1", function() {
		var ds = new DataSet();
		ds.nodeBuilder.create(1234);
		var id = {id: 1234, type: "node"};
		var n = ds.get(id);
		util.assert(n, "1 - should have found object");
	}),
	test("get a node - id object - 2", function() {
		var ds = new DataSet();
		ds.nodeBuilder.create(1234);
		var id = {id: 1234, type:  OsmPrimitiveType.NODE};
		var n = ds.get(id);
		util.assert(n, "2 - should have found object");
	}),
	
	// --- ways	
	test("get a way - id, 'way'", function() {
		var ds = new DataSet();
		ds.wayBuilder.create(1234);
		var w = ds.get(1234, "way");
		util.assert(w, "1 - should have found object");
	}),
	test("get a way - id, OsmPrimitiveType.WAY", function() {
		var ds = new DataSet();
		ds.wayBuilder.create(1234);
		var w = ds.get(1234, OsmPrimitiveType.WAY);
		util.assert(w, "1 - should have found object");
	}),
	test("get a way - simpleprimitiveid", function() {
		var ds = new DataSet();
		ds.wayBuilder.create(1234);
		var id = new SimplePrimitiveId(1234, OsmPrimitiveType.WAY);
		var w = ds.get(id);
		util.assert(w, "1 - should have found object");
	}),
	test("get a way - id object - 1", function() {
		var ds = new DataSet();
		ds.wayBuilder.create(1234);
		var id = {id: 1234, type: "way"};
		var w = ds.get(id);
		util.assert(w, "1 - should have found object");
	}),
	test("get a way - id object - 2", function() {
		var ds = new DataSet();
		ds.wayBuilder.create(1234);
		var id = {id: 1234, type:  OsmPrimitiveType.WAY};
		var w = ds.get(id);
		util.assert(w, "2 - should have found object");
	}),
	
	// --- relations	
	test("get a relation - id, 'relation'", function() {
		var ds = new DataSet();
		ds.relationBuilder.create(1234);
		var r = ds.get(1234, "relation");
		util.assert(r, "1 - should have found object");
	}),
	test("get a relation - id, OsmPrimitiveType.RELATION", function() {
		var ds = new DataSet();
		ds.relationBuilder.create(1234);
		var r = ds.get(1234, OsmPrimitiveType.RELATION);
		util.assert(r, "1 - should have found object");
	}),
	test("get a relation - simpleprimitiveid", function() {
		var ds = new DataSet();
		ds.relationBuilder.create(1234);
		var id = new SimplePrimitiveId(1234, OsmPrimitiveType.RELATION);
		var r = ds.get(id);
		util.assert(r, "1 - should have found object");
	}),
	test("get a relation - id object - 1", function() {
		var ds = new DataSet();
		ds.relationBuilder.create(1234);
		var id = {id: 1234, type: "relation"};
		var r = ds.get(id);
		util.assert(r, "1 - should have found object");
	}),
	test("get a relation - id object - 2", function() {
		var ds = new DataSet();
		ds.relationBuilder.create(1234);
		var id = {id: 1234, type:  OsmPrimitiveType.RELATION};
		var r = ds.get(id);
		util.assert(r, "2 - should have found object");
	}),
	
	test("get null - should fail", function() {
		var ds = new DataSet();
		ds.relationBuilder.create(1234);
		tu.expectAssertionError("get null - should fail", function() {
			ds.get(null);
		});
	}),
	test("get undefined - should fail", function() {
		var ds = new DataSet();
		ds.relationBuilder.create(1234);
		tu.expectAssertionError("get undefined - should fail", function() {
			ds.get(undefined);
		});
	}),
	test("illegal numeric id - should fail", function() {
		var ds = new DataSet();
		ds.relationBuilder.create(1234);
		tu.expectAssertionError("get - null id - should fail", function() {
			ds.get(null, "node");
		});
		tu.expectAssertionError("get - undefined id - should fail", function() {
			ds.get(undefined, "node");
		});
		tu.expectAssertionError("get - 0 id - should fail", function() {
			ds.get(0, "node");
		});
		tu.expectAssertionError("get -id as string - should fail", function() {
			ds.get("1234", "node");
		});
	}),
	test("illegal type - should fail", function() {
		var ds = new DataSet();
		ds.relationBuilder.create(1234);
		tu.expectAssertionError("get - null type - should fail", function() {
			ds.get(1234, null);
		});
		tu.expectAssertionError("get - undefined type - should fail", function() {
			ds.get(1234, undefined);
		});
		tu.expectAssertionError("get - unknown string type - should fail", function() {
			ds.get(1234, "nosuchtype");
		});
		tu.expectAssertionError("get - illegal OsmPrimitiveType - should fail", function() {
			ds.get(1234, OsmPrimitiveType.CLOSEDWAY);
		});
	}),
	test("id object - error cases", function() {
		var ds = new DataSet();
		ds.relationBuilder.create(1234);
		tu.expectAssertionError("missing id", function() {
			ds.get({type: "node"});
		});
		tu.expectAssertionError("null id", function() {
			ds.get({id: null, type: "node"});
		});
		tu.expectAssertionError("undefined id", function() {
			ds.get({id: undefined, type: "node"});
		});
		tu.expectAssertionError("0 id", function() {
			ds.get({id: 0, type: "node"});
		});
		tu.expectAssertionError("missing type", function() {
			ds.get({id: 1234});
		});
		tu.expectAssertionError("null type", function() {
			ds.get({id: 1234, type: null});
		});
		tu.expectAssertionError("undefined type", function() {
			ds.get({id: 1234, type: undefined});
		});
		tu.expectAssertionError("illegal string type", function() {
			ds.get({id: 1234, type: "nosuchtype"});
		});
		tu.expectAssertionError("illegal OsmPrimitiveType", function() {
			ds.get({id: 1234, type: OsmPrimitiveType.CLOSEDWAY});
		});
	})
).run();