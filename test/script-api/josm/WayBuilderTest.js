var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var wb = require("josm/builder").WayBuilder;
var nb = require("josm/builder").NodeBuilder;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var ArrayList = java.util.ArrayList;

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
			util.assert(false, "Expected AssertionError, got {0}", e.toSource());
		} 
	}
};

var suite = tu.suite(

	// -- with nodes 
	test("local way - most simple way", function() {
		var way = wb.create();
		util.assert(util.isSomething(way), "expected a way object");		
		util.assert(way.id < 0, "id should be negative");
	}),
	test("local way - with nodes - two local nodes", function() {
		var way = wb.withNodes(nb.create(), nb.create()).create();
		util.assert(util.isSomething(way), "expected a way object");		
		util.assert(way.id < 0, "id should be negative");
		util.assert(way.nodes.length == 2, "2 nodes expected");
	}),
	test("local way - with nodes - two local nodes (as array)", function() {
		var way = wb.withNodes([nb.create(), nb.create()]).create();		
		util.assert(util.isSomething(way), "expected a way object");		
		util.assert(way.id < 0, "id should be negative");
		util.assert(way.nodes.length == 2, "2 nodes expected");
	}),
	test("local way - with nodes - two local nodes (as list)", function() {
		var nodes = new ArrayList();
		nodes.add(nb.create());
		nodes.add(nb.create());
		var way = wb.withNodes(nodes).create();		
		util.assert(util.isSomething(way), "expected a way object");		
		util.assert(way.id < 0, "id should be negative");
		util.assert(way.nodes.length == 2, "2 nodes expected");
	}),
	test("local way - with nodes - null is OK", function() {
		var way = wb.withNodes(null).create();		
		util.assert(way.length == 0, "no nodes expected");
	}),
	test("local way - with nodes - no nodes is OK", function() {
		var way = wb.withNodes().create();		
		util.assert(way.length == 0, "no nodes expected");
	}),
	test("local way - with nodes - null nodes are skipped", function() {
		var way = wb.withNodes(nb.create(), null, nb.create()).create();		
		util.assert(way.length == 2, "2 nodes expected");
	}),
	test("local way - with nodes - undefined nodes are skipped", function() {
		var way = wb.withNodes([nb.create(), undefined, nb.create()]).create();		
		util.assert(way.length == 2, "2 nodes expected");
	}),
	test("local way - with nodes - illegal type of arguments", function() {
		expectAssertionError(function() {
			var way = wb.withNodes("can pass in a string").create();
		});
	}),
	test("local way - with nodes - illegal types of nodes", function() {
		expectAssertionError(function() {
			var way = wb.withNodes([nb.create(), "string not allowed"]).create();
		});
	}),
	
	
	test("create - id = 1", function() {
		var way = wb.create(1);
		util.assert(way.id == 1, "id should be 1");
		util.assert(way.version == 1, "version should be 1");
	}),
	
	test("create - id = 1 (optional argument)", function() {
		var way = wb.create({id: 1});
		util.assert(way.id == 1, "id should be 1");
		util.assert(way.version == 1, "version should be 1");
	}),
	
	test("create - id = 1, version=2 ", function() {
		var way = wb.create(1, {version: 2});
		util.assert(way.id == 1, "id should be 1");
		util.assert(way.version == 2, "version should be 2");
	}),
	test("create - no id, tags as optional parameters ", function() {
		var way = wb.create({tags: {highway: "residential"}});
		util.assert(way.id < 0, "id should be negative");
		util.assert(way.version == undefined, "version should be unknown");
		util.assert(way.length == 0, "0 nodes expected");
		util.assert(way.tags.highway == "residential", "highway=residential expected");
	}),
	test("create - global id, tags and nodes", function() {
		var way = wb.create(1234, {nodes: [nb.create(), nb.create()], tags: {highway: "residential"}});
		util.assert(way.id == 1234, "id = 1234 expected");
		util.assert(way.version == 1, "version =1 expected");
		util.assert(way.length == 2, "2 nodes expected");
		util.assert(way.tags.highway == "residential", "highway=residential expected");
	}),
	test("create - id 0 - not allowed", function() {
		expectAssertionError(function() {
			var way = wb.create(0);
		})
	}),
	test("create - id -1 - negative id not allowed", function() {
		expectAssertionError(function() {
			var way = wb.create(-1);
		})
	}),
	test("create - id - illegal type", function() {
		expectAssertionError(function() {
			var way = wb.create("1234"); // string not allowed 
		})
	}),
	test("create - id - null not allowed", function() {
		expectAssertionError(function() {
			var way = wb.create(null);  
		})
	}),
	test("create - id - undefined not allowed", function() {
		expectAssertionError(function() {
			var way = wb.create(undefined);  
		})
	}),
	test("create - named args can be null", function() {
		var way = wb.create(1, null);  
	}),
	test("create - named args can be undefined", function() {
		var way = wb.create(1, undefined);  
	}),
	test("create - id - named args must be an object", function() {
		expectAssertionError(function() {
			var way = wb.create(1, "can use a string here");  
		})
	}),
	test("create - version - must not be 0", function() {
		expectAssertionError(function() {
			var way = wb.create(1, {version: 0});  
		})
	}),
	test("create - version - must not be negative", function() {
		expectAssertionError(function() {
			var way = wb.create(1, {version: -1});  
		})
	}),
	test("create - version - must be a number", function() {
		expectAssertionError(function() {
			var way = wb.create(1, {version: "1234"});  
		})
	}),
	test("create - nodes - can be null", function() {
		var way = wb.create(1, {nodes: null});  
	}),
	test("create - nodes - can be undefined", function() {
		var way = wb.create(1, {nodes: undefined});  
	}),
	test("create - nodes - can't be a single node", function() {
		expectAssertionError(function() {
			var way = wb.create(1, {nodes: nb.create()});  
		})		  
	}),
	test("create - nodes - can't be a string", function() {
		expectAssertionError(function() {
			var way = wb.create(1, {nodes: "node 1"});  
		})		  
	})
);

suite.run();