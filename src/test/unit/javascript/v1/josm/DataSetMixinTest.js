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

var suites = [];

suites.push(tu.suite("adding objects",
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
	
));

suites.push(tu.suite("getting objects",
		
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
));


suites.push(tu.suite("removing objects",	
	// --- nodes
	test("remove a node - id, 'node'", function() {
		var ds = new DataSet();
		ds.nodeBuilder.create(1234);
		ds.remove(1234,"node");
		util.assert(!ds.has(1234,"node"), "1 - should not have object");
	}),
	test("remove a node - {id:..., type: ...}", function() {
		var ds = new DataSet();
		ds.nodeBuilder.create(1234);
		ds.remove({id: 1234, type: "node"});
		util.assert(!ds.has(1234,"node"), "1 - should not have object");
	}),
	test("remove a node - PrimitiveId", function() {
		var ds = new DataSet();
		ds.nodeBuilder.create(1234);
		ds.remove(new SimplePrimitiveId(1234, OsmPrimitiveType.NODE));
		util.assert(!ds.has(1234,"node"), "1 - should not have object");
	}),
	test("remove a node - multiple ids", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		ds.remove(n1,n2);
		util.assert(!ds.has(n1), "1 - should not have object");
		util.assert(!ds.has(n2), "2 - should not have object");
	}),
	test("remove a node - arrays of ids", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		ds.remove([n1,n2]);
		util.assert(!ds.has(n1), "1 - should not have object");
		util.assert(!ds.has(n2), "2 - should not have object");
	}),
	test("remove a node - list of ids", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var list = new ArrayList();
		list.add(n1); list.add(n2);
		ds.remove(list);
		util.assert(!ds.has(n1), "1 - should not have object");
		util.assert(!ds.has(n2), "2 - should not have object");
	}),
	test("remove  null - should be skipped", function() {
		var ds = new DataSet();
		ds.remove(null);
	}),
	test("remove  undefined - should be skipped", function() {
		var ds = new DataSet();
		ds.remove(undefined);
	}),
	test("remove  list with null or undefined - should be skipped", function() {
		var ds = new DataSet();
		ds.remove([null, undefined]);
	})
));


