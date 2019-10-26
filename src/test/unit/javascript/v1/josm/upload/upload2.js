(function() {
/** 
 * Functional test - uploads a small test data set to the test API server 
 */

var tu = require("josm/unittest");
var util = require("josm/util");
var api = require("josm/api").Api;
var conf = require("josm/api").ApiConfig;
var out = java.lang.System.out;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var OsmApi = org.openstreetmap.josm.io.OsmApi; 

function assertNoProductionServer() {
	var host = OsmApi.getOsmApi().getHost();
	util.assert(host != "api.openstreetmap.org", "WARNING: do not upload test data to the OSM production server! Aborting script.");
};

conf.authMethod = "basic";
conf.serverUrl = "http://api06.dev.openstreetmap.org/api";
conf.setCredentials("basic", {user: "guggis", password:"guggis1234"});
assertNoProductionServer();


var ds = new DataSet();
ds.wayBuilder.withNodes(
	ds.nodeBuilder.withTags({name: 'node1'}).create(),
	ds.nodeBuilder.withTags({name: 'node2'}).create()
).withTags({name: 'way1'}).create();

api.upload(ds, "testing");

}());