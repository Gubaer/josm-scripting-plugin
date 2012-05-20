
var URL = java.net.URL;
var OsmApi = org.openstreetmap.josm.io.OsmApi;
var Changeset = org.openstreetmap.josm.data.osm.Changeset;
var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
var PrimitiveId = org.openstreetmap.josm.data.osm.PrimitiveId;
var SimplePrimitiveId = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
var ProgressMonitor = org.openstreetmap.josm.gui.progress.ProgressMonitor;
var NullProgressMonitor = org.openstreetmap.josm.gui.progress.NullProgressMonitor;
var OsmServerChangesetReader = org.openstreetmap.josm.io.OsmServerChangesetReader;

var util = require("josm/util");

(function() { 

/**
 * <p>Provides methods to open,close, get, update, etc. changesets on the OSM API server.</p>
 * 
 * @class
 * @name ChangesetApi
 */
exports.ChangesetApi = function() {
}
	
/**
 * <p>Creates and opens a changeset</p>
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
 * @return org.openstreetmap.josm.data.osm.Changeset  
 */	
exports.ChangesetApi.open = function() {
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

/**
 * <p>Closes a changeset</p>
 * 
 * <ul>
 *   <li><code>close(id)</code>  - closes the changeset with the given id</li>
 *   <li><code>close(aChangeset)</code>  - closes the changeset given by <code>aChangeset</code></li>
 * </ul>
 * 
 * @example
 * var api = require("josm/api").ChangesetApi;
 * var util = require("josm/util");
 * var Changeset = org.openstreetmap.josm.data.osm.Changeset;
 * 
 * // closs the changeset 12345 
 * api.close(12345);
 * 
 * // open a new changeset with the tags given by the supplied changeset
 * var cs2 = new Changeset(12345);
 * cs2 = api.close(cs2); 
 * util.assert(cs2.closed);  // the changeset is now closed  
 * 
 * @name close
 * @memberOf ChangesetApi
 * @method
 * @static
 * @return org.openstreetmap.josm.data.osm.Changeset  
 */	
exports.ChangesetApi.close = function() {
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

/**
 * <p>Updates a changeset</p>
 * 
 * <ul>
 *   <li><code>update(aChangeset)</code>  - updates the changeset <code>aChangeset</code></li>
 * </ul>
 * 
 * @example
 * var api = require("josm/api").ChangesetApi;
 * var Changeset = org.openstreetmap.josm.data.osm.Changeset;
 * 
 * // update the comment of a changeset
 * var cs2 = new Changeset(12345);
 * cs2.put("comment", "an updated comment");  
 * cs2 = api.update(cs2); 
 * 
 * @name update
 * @memberOf ChangesetApi
 * @method
 * @static
 * @return org.openstreetmap.josm.data.osm.Changeset  
 */	
exports.ChangesetApi.update = function () {
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

/**
 * <p>Get a changeset</p>
 * 
 * <ul>
 *   <li><code>get(aChangeset)</code>  - gets the changeset specified by <code>aChangeset</code>. aChangset must be 
 *     an instance of <code>Changeset</code>. aChangeset.id &gt; 0 expected.</li>
 *   <li><code>get(id)</code>  - gets the changeset for the id. id must be a number &gt; 0.</li> 
 * </ul>
 * 
 * @example
 * var api = require("josm/api").ChangesetApi;
 * var Changeset = org.openstreetmap.josm.data.osm.Changeset;
 * 
 * // get the changeset with id 12345 
 * var cs1 = api.get(12345);
 * 
 * // get the changeset with id 12345
 * var cs2 = new Changeset(12345);
 * cs2 = api.get(cs2);   
 * 
 * @name get
 * @memberOf ChangesetApi
 * @method
 * @static
 * @return org.openstreetmap.josm.data.osm.Changeset  
 */	
exports.ChangesetApi.get = function() {
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

}());

(function () {
	
/**
 * <p>Collection of static methods to download objects from and upload objects to the 
 * OSM server.</p>
 * 
 * @namespace
 * @name Api
 */	
exports.Api = function() {
};

function normalizeType(type) {
	util.assert(util.isSomething(type), "type must not be null or undefined");
	if (util.isString(type)) {
		try {
			type = OsmPrimitiveType.fromApiTypeName(type);
		} catch(e) {
			util.assert(false, "Invalid primitive type, got ''{0}''", type);
		}
	} else if (type instanceof OsmPrimitiveType) {
		if (! [OsmPrimitiveType.NODE, OsmPrimitiveType.WAY, OsmPrimitiveType.RELATION].contains(type)) {
			util.assert(false, "Invalid primitive type, got {0}", type);
		}
	} else {
		util.assert(false, "Invalid primitive type, got {0}", type);
	}
	return type;
}

function normalizeId(id) {
	util.assert(util.isSomething(id), "id must not be null or nothing");
	util.assert(util.isNumber(id), "Expected a number as id, got {0}", id);
	util.assert(id > 0, "Expected a positive number as id, got {0}", id);
	return id;
}

function primitiveIdFromObject(o) {
	util.assert(o.hasOwnProperty("id"), "Mandatory property 'id' is missing in object {0}", o);
	util.assert(o.hasOwnProperty("type"), "Mandatory property 'type' is missing in object {0}", o);
	return new SimplePrimitiveId(normalizeId(o.id), normalizeType(o.type));
};

function downloadObject_1() {
	var id;
	var o = arguments[0];
	util.assert(util.isSomething(o), "Argument 0: must not be null or undefined");
	if (o instanceof PrimitiveId) {
		id = o;
	} else if (typeof o === "object") {
		id = primitiveIdFromObject(o);
	} else {
		util.assert(false, "Argument 0: unexpected type, got {0}", o);
	}
	var reader = new OsmServerObjectReader(id, false);
	var ds = reader.parseOsm(null /* null progress monitor */);
	return ds;
}

function optionFull(options) {
	if (!options.hasOwnProperty("full")) return undefined;
	var o = options.full;
	if (typeof o === "boolean") return o;
	util.assert("Expected a boolean value for option 'full', got {0}", o);
}

function optionVersion(options) {
	if (!options.hasOwnProperty("version")) return undefined;
	var o = options.version;
	util.assert(util.isNumber(o), "Expected a number for option 'version', got {0}", o);
	util.assert(version > 0, "Expected a number > 0 for option 'version', got {0}", o);
	return o;
}

function downloadObject_2() {
	var id;
	var options = {full: undefined, version: undefined};
	if (util.isNumber(arguments[0])) {
		var id = normalizeId(arguments[0]);
		var type = normalizeType(arguments[1]);
		id = new SimplePrimitiveId(id, type);
	} else if (arguments[0] instanceof PrimitiveId) {
		id = arguments[0];
		var o = arguments[1];
		if (util.isSomething(o)) {
			util.assert(typeof o === "object", "Expected an object with named parameters, got {0}", o);
			options.full = optionFull(o);
			options.version = optionVersion(o);
		}
	} else if (typeof arguments[0] === "object") {
		id = primitiveIdFromObject(arguments[0]);
	} else {
		util.assert(false, "Unsupported types of arguments");
	}
	var reader;
	if (util.isDef(options.version)) {
		reader = new OsmServerObjectReader(id, !!options.full, version);
	} else {
		reader = new OsmServerObjectReader(id, !!options.full);
	}
	var ds = reader.parseOsm(null /* null progress monitor */);
	return ds;
}

function downloadObject_3() {
	var id;
	var options = {full: undefined, version: undefined};
	var n = normalizeId(arguments[0]);
	var type = normalizeType(arguments[1]);
	id = new SimplePrimitiveId(n, type);
	
	util.assert(typeof arguments[2] === "object", "Expected an object with named parameters, got {0}", arguments[2]);
	options.full = optionFull(arguments[2]);
	options.version = optionVersion(arguments[2]);
	var reader;
	if (util.isDef(options.version)) {
		reader = new OsmServerObjectReader(id, !!options.full, version);
	} else {
		reader = new OsmServerObjectReader(id, !!options.full);
	}
	var ds = reader.parseOsm(null /* null progress monitor */);
	return ds;
}

/**
 * <p>Downloads an object from the server.</p>
 * 
 * <p>There are multiple options to specify what object to download. In addition, the function
 * accepts a set of optional named parameters as last argument.</p>
 * 
 * <dl>
 *   <dt><strong>downloadObject(id, type, ?options)</strong></dt>
 *   <dd><var>id</var> is the global numeric id. 
 *   <var>type</var> is either one of the strings "node", "way", or "relation", or one of the 
 *   enumeration OsmPrimitiveType.NODE, OsmPrimitiveType.WAY, or OsmPrimitiveType.RELATION
 *   </dd>
 *   
 *   <dt><strong>downloadObject(id, ?options)</strong></dt>
 *   <dd><var>id</var> is a <var>PrimitiveId</var> or an object 
 *   with the (mandatory) properties <var>id</var> and <var>type</var>, i.e. an object <code>{id: ..., type: ...}</code>.
 *   <var>id</var> is again a number, <var>type</var> is again either one of the strings "node", "way", or "relation", or one of the 
 *   enumeration OsmPrimitiveType.NODE, OsmPrimitiveType.WAY, or OsmPrimitiveType.RELATION.
 *   </dd> 
 * </dl>
 * In both cases, <var>?otpions</var> is an (optional) object with the following two (optional) properties:
 * <dl>
 *   <dt><strong>full</strong>  - a boolean value</dt>
 *   <dd>If <var>true</var>, the object and its immediate children are downloaded, i.e. the nodes of a way and 
 *   the relation members of a relation. Default if missing is <var>false</var>.</dd>
 *   
 *   <dt><strong>version</strong>  - a positive number</dt>
 *   <dd>If present, the specified version of the object is downloaded. If missing, the current version is downloaded.</dd>
 * </dl>
 * 
 * @example
 * var api = require("josm/api").Api;
 * 
 * // download the node with id 12345
 * var ds1 = api.downloadObject(12345, "node");
 * 
 * // download the node with id 12345
 * var ds2 = api.downloadObject({id: 12345, type: "node"});
 * 
 * // download the node with id 12345
 * var id = new SimplePrimitiveId(12345, OsmPrimitiveType.NODE);
 * var ds3 = api.downloadObject(id);
 *
 * // download version 5 of the full way 12345 (including its nodes) 
 * var ds3 = api.downloadObject({id: 12345: type: "way"}, {full: true, version: 5});
 * 
 * @method
 * @static
 * @name downloadObject
 * @memberOf Api
 * @return org.openstreetmap.josm.data.osm.DataSet
 */
exports.Api.downloadObject = function() {
	var id;
	var options = {full: undefined ,version: undefined};
	
	switch(arguments.length) {
	case 0:
		util.assert(false, "Unexpected number of arguments, got {0}", arguments.length);
	case 1:
		return downloadObject_1(arguments);
		break;
	case 2:
		return downloadObject_2(arguments);
		break;
	case 3: 
		return downloadObject_3(arguments);
		break;
	default:
		util.assert(false, "Unexpected number of arguments, got {0}", arguments.length);
	}
};
	
}()) 