suites.push(tu.suite("selection",	
	test("get selection object", function() {
		var ds = new DataSet();
		var sel = ds.selection;
		util.assert(!!sel, "should be defined");			
	}),
	
	test("add - id", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		ds.selection.add(n1);			
		util.assert(ds.selection.isSelected(n1), "should be selected");			
	}),
	
	test("add - getPrimitiveId()", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		ds.selection.add(n1.getPrimitiveId());			
		util.assert(ds.selection.has(n1.getPrimitiveId()), "should be selected");			
	}),
	
	test("add - just a node", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		ds.selection.add(n1);			
		util.assert(ds.selection.has(n1), "should be selected");			
	}),
	test("add - null", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		ds.selection.add(null);			
	}),
	test("add - undefined", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		ds.selection.add(undefined);			
	}),
	test("add - multiple objects", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1,n2,w1);	
		util.assert(ds.selection.has(n1), "1 - should be selected");			
		util.assert(ds.selection.has(n2), "2 - should  be selected");
		util.assert(ds.selection.has(w1), "3 - should  be selected");
	}),
	test("add - multiple objects - as array", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add([n1,n2,w1]);	
		util.assert(ds.selection.has(n1), "1 - should be selected");			
		util.assert(ds.selection.has(n2), "2 - should  be selected");
		util.assert(ds.selection.has(w1), "3 - should  be selected");
	}),
	test("add - multiple objects - as collection", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		var set = new HashSet();
		set.add(n1); set.add(n2); set.add(w1);
		ds.selection.add(set);	
		util.assert(ds.selection.has(n1), "1 - should be selected");			
		util.assert(ds.selection.has(n2), "2 - should  be selected");
		util.assert(ds.selection.has(w1), "3 - should  be selected");
	}),

	
	// -- set 
	test("set - just a single node", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1);	
		ds.selection.set(n2);
		util.assert(ds.selection.isSelected(n2), "should be selected");			
		util.assert(!ds.selection.isSelected(n1), "should not be selected");
	}),
	test("set - a node , getPrimitiveId()", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1);	
		ds.selection.set(n2.getPrimitiveId())
		util.assert(ds.selection.isSelected(n2), "should be selected");			
		util.assert(!ds.selection.isSelected(n1), "should not be selected");
	}),
	test("set - a node, the node", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1);	
		ds.selection.set(n2)
		util.assert(ds.selection.isSelected(n2), "should be selected");			
		util.assert(!ds.selection.isSelected(n1), "should not be selected");
	}),
	test("set - null", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1);	
		ds.selection.set(null)
		util.assert(!ds.selection.isSelected(n1), "should  be selected");
	}),
	test("set - undefined", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1);	
		ds.selection.set(undefined)
		util.assert(!ds.selection.isSelected(n1), "should  be selected");
	}),
	
	
	// -- clear
	test("clear - just a single node", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1,n2);	
		util.assert(ds.selection.isSelected(n2), "1 - should be selected");
		ds.selection.clear(n1);
		util.assert(ds.selection.isSelected(n2), "2 - should be selected");			
		util.assert(!ds.selection.isSelected(n1), "3 - should not be selected");
	}),
	test("clear - a node , getPrimitiveId()", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1,n2);	
		ds.selection.clear(n1.getPrimitiveId())
		util.assert(ds.selection.isSelected(n2), "1 - should be selected");			
		util.assert(!ds.selection.isSelected(n1), "2 - should not be selected");
	}),
	test("clear - a node, the node", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1,n2);	
		ds.selection.clear(n1)
		util.assert(ds.selection.isSelected(n2), "1 - should be selected");			
		util.assert(!ds.selection.isSelected(n1), "2 - should not be selected");
	}),
	test("clear - null", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1,n2);	
		ds.selection.clear(null)
		util.assert(ds.selection.isSelected(n1), "1 - should  be selected");
		util.assert(ds.selection.isSelected(n2), "2 - should  be selected");
	}),
	test("clear - undefined", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1,n2);	
		ds.selection.clear(undefined)
		util.assert(ds.selection.isSelected(n1), "1 - should  be selected");
		util.assert(ds.selection.isSelected(n2), "2 - should  be selected");
	}),
	
	test("clear - multiple objects", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add(n1,n2,w1);	
		ds.selection.clear(n1,w1)
		util.assert(!ds.selection.isSelected(n1), "1 - should not be selected");			
		util.assert(ds.selection.isSelected(n2), "2 - should  be selected");
		util.assert(!ds.selection.isSelected(w1), "3 - should  not be selected");
	}),
	test("clear - multiple objects - as array", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		ds.selection.add([n1,n2,w1]);	
		ds.selection.clear([n1,w1]);
		util.assert(!ds.selection.isSelected(n1), "1 - should not be selected");			
		util.assert(ds.selection.isSelected(n2), "2 - should  be selected");
		util.assert(!ds.selection.isSelected(w1), "3 - should  not be selected");
	}),
	test("clear - multiple objects - as collection", function() {
		var ds = new DataSet();
		var n1 = ds.nodeBuilder.create(1);
		var n2 = ds.nodeBuilder.create(2);
		var w1 = ds.wayBuilder.withNodes(n1,n2).create(1);
		var set = new HashSet();
		set.add(n1); set.add(n2); set.add(w1);
		ds.selection.add(set);	
		var set = new HashSet();
		set.add(n1);  set.add(w1);
		ds.selection.clear(set);
		util.assert(!ds.selection.isSelected(n1), "1 - should not be selected");			
		util.assert(ds.selection.isSelected(n2), "2 - should  be selected");
		util.assert(!ds.selection.isSelected(w1), "3 - should  not be selected");
	})

));


suites.push(tu.suite("node, way, relation - object accessors",	
	test("node", function() {
		var ds = new DataSet();
		ds.nodeBuilder.create(1);
		var local = ds.nodeBuilder.create();
		var n = ds.node(1);
		util.assert(n, "node should be found");
		n = ds.node(2);
		util.assert(! n, "node should not be found");
		n = ds.node(local.id);
		util.assert(n, "node should be found");
		
		tu.expectAssertionError("illegal id 0", function() {
			ds.node(0);
		});
		
		tu.expectAssertionError("unsupported type", function() {
			ds.node("1");
		});
		
		tu.expectAssertionError("primitive id not supported", function() {
			ds.node(new SimplePrimitiveId(1, OsmPrimitiveType.NODE));
		});
	}),
	test("way", function() {
		var ds = new DataSet();
		ds.wayBuilder.create(1);
		var local = ds.wayBuilder.create();
		
		var w = ds.way(1);
		util.assert(w, "way should be found");
		w = ds.way(2);
		util.assert(! w, "way should not be found");
		w = ds.way(local.id);
		util.assert(w, "way should be found");

		tu.expectAssertionError("illegal id 0", function() {
			ds.way(0);
		});
		
		tu.expectAssertionError("unsupported type", function() {
			ds.way("1");
		});
		
		tu.expectAssertionError("primitive id not supported", function() {
			ds.way(new SimplePrimitiveId(1, OsmPrimitiveType.WAY));
		});
	}),
	test("relation", function() {
		var ds = new DataSet();
		ds.relationBuilder.create(1);
		var local = ds.relationBuilder.create();
		
		var r = ds.relation(1);
		util.assert(r, "relation should be found");
		r = ds.relation(2);
		util.assert(! r, "relation should not be found");
		r = ds.relation(local.id);
		util.assert(r, "relation should be found");

		tu.expectAssertionError("illegal id 0", function() {
			ds.relation(0);
		});
		
		tu.expectAssertionError("unsupported type", function() {
			ds.relation("1");
		});
		
		tu.expectAssertionError("primitive id not supported", function() {
			ds.relation(new SimplePrimitiveId(1, OsmPrimitiveType.RELATION));
		});
	})
));


