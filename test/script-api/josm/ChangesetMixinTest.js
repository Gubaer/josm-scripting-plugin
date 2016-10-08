var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var nb = require("josm/builder").NodeBuilder;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var Node    = org.openstreetmap.josm.data.osm.Node;
var Way     = org.openstreetmap.josm.data.osm.Way;
var LatLon     = org.openstreetmap.josm.data.coor.LatLon;
var ArrayList = java.util.ArrayList;
var Changeset = org.openstreetmap.josm.data.osm.Changeset;

var suites = [];

suites.push(suite = tu.suite("properties access",
    test("id - read", function() {
        var cs = new Changeset(1234);
        util.assert(cs.id === 1234, "unexpected id");
    }),
    test("open", function() {
        var cs = new Changeset(1234);
        util.assert(!cs.open, "1 - should not be open");
        util.assert(!cs.isOpen, "2 - should not be open");

        cs.open = false;
        util.assert(!cs.open, "3 - should not be open");
        util.assert(!cs.isOpen, "4 - should not be open");

        cs.open = true;
        util.assert(cs.open, "4 - should  be open");
        util.assert(cs.isOpen, "5 - should  be open");

        tu.expectAssertionError("can't assign null", function() {
            cs.open = null;
        });
        tu.expectAssertionError("can't assign undefined", function() {
            cs.open = undefined;
        });
        tu.expectAssertionError("can't assign a string", function() {
            cs.open = "true";
        });
    }),
    test("min", function() {
        var cs = new Changeset(1234);
        cs.min = new LatLon(1,2);
        util.assert(cs.min.lat == 1, "1 - should be 1");
        util.assert(cs.min.lon == 2, "2 - should be 2");

        cs.min = null;
        util.assert(cs.min == undefined, "3 - should be undefined");

        cs.min = {lat: 1, lon: 2};
        util.assert(cs.min.lat == 1, "1 - should be 1");
        util.assert(cs.min.lon == 2, "2 - should be 2");
    }),

    test("max", function() {
        var cs = new Changeset(1234);
        cs.max = new LatLon(1,2);
        util.assert(cs.max.lat == 1, "1 - should be 1");
        util.assert(cs.max.lon == 2, "2 - should be 2");

        cs.max = null;
        util.assert(cs.max == undefined, "3 - should be undefined");

        cs.max = {lat: 1, lon: 2};
        util.assert(cs.max.lat == 1, "4 - should be 1");
        util.assert(cs.max.lon == 2, "5 - should be 2");
    }),
    test("bounds", function() {
        var cs = new Changeset(1234);
        cs.min = {lat: 1, lon: 2};
        cs.max = new LatLon(3,4);
        util.assert(cs.bounds.min.lat == 1, "1 - should be 1");
        util.assert(cs.bounds.min.lon == 2, "2 - should be 2");
        util.assert(cs.bounds.max.lat == 3, "3 - should be 3");
        util.assert(cs.bounds.max.lon == 4, "4 - should be 4");

        cs = new Changeset(1234);
        util.assert(cs.bounds == undefined, "should be undefined");
    }),
    test("tags", function() {
        var cs = new Changeset(1234);
        cs.tags.comment = "my comment";
        util.assert(cs.tags.comment == "my comment", "unexpected tag");
    })
));

exports.run = function() {
    for (var i=0; i< suites.length; i++) {
        suites[i].run();
    }
};

