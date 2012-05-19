
var URL = java.net.URL;
var OsmApi = org.openstreetmap.josm.io.OsmApi;
var Changeset = org.openstreetmap.josm.data.osm.Changeset;
var ProgressMonitor = org.openstreetmap.josm.gui.progress.ProgressMonitor;
var NullProgressMonitor = org.openstreetmap.josm.gui.progress.NullProgressMonitor;
var OsmServerChangesetReader = org.openstreetmap.josm.io.OsmServerChangesetReader;

var util = require("josm/util");

(function() { 
	
function open() {
	var cs;
	switch(arguments.length) {
	case 0: 
		cs = new Changeset();
		break;
	case 1: 
		var o = arguments[0];
		if (o instanceof Changeset) {
			cs = o;
		} else if (typeof o === "object") {
			cs = new Changeset();
			for (var p in o) {
				if (!o.hasOwnProperty(p)) continue;
				var key = p;
				var value = o[p];
				key = util.trim(key);
				value = value + ""; // convert to string 
				cs.put(key,value);
			}
		} else {
			util.assert(false, "Unexpected type of argument, expected Changeset or object, got {0}", o);
		}
		break;
	default:
		util.assert(false, "Unexpected number of arguments, got {0}", arguments.length);
	}
	var api = OsmApi.getOsmApi();
	api.openChangeset(cs, NullProgressMonitor.INSTANCE);
	return cs;
};

function close() {
	var cs;
	switch(arguments.length) {
	case 0: 
		util.assert(false, "Missing arguments. Expected a changeset it or a changeset");
	case 1: 
		var o = arguments[0];
		if (o instanceof Changeset) {
			cs = o;
		} else if (util.isNumber(o)) {
			util.assert(o > 0, "Expected a positive changeset id, got {0}", o);
			cs = new Changeset(id);
		} else {
			util.assert(false, "Unexpected type of argument, expected Changeset or number, got {0}", o);
		}
		break;
	default:
		util.assert(false, "Unexpected number of arguments, got {0}", arguments.length);
	}
	var api = OsmApi.getOsmApi();
	api.closeChangeset(cs, NullProgressMonitor.INSTANCE);
	return cs;
};

function update() {
	var cs;
	switch(arguments.length) {
	case 0: 
		util.assert(false, "Missing arguments. Expected a changeset");
	case 1: 
		var o = arguments[0];
		if (o instanceof Changeset) {
			cs = o;
		} else {
			util.assert(false, "Unexpected type of argument, expected Changeset, got {0}", o);
		}
		break;
	default:
		util.assert(false, "Unexpected number of arguments, got {0}", arguments.length);
	}
	var api = OsmApi.getOsmApi();
	api.updateChangeset(cs, NullProgressMonitor.INSTANCE);
	return cs;
};

function get() {
	var cs;
	switch(arguments.length) {
	case 0: 
		util.assert(false, "Missing arguments. Expected a changeset it or a changeset");
	case 1: 
		var o = arguments[0];
		if (o instanceof Changeset) {
			cs = o;
		} else if (util.isNumber(o)) {
			util.assert(o > 0, "Expected a positive changeset id, got {0}", o);
			cs = new Changeset(id);
		} else {
			util.assert(false, "Unexpected type of argument, expected Changeset or number, got {0}", o);
		}
		break;
	default:
		util.assert(false, "Unexpected number of arguments, got {0}", arguments.length);
	}
	var reader = new OsmServerChangesetReader();
	cs = reader.readChangeset(cs.id, NullProgressMonitor.INSTANCE);
	return cs;
};

/**
 * <p>Provides methods to open,close, get, update, etc. changesets on the OSM API server.</p>
 * 
 * @class
 * @name ChangesetApi
 */
exports.ChangesetApi = function() {
}

/**
 * <p>Opens a changeset</p>
 * 
 * <ul>
 *   <li><code>open()</code>  - open a new changeset with no tags</li>
 *   <li><code>open(aChangeset)</code>  - open a new changeset with the tags from <code>aChangeset</code></li>
 *   <li><code>open(anObject)</code>  - open a new changeset with the tags given by the properties of <code>anObject</code></li>
 * </ul>
 * 
 * @example
 * var api = require("josm/api").ChangesetApi;
 * var Changeset = org.openstreetmap.josm.data.osm.Changeset;
 * 
 * // open a new changeset with no tags 
 * var cs1 = api.open();
 * 
 * // open a new changeset with the tags given by the supplied changeset
 * var cs2 = new Changeset();
 * cs2.put("comment", "a test comment");
 * cs2 = api.open(cs2);
 * 
 * // open a new changeset with the tags given by the object
 * var cs3 = api.open({comment: "a test comment"});
 * 
 * @name open
 * @memberOf ChangesetApi
 * @method
 * @static
 * @return org.openstreetmap.josm.data.osm.ChangeSet  
 */
exports.ChangesetApi.open = open;
exports.ChangesetApi.close = close;
exports.ChangesetApi.get = get;
exports.ChangesetApi.update = update;


}());

 