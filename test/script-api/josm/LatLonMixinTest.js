var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var LatLon    = org.openstreetmap.josm.data.coor.LatLon;

tu.suite("properties acess",
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
).run();	
