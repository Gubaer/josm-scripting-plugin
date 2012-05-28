var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var nb = require("josm/builder").NodeBuilder;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var Node    = org.openstreetmap.josm.data.osm.Node;
var Way     = org.openstreetmap.josm.data.osm.Way;
var ArrayList = java.util.ArrayList;

tu.suite("properties access",
	test("id - read", function() {
		var n = nb.create(1234);		
		util.assert(n.id === 1234, "unexpected id");
	}),
	test("id - set - should fail", function() {
		tu.expectAssertionError("id - set - should fail", function() {
			var n = nb.create(1234);
			n.id = 1234;
		});
	}),
	test("lat - get/set ", function() {
		// a global node
		var n = nb.create(1234);
		n.lat = 23.23;
		util.assert(n.lat == 23.23, "1 - unexpected lat");
		
		// a local node
		n = nb.create();
		n.lat = 23.23;
		util.assert(n.lat == 23.23, "2 - unexpected lat");
		
		// a local node
		n = nb.create({lat: 12.34});
		util.assert(n.lat == 12.34, "3 - unexpected lat");

				
		tu.expectAssertionError("can't assign null", function() {
			n.lat = null;
		});
		tu.expectAssertionError("can't assign undefined", function() {
			n.lat = undefined;
		});
		tu.expectAssertionError("can't assign a string", function() {
			n.lat = "23.23";
		});

		tu.expectAssertionError("can't assign an illegal lat", function() {
			n.lat = -234.23;
		});
		
		tu.expectAssertionError("can't set lat on a proxy node", function() {
			n = nb.createProxy(12345);
			n.lat = 23.44;
		});
	}),
	test("lon - get/set ", function() {
		// a global node
		var n = nb.create(1234);
		n.lon = 23.23;
		util.assert(n.lon == 23.23, "1 - unexpected lon");
		
		// a local node
		n = nb.create();
		n.lon = 23.23;
		util.assert(n.lon == 23.23, "2 - unexpected lon");
		
		// a local node with initialized lon
		n = nb.create({lon: 12.34});
		util.assert(n.lon == 12.34, "3 - unexpected lat");
				
		tu.expectAssertionError("can't assign null", function() {
			n.lon = null;
		});
		tu.expectAssertionError("can't assign undefined", function() {
			n.lon = undefined;
		});
		tu.expectAssertionError("can't assign a string", function() {
			n.lon = "23.23";
		});

		tu.expectAssertionError("can't assign an illegal lat", function() {
			n.lon = -234.23;
		});
		
		tu.expectAssertionError("can't set lon on a proxy node", function() {
			n = nb.createProxy(12345);
			n.lon = 23.44;
		});
	})
).run();
	