suites.push(tu.suite("query",	
	test("query - josm expression - simple", function() {
			var ds = new DataSet();
			var nb = ds.nodeBuilder;
			var wb = ds.wayBuilder;
			ds.nodeBuilder.withTags({name: 'test'}).create(1);
			ds.nodeBuilder.withTags({amenity: 'restaurant'}).create(2);
			var w = ds.wayBuilder.withTags({highway: 'residential'}).withNodes(ds.node(1), ds.node(2)).create();
			var objs = ds.query("name=test");
			util.assert(objs.length == 1, "should have found one node, found {0}", objs.length);
			util.assert(objs[0].id == 1, "should have found node 1");
	}),
	test("query - josm expression - type", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		var wb = ds.wayBuilder;
		ds.nodeBuilder.withTags({name: 'test'}).create(1);
		ds.nodeBuilder.withTags({amenity: 'restaurant'}).create(2);
		var w = ds.wayBuilder.withTags({highway: 'residential'}).withNodes(ds.node(1), ds.node(2)).create();
		var objs = ds.query("type:node");
		util.assert(objs.length == 2, "should have found two node, found {0}", objs.length);
		util.assert(objs[0].isNode, "1 - should be a node");
		util.assert(objs[1].isNode, "2 - should be a node");
	}),
	test("query - josm expression - combined, with regexp", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		var wb = ds.wayBuilder;
		ds.nodeBuilder.withTags({name: 'test'}).create(1);
		ds.nodeBuilder.withTags({amenity: 'tttttt'}).create(2);
		var w = ds.wayBuilder.withTags({highway: 'tasdf'}).withNodes(ds.node(1), ds.node(2)).create();
		var objs = ds.query("type:node && *=^tt.*", {withRegexp: true});
		util.assert(objs.length == 1, "should have found two node, found {0}", objs.length);
		util.assert(objs[0].isNode, "1 - should be a node");
	}),
	
	
	test("query - predicate - simple", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		var wb = ds.wayBuilder;
		ds.nodeBuilder.withTags({name: 'test'}).create(1);
		ds.nodeBuilder.withTags({amenity: 'restaurant'}).create(2);
		var w = ds.wayBuilder.withTags({highway: 'residential'}).withNodes(ds.node(1), ds.node(2)).create();
		var objs = ds.query(function(obj) {
			return obj.get("name") == "test";
		});
		util.assert(objs.length == 1, "should have found one node, found {0}", objs.length);
		util.assert(objs[0].id == 1, "should have found node 1");
	}),
	test("query - josm expression - type", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		var wb = ds.wayBuilder;
		ds.nodeBuilder.withTags({name: 'test'}).create(1);
		ds.nodeBuilder.withTags({amenity: 'restaurant'}).create(2);
		var w = ds.wayBuilder.withTags({highway: 'residential'}).withNodes(ds.node(1), ds.node(2)).create();		
		var objs = ds.query(function(obj) {
			return obj.isNode;
		});
		util.assert(objs.length == 2, "should have found two node, found {0}", objs.length);
		util.assert(objs[0].isNode, "1 - should be a node");
		util.assert(objs[1].isNode, "2 - should be a node");
	}),
	test("query - josm expression - combined, with regexp", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		var wb = ds.wayBuilder;
		ds.nodeBuilder.withTags({name: 'test'}).create(1);
		ds.nodeBuilder.withTags({amenity: 'tttttt'}).create(2);
		var w = ds.wayBuilder.withTags({highway: 'tasdf'}).withNodes(ds.node(1), ds.node(2)).create();
		var objs = ds.query("type:node && *=^tt.*", {withRegexp: true});
		
		var objs = ds.query(function(){
			var regexp = /^tt.*/;
			return function(obj) {
				if (!obj.isNode) return false;
				var keys = obj.keys;
				for (var i=0; i< keys.length; i++) {
					if (regexp.test(obj.get(keys[i]))) return true;
				}
			};
		}());				
		util.assert(objs.length == 1, "should have found two node, found {0}", objs.length);
		util.assert(objs[0].isNode, "1 - should be a node");
	})
));


