var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var LatLon    = org.openstreetmap.josm.data.coor.LatLon;

var suites = [];

suites.push(tu.suite("properties acess",
    test("lat", function() {
        var pos = new LatLon(1,2);
        util.assert(pos.lat == 1, "1- unexpected lat");
        util.assert(pos.$lat() == 1, "2 - unexpected lat");

        tu.expectError("can't set value for 'lat'", function() {
            pos.lat = 1;
        });
    }),

    test("lon", function() {
        tu.expectAssertionError("id - set - should fail", function() {
            var pos = new LatLon(1,2);
            util.assert(pos.lon == 2, "1- unexpected lon");
            util.assert(pos.$lon() == 2, "2 - unexpected lon");

            tu.expectError("can't set value for 'lon'", function() {
                pos.lon = 1;
            });
        });
    })
));

suites.push(tu.suite("make",
    test("make - valid object", function() {
        var pos = LatLon.make({lat: 1, lon: 2});
        util.assert(pos.lat == 1, "1- unexpected lat");
        util.assert(pos.lon == 2, "2 - unexpected lon");
    }),
    test("make - incomplete object", function() {
        tu.expectError("missing lat", function() {
            var pos = LatLon.make({lon: 2});
        });

        tu.expectError("missing lon", function() {
            var pos = LatLon.make({lat: 2});
        });
    }),
    test("make - illegal lats", function() {
        tu.expectError("null", function() {
            var pos = LatLon.make({lat: null, lon: 2});
        });

        tu.expectError("undefined", function() {
            var pos = LatLon.make({lat: undefined, lon: 2});
        });

        tu.expectError("string", function() {
            var pos = LatLon.make({lat: "1", lon: 2});
        });

        tu.expectError("illegal lat", function() {
            var pos = LatLon.make({lat: 200, lon: 2});
        });
    }),
    test("make - illegal lons", function() {
        tu.expectError("null", function() {
            var pos = LatLon.make({lat: 1, lon: null});
        });

        tu.expectError("undefined", function() {
            var pos = LatLon.make({lat: 1, lon: undefined});
        });

        tu.expectError("string", function() {
            var pos = LatLon.make({lat: 1, lon: "2"});
        });

        tu.expectError("illegal lat", function() {
            var pos = LatLon.make({lat: 1, lon: 200});
        });
    })
));

exports.run = function() {
    return suites
        .map(function(a) { return a.run(); })
        .reduce(function(a, b) { return a + b; });
};
