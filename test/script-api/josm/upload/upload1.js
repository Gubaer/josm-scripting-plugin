(function() {
/** 
 * Functional test - uploads a small test data set to the test API server, 
 * downloads it again, modifies one node and uploads again. 
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

// upload an array of primitives
//
var data = [];
var nb = require("josm/builder").NodeBuilder;
var wb = require("josm/builder").WayBuilder;
var n1 = nb.withPosition(46.9488598, 7.4627158).withTags({name: "n1"}).create();
var n2 = nb.withPosition(46.9488506, 7.4653749).withTags({name: "n2"}).create();
var w1 = wb.withNodes(n1,n2).withTags({name: "w1"}).create();

out.println(">>> Uploading data ...");
api.upload([n1,n2,w1], "test upload");

// downloads the data
//
out.println(">>> Downloading the data...");
var bounds = {min: {lat: 46.9469986, lon: 7.4618811}, max: {lat: 46.9506842, lon: 7.4661356}};
var ds = api.downloadArea(bounds);

// change two tags of one node and upload again. We use the "individualobjects"
// strategy- this should result in two upload calls to the server 
//
out.println(">>> Upload the data ...");
ds.node(n1.id).tags.name = "n1 - updated";
ds.node(n2.id).tags.name = "n2 - updated";
api.upload(ds, "test upload 2", {strategy: "individualobjects"});


}());