suites.push(tu.suite("each",	
	test("each - loop over a simple data set", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		var wb = ds.wayBuilder;
		ds.nodeBuilder.withTags({name: 'test'}).create(1);
		ds.nodeBuilder.withTags({amenity: 'restaurant'}).create(2);
		var w = ds.wayBuilder.withTags({highway: 'residential'}).withNodes(ds.node(1), ds.node(2)).create();
		var map = {};
		ds.each(function(obj) {
			map[obj.id] = obj;
		});
		util.assert(map[1] == ds.node(1), "1 - wrong object");
		util.assert(map[2] == ds.node(2), "2 - wrong object");
		util.assert(map[w.id] == ds.way(w.id), "3 - wrong object");			
	}), 
	test("each - null delegate", function() {
		var ds = new DataSet();
		ds.each(null);
	}),
	test("each - undefined delegate", function() {
		var ds = new DataSet();
		ds.each(undefined);
	}),
	test("each - null options", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		var wb = ds.wayBuilder;
		ds.nodeBuilder.withTags({name: 'test'}).create(1);
		ds.nodeBuilder.withTags({amenity: 'restaurant'}).create(2);
		var w = ds.wayBuilder.withTags({highway: 'residential'}).withNodes(ds.node(1), ds.node(2)).create();
		var map = {};
		ds.each(function(obj) {
			map[obj.id] = obj;
		}, null);
		util.assert(map[1] == ds.node(1), "1 - wrong object");
		util.assert(map[2] == ds.node(2), "2 - wrong object");
		util.assert(map[w.id] == ds.way(w.id), "3 - wrong object");	
	}),
	test("each - undefined options", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		var wb = ds.wayBuilder;
		ds.nodeBuilder.withTags({name: 'test'}).create(1);
		ds.nodeBuilder.withTags({amenity: 'restaurant'}).create(2);
		var w = ds.wayBuilder.withTags({highway: 'residential'}).withNodes(ds.node(1), ds.node(2)).create();
		var map = {};
		ds.each(function(obj) {
			map[obj.id] = obj;
		}, undefined);
		util.assert(map[1] == ds.node(1), "1 - wrong object");
		util.assert(map[2] == ds.node(2), "2 - wrong object");
		util.assert(map[w.id] == ds.way(w.id), "3 - wrong object");	
	}),
	test("each - options - all: false", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		var wb = ds.wayBuilder;
		ds.nodeBuilder.withTags({name: 'test'}).create(1);
		ds.nodeBuilder.withTags({amenity: 'restaurant'}).create(2);
		ds.nodeBuilder.createProxy(3);
		var w = ds.wayBuilder.withTags({highway: 'residential'}).withNodes(ds.node(1), ds.node(2)).create();
		var map= {};
		ds.each(function(obj) {
			map[obj.id] = obj;
		}, {all: false});
		util.assert(map[1] == ds.node(1), "1 - wrong object");
		util.assert(map[2] == ds.node(2), "2 - wrong object");
		util.assert(map[w.id] == ds.way(w.id), "3 - wrong object");	
		util.assert(!map[4], "should not have proxy node");
	}),
	test("each - options - all: true", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		var wb = ds.wayBuilder;
		ds.nodeBuilder.withTags({name: 'test'}).create(1);
		ds.nodeBuilder.withTags({amenity: 'restaurant'}).create(2);
		ds.nodeBuilder.createProxy(3);
		var w = ds.wayBuilder.withTags({highway: 'residential'}).withNodes(ds.node(1), ds.node(2)).create();
		var map = {};
		ds.each(function(obj) {
			map[obj.id] = obj;
		}, {all: true});
		util.assert(map[1] == ds.node(1), "1 - wrong object");
		util.assert(map[2] == ds.node(2), "2 - wrong object");
		util.assert(map[w.id] == ds.way(w.id), "3 - wrong object");	
		util.assert(map[3], "should  have proxy node");
	}),
	test("each - options - all: false", function() {
		var ds = new DataSet();
		var nb = ds.nodeBuilder;
		var wb = ds.wayBuilder;
		ds.nodeBuilder.withTags({name: 'test'}).create(1);
		ds.nodeBuilder.withTags({amenity: 'restaurant'}).create(2);
		ds.nodeBuilder.createProxy(3);
		var w = ds.wayBuilder.withTags({highway: 'residential'}).withNodes(ds.node(1), ds.node(2)).create();
		var map = {};
		tu.expectAssertionError("unsupported options", function() {
			ds.each(function(obj) {
				map[obj.id] = obj;
			}, "all");
		});
	})
));

exports.run = function() {
    return suites
        .map(function(a) { return a.run(); })
        .reduce(function(a, b) { return a + b; });
};