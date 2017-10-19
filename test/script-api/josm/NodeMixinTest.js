var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var nb = require("josm/builder").NodeBuilder;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var Node    = org.openstreetmap.josm.data.osm.Node;
var Way     = org.openstreetmap.josm.data.osm.Way;
var LatLon     = org.openstreetmap.josm.data.coor.LatLon;
var ArrayList = java.util.ArrayList;

var suites = [];

suites.push(tu.suite("properties access",
    test("id - read", function() {
        var n = nb.create(1234);
        util.assert(n.id === 1234, "unexpected id");
    }),
    test("id - set - should fail", function() {
        tu.expectError("id - set - should fail", function() {
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
        
        // make sure lon is *not* changed, only lat
        n = nb.create({lat: 0, lon: 56.78});
        n.lat = 12.34;
        util.assert(n.lat == 12.34, "4 - unexpected lat");
        util.assert(n.lon == 56.78, "4 - unexpected lon");

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
        
        // make sure lat is *not* changed, only lon
        n = nb.create({lat: 12.34, lon: 0.0});
        n.lon = 56.78;
        util.assert(n.lat == 12.34, "4 - unexpected lat");
        util.assert(n.lon == 56.78, "4 - unexpected lon");

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
    }),

    test("pos - get/set ", function() {
        var n;

        // a global node
        n = nb.create(1234);
        n.pos = new LatLon(1,2);
        var pos = n.pos;
        util.assert(pos.$lat() == 1, "1 - unexpected lat");
        util.assert(pos.$lon() == 2, "1 - unexpected lon");

        // a local node
        n = nb.create();
        n.pos = new LatLon(1,2);
        var pos = n.pos;
        util.assert(pos.$lat() == 1, "2 - unexpected lat");
        util.assert(pos.$lon() == 2, "2 - unexpected lon");

        // a loc
        n = nb.create();
        n.pos = {lat: 1, lon: 2};
        util.assert(n.lat == 1, "3 - unexpected lat");
        util.assert(n.lon == 2, "3 - unexpected lon");


        tu.expectAssertionError("can't assign null", function() {
            n.pos = null;
        });
        tu.expectAssertionError("can't assign undefined", function() {
            n.pos = undefined;
        });
        tu.expectAssertionError("can't assign a string", function() {
            n.pos= "1,2";
        });

        tu.expectAssertionError("can't assign an object with missing lat property", function() {
            n.lon = {lon: 123};
        });

        tu.expectAssertionError("can't assign an object with missing lon property", function() {
            n.lon = {lat: 2};
        });
        tu.expectAssertionError("can't assign an object wit illegal lat - null", function() {
            n.lon = {lat: null, lon: 2};
        });
        tu.expectAssertionError("can't assign an object wit illegal lat - undefined", function() {
            n.lon = {lat: undefined, lon: 2};
        });
        tu.expectAssertionError("can't assign an object wit illegal lat - string", function() {
            n.lon = {lat: "1", lon: 2};
        });
        tu.expectAssertionError("can't assign an object wit illegal lat - illegal value", function() {
            n.lon = {lat: -456, lon: 2};
        });
        tu.expectAssertionError("can't assign an object with illegal lon - null", function() {
            n.lon = {lat: 1, lon: null};
        });
        tu.expectAssertionError("can't assign an object with illegal lon - undefined", function() {
            n.lon = {lat: 1, lon: undefined};
        });
        tu.expectAssertionError("can't assign an object with illegal lon - string", function() {
            n.lon = {lat: 1, lon: "2"};
        });
        tu.expectAssertionError("can't assign an object with illegal lon - illegal value", function() {
            n.lon = {lat: 1, lon: 564};
        });
    }),

    test("east - set/get", function() {
        var n;

        // should have a defined east
        n = nb.create(1234);
        n.pos = new LatLon(1,2);
        util.assert(util.isNumber(n.east), "1- east should be a number");

        // proxies don't have an east
        n = nb.createProxy(1234);
        util.assert(n.east == undefined, "east should be undefined");

        // local nodes should have defined east (assuming
        // the current projection is set)
        n = nb.create();
        util.assert(util.isNumber(n.east), "2 - east should be a number");

        tu.expectError("can't set value for 'east'", function() {
            n.east = 1;
        });

    }),

    test("north - set/get", function() {
        var n;

        // should have a defined east
        n = nb.create(1234);
        n.pos = new LatLon(1,2);
        util.assert(util.isNumber(n.north), "1-  north should be a number");

        // proxies don't have an east
        n = nb.createProxy(1234);
        util.assert(n.north == undefined, "north should be undefined");

        // local nodes should have defined north (assuming
        // the current projection is set)
        n = nb.create();
        util.assert(util.isNumber(n.north), "2 - north should be a number");

        tu.expectError("can't set value for 'north'", function() {
            n.north = 1;
        });
    })
));

suites.push(tu.suite("setting modified flag as side effect",
    test("lat", function() {
        var n = nb.create(1234);
        n.lat = 1;
        util.assert(n.modified, "1 - should be modified");
        n.modified = false;
        n.lat = 1;
        util.assert(!n.modified, "2 - should not be modified");
        n.lat = 2;
        util.assert(n.modified, "3 - should  be modified");
    }),
    test("lon", function() {
        var n = nb.create(1234);
        n.lon = 1;
        util.assert(n.modified, "1 - should be modified");
        n.modified = false;
        n.lon = 1;
        util.assert(!n.modified, "2 - should not be modified");
        n.lon = 2;
        util.assert(n.modified, "3 - should  be modified");
    }),
    test("pos", function() {
        var n = nb.create(1234);
        n.pos = {lat: 1, lon: 2};
        util.assert(n.modified, "1 - should be modified");
        n.modified = false;
        n.pos =  {lat: 1, lon: 2};
        util.assert(!n.modified, "2 - should not be modified");
        n.pos = {lat: 3, lon: 4};
        util.assert(n.modified, "3 - should  be modified");
    })
));

exports.run = function() {
    for (var i=0; i<suites.length; i++) {
        suites[i].run();
    }
};