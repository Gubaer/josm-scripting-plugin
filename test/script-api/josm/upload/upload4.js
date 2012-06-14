(function() {
/** 
 * Functional test - tries to delete an already deleted node 
 */

var tu = require("josm/unittest");
var util = require("josm/util");
var api = require("josm/api").Api;
var conf = require("josm/api").ApiConfig;
var csapi = require("josm/api").ChangesetApi;
var out = java.lang.System.out;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var OsmApi = org.openstreetmap.josm.io.OsmApi; 
var HashSet = java.util.HashSet;
var nb = require("josm/builder").NodeBuilder;

function assertNoProductionServer() {
	var host = OsmApi.getOsmApi().getHost();
	util.assert(host != "api.openstreetmap.org", "WARNING: do not upload test data to the OSM production server! Aborting script.");
};

conf.authMethod = "basic";
conf.serverUrl = "http://api06.dev.openstreetmap.org/api";
conf.setCredentials("basic", {user: "guggis", password:"guggis1234"});
assertNoProductionServer();


var cs = csapi.open();

var node = nb.withPosition(46.9488598, 7.4627158).create();
api.upload([node], "uploading a node");




data = [];
for(var i=0; i< 50; i++) {
	data.push(nb.withPosition(46.9488598, 7.4627158).create());
}
//uploading 50 nodes in already open changeset, in chunked mode, 10 objects
//per request, with closing the changeset
//
api.upload(data, "test upload - 2", {
	strategy: "chunked",
	changeset: cs,
	chunkSize: 10,
	closeChangeset: true
});

cs = csapi.get(cs.id);
out.println("Changeset " + cs.getId() + ": open" + cs.isOpen());

}());