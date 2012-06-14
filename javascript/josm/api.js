/**
 * <p>This module provides functions to retrieve data from and upload data
 * to an OSM server.</p> 
 * 
 * @module josm/api
 */
var URL = java.net.URL;
var OsmApi = org.openstreetmap.josm.io.OsmApi;
var Changeset = org.openstreetmap.josm.data.osm.Changeset;
var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
var PrimitiveId = org.openstreetmap.josm.data.osm.PrimitiveId;
var SimplePrimitiveId = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
var ProgressMonitor = org.openstreetmap.josm.gui.progress.ProgressMonitor;
var NullProgressMonitor = org.openstreetmap.josm.gui.progress.NullProgressMonitor;
var OsmServerChangesetReader = org.openstreetmap.josm.io.OsmServerChangesetReader;
var OsmServerObjectReader = org.openstreetmap.josm.io.OsmServerObjectReader;
var OsmServerBackreferenceReader = org.openstreetmap.josm.io.OsmServerBackreferenceReader;

var util = require("josm/util");

(function() { 

/**
 * <p>Provides methods to open, close, get, update, etc. changesets on the OSM API server.</p>
 * 
 * <p><strong>Note:</strong> this class doesn't provide a constructor. Methods and properties
 * are "static".</p>
 * 
 * @example
 * // load the changeset api
 * var api = require("josm/api").ChangesetApi;
 *
 * // create a new changeset on the server 
 * var cs = api.open();
 * 
 * @class
 * @name ChangesetApi
 * @memberof josm/api
 */
exports.ChangesetApi = {};
	
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
 * @type org.openstreetmap.josm.data.osm.Changeset  
 * @summary Creates and opens a changeset
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
 * <dl>
 *   <dt><code class="signature">close(id)</code></dt>
 *   <dd>closes the changeset with the given id</dd>
 *   
 *   <dt><code class="signature">close(aChangeset)</code><dt>
 *   <dd>Xloses the changeset given by <code>aChangeset</code></dd>
 * </dl>
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
 * @param {number|org.openstreetmap.josm.data.osm.Changeset} changeset  the changeset to close 
 * @method
 * @static
 * @type org.openstreetmap.josm.data.osm.Changeset  
 * @summary Closes a changeset
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
 * <dl>
 *   <dt><code class="signature">update(aChangeset)</code></dt>
 *   <dd>Updates the changeset <code>aChangeset</code></dd>
 * </dl>
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
 * @param {org.openstreetmap.josm.data.osm.Changeset} changeset  the changeset to update
 * @memberOf ChangesetApi
 * @method
 * @static
 * @type org.openstreetmap.josm.data.osm.Changeset  
 * @summary Updates a changeset
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
 * <p>Get a changeset from the server</p>
 * 
 * <dl>
 *   <dt><code class="signature>get(aChangeset)</code></dt>
 *   <dd>Gets the changeset specified by <code>aChangeset</code>. aChangset must be 
 *     an instance of <code>Changeset</code>. aChangeset.id &gt; 0 expected.</dd>
 *     
 *   <dt><code class="signature">get(id)</code></dt>
 *   <dd>gets the changeset for the id. id must be a number &gt; 0.</dd> 
 * </dl>
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
 * @param {number|org.openstreetmap.josm.data.osm.Changeset} changeset  the changeset to close
 * @method
 * @static
 * @type org.openstreetmap.josm.data.osm.Changeset 
 * @summary Get a changeset from the server  
 */	
exports.ChangesetApi.get = function() {
	var cs;
	switch(arguments.length) {
	case 0: 
		util.assert(false, "Missing arguments. Expected a changeset id or a changeset");
	case 1: 
		var o = arguments[0];
		if (o instanceof Changeset) {
			cs = o;
		} else if (util.isNumber(o)) {
			util.assert(o > 0, "Expected a positive changeset id, got {0}", o);
			cs = new Changeset(o);
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
	
var undefined; 
	
/**
 * <p>Collection of static methods to download objects from and upload objects to the 
 * OSM server.</p>
 * 
 * <p><strong>Note:</strong> this class doesn't provide a constructor. Methods and properties
 * are "static".</p>
 *
 * @example
 * // load the changeset api
 * var api = require("josm/api").Api;
 *
 * // download node 12345 
 * var ds = api.downloadObject(12345, "node");
 *
 * @class
 * @name Api
 * @memberof josm/api
 */	
exports.Api = {};

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
 *   <dt><code class="signature">downloadObject(id, type, ?options)</code></dt>
 *   <dd><code>id</code> is the global numeric id. 
 *   <code>type</code> is either one of the strings "node", "way", or "relation", or one of the 
 *   enumeration OsmPrimitiveType.NODE, OsmPrimitiveType.WAY, or OsmPrimitiveType.RELATION
 *   </dd>
 *   
 *   <dt><codee class="signaure">downloadObject(id, ?options)</code></dt>
 *   <dd><code>id</code> is a <code>PrimitiveId</code> or an object 
 *   with the (mandatory) properties <code>id</code> and <code>type</code>, i.e. an object <code>{id: ..., type: ...}</code>.
 *   <code>id</code> is again a number, <code>type</code> is again either one of the strings "node", "way", or "relation", or one of the 
 *   enumeration OsmPrimitiveType.NODE, OsmPrimitiveType.WAY, or OsmPrimitiveType.RELATION.
 *   </dd> 
 * </dl>
 * In both cases, <code>?options</code> is an (optional) object with the following two (optional) properties:
 * <dl>
 *   <dt><code class="signature">full</code>: boolean</dt>
 *   <dd>If <code>true</code>, the object and its immediate children are downloaded, i.e. the nodes of a way and 
 *   the relation members of a relation. Default if missing is <code>false</code>.</dd>
 *   
 *   <dt><code class="signature">version</code>: number</dt>
 *   <dd>If present, the specified version of the object is downloaded. If missing, the current version is downloaded.</dd>
 * </dl>
 * 
 * @example
 * var api = require("josm/api").Api;
 * var SimlePrimitiveId = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
 * var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
 * 
 * // download the node with id 12345
 * var ds1 = api.downloadObject(12345, "node");
 * 
 * // download the node with id 12345
 * var ds2 = api.downloadObject({id: 12345, type: "node"});
 * 
 * // download the full relation (including its members) with id 12345
 * var id = new SimplePrimitiveId(12345, OsmPrimitiveType.RELATION);
 * var ds3 = api.downloadObject(id, {full: true});
 *
 * // download version 5 of the full way 12345 (including its nodes) 
 * var ds4 = api.downloadObject(12345, OsmPrimitiveType.WAY, {full: true, version: 5});
 * 
 * @method
 * @static
 * @name downloadObject
 * @memberOf Api
 * @type org.openstreetmap.josm.data.osm.DataSet
 * @summary Downloads an object from the server. 
 */
exports.Api.downloadObject = function() {
	var id;
	var options = {full: undefined,version: undefined};
	
	switch(arguments.length) {
	case 0:
		util.assert(false, "Unexpected number of arguments, got {0}", arguments.length);
	case 1:
		return downloadObject_1.apply(this, arguments);
		break;
	case 2:
		return downloadObject_2.apply(this, arguments);
		break;
	case 3: 
		return downloadObject_3.apply(this, arguments);
		break;
	default:
		util.assert(false, "Unexpected number of arguments, got {0}", arguments.length);
	}
};

function downloadReferrer_1()  {
	var id;
	var type;
	var o = arguments[0];
	util.assert(util.isSomething(o), "Argument 0: must not be null or undefined");
	if (o instanceof PrimitiveId) {
		id = o;
	} else if (typeof o === "object") {
		id = primitiveIdFromObject(o);
	} else {
		util.assert(false, "Argument 0: unexpected type, got {0}", o);
	}
	var reader = new OsmServerBackreferenceReader(id.getUniqueId(), id.getType());
	var ds = reader.parseOsm(NullProgressMonitor.INSTANCE);
	return ds;
};

function downloadReferrer_2() {
	var id;
	var options = {full: undefined};
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
		}
	} else if (typeof arguments[0] === "object") {
		id = primitiveIdFromObject(arguments[0]);
		var o = arguments[1];
		if (util.isSomething(o)) {
			util.assert(typeof o === "object", "Expected an object with named parameters, got {0}", o);
			options.full = optionFull(o);
		}
	} else {
		util.assert(false, "Unsupported types of arguments");
	}
	var reader = new OsmServerBackreferenceReader(id.getUniqueId(), id.getType());
	if (options.full){
		reader.setReadFull(true);
	}
	var ds = reader.parseOsm(NullProgressMonitor.INSTANCE);
	return ds;
};

function downloadReferrer_3() {
	var id;
	var options = {full: undefined};
	var n = normalizeId(arguments[0]);
	var type = normalizeType(arguments[1]);
	id = new SimplePrimitiveId(n, type);
	
	util.assert(typeof arguments[2] === "object", "Expected an object with named parameters, got {0}", arguments[2]);
	options.full = optionFull(arguments[2]);
	var reader;
	var reader = new OsmServerBackreferenceReader(id.getUniqueId(), id.getType());
	if (options.full){
		reader.setReadFull(true);
	}
	var ds = reader.parseOsm(NullProgressMonitor.INSTANCE);
	return ds;
};

/**
 * <p>Downloads the objects <em>referring</em> to another object from the server.</p>
 * 
 * <p>Downloads primitives from the OSM server which
 * refer to a specific primitive. Given a node, the referring ways and relations are downloaded.
 * Given a way or a relation, only referring relations are downloaded.</p>
 *
 * <p>The default behaviour is to reply proxy objects only.</p>
 *
 * <p>If you set the option <code>{full: true}</code>, every referring object is downloaded in full.</p>
 * 
 * <p>There are multiple options to specify what referrers to download. In addition, the function
 * accepts a set of optional named parameters as last argument.</p>
 * 
 * <dl>
 *   <dt><code class="signature">downloadReferrer(id, type, ?options)</code></dt>
 *   <dd><code>id</code> is the global numeric id. 
 *   <code>type</code> is either one of the strings "node", "way", or "relation", or one of the 
 *   enumeration {@josmclass org.openstreetmap.josm.data.osm.OsmPrimitiveType}.NODE, 
 *   {@josmclass org.openstreetmap.josm.data.osm.OsmPrimitiveType}.WAY, 
 *   or {@josmclass org.openstreetmap.josm.data.osm.OsmPrimitiveType}.RELATION.
 *   </dd>
 *   
 *   <dt><code class="signature">downloadReferrer(id, ?options)</code></dt>
 *   <dd><code>id</code> is a <code>PrimitiveId</code> or an object 
 *   with the (mandatory) properties <code>id</code> and <code>type</code>, i.e. an object <code>{id: ..., type: ...}</code>.
 *   <code>id</code> is again a number, <code>type</code> is again either one of the strings "node", "way", or "relation", or one of the 
 *   enumeration {@josmclass org.openstreetmap.josm.data.osm.OsmPrimitiveType}.NODE, {@josmclass org.openstreetmap.josm.data.osm.OsmPrimitiveType}.WAY, or {@josmclass org.openstreetmap.josm.data.osm.OsmPrimitiveType}.RELATION.
 *   </dd> 
 * </dl>
 * In both cases, <code>?options</code> is an (optional) object with the following  (optional) property:
 * <dl>
 *   <dt><code class="signature">full</code>:boolean</dt>
 *   <dd>If <code>true</code>, the the <strong>full</strong> objects are retrieved using multi-gets. If missing or <code>false</code>,
 *   only proxy objects are downloaded. Default: false</dd>
 * </dl>
 * 
 * @example
 * var api = require("josm/api").Api;
 * var nbuilder = require("josm/builder").NodeBuilder;
 * var SimlePrimitiveId = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
 * var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
 * 
 * // download the objects referring to the node with id 12345
 * var ds1 = api.downloadReferrer(12345, "node");
 * 
 * // download the objects referring to the node with id 12345
 * var ds2 = api.downloadReferrer({id: 12345, type: "node"});
 * 
 * // download the relations referring to the  relation with id 12345. 
 * // Referring relations are downloaded in full.  
 * var id = new SimplePrimitiveId(12345, OsmPrimitiveType.RELATION);
 * var ds3 = api.downloadReferrer(id, {full: true});
 * 
 *  // create the global node 12345 ...
 *  var node = nbuilder.create(12345);
 *  // ... and downloads its referrers in full 
 *  var ds = api.downloadReferrer(node, {full: true});
 * 
 * @method
 * @static
 * @name downloadReferrer
 * @memberOf Api
 * @type org.openstreetmap.josm.data.osm.DataSet
 * @summary Downloads the objects <em>referring</em> to another object from the server.
 */
exports.Api.downloadReferrer = function() {
	var id;
	switch(arguments.length) {
	case 0:
		util.assert(false, "Unexpected number of arguments, got {0}", arguments.length);
	case 1:
		return downloadReferrer_1.apply(this, arguments);
		break;
	case 2:
		return downloadReferrer_2.apply(this, arguments);
		break;
	case 3: 
		return downloadReferrer_3.apply(this, arguments);
		break;
	default:
		util.assert(false, "Unexpected number of arguments, got {0}", arguments.length);
	}
};

/**
 * <p>Downloads the objects within a bounding box.</p>
 *
 * @example
 * var api = require("josm/api").Api;
 * var ds1 = api.downloadArea(new Bounds(
 *     new LatLon(46.9479186,7.4619484),   // min
 *     new LatLon(46.9497642, 7.4660683)   // max
 * ));
 * 
 * var ds1 = api.downloadArea({
 *     min: {lat: 46.9479186, lon: 7.4619484}, 
 *     max: {lat: 46.9497642, lon: 7.4660683}  
 * }); 
 * 
 * @method
 * @static
 * @name downloadArea
 * @memberOf Api
 * @type org.openstreetmap.josm.data.osm.DataSet
 * @summary Downloads the objects within a bounding box
 */
exports.Api.downloadArea = function() {
	var BoundingBoxDownloader = org.openstreetmap.josm.io.BoundingBoxDownloader;
	var NullProgressMonitor = org.openstreetmap.josm.gui.progress.NullProgressMonitor;
	var Bounds = org.openstreetmap.josm.data.Bounds;
	
	util.assert(arguments.length == 1, "Expected 1 argument, got {0}", arguments.length);
	var bounds = arguments[0];
	util.assert(util.isSomething(bounds), "bounds: must not be null or undefined");
	if (bounds instanceof Bounds) {
		// do nothing
	} else if (typeof bounds === "object") {
		bounds = Bounds.make(bounds); // convert to bounds
	} else {
		util.assert(false, "expected an instance of Bounds or an object, got {0}", bounds);
	}
	var downloader = new BoundingBoxDownloader(bounds);
	return downloader.parseOsm(NullProgressMonitor.INSTANCE);
};


/**
 * <p>Uploads objects to the server.</p>
 * 
 * <p>You can submit data either as {@josmclass org.openstreetmap.josm.data.osm.DataSet},
 * {@josmclass org.openstreetmap.josm.data.APIDataSet}, javascript array of 
 * {@josmclass org.openstreetmap.josm.data.osm.OsmPrimitive}s or a {@class java.util.Collection} of
 * {@josmclass org.openstreetmap.josm.data.osm.OsmPrimitive}s.</p>
 * 
 * <p>This method supports the same upload strategy as the JOSM upload dialog. Supply the named
 * parameter <code>{strategy: ...}</code> to choose the strategy.</p>
 * 
 * <p style="background-color: #FAC0C7;border-style: solid; border-color: white;">
 * Be careful when uploading data to the OSM server! Make sure not to upload copyright protected data
 * or test data.
 * </p>
 * 
 * <p>The method takes care to update the primitives in the uploaded data when the upload succeeds. For instance,
 * uploaded new primitives become global objects and get assigned their new id and version, successfully deleted
 * objects become invisible, etc.</p>
 * 
 * <p>Even if the entire upload of a dataset fails, a subset therefore may have been uploaded successfully. In order
 * to keep track, which pritives have been uploaded successfully in case of an error, the method replies a collection of
 * the successfully uploaded objects.</p>    

 * <p>Named options</p>
 * <dl>
 *   <dt><code class="signature">strategy: string|{@josmclass org.openstreetmap.josm.gui.io.UploadStrategy}</code></dt>
 *   <dd>Indicates how the data is uploaded. Either one of the strings
 *     <ul>
 *          <li>individualobjects</li>
 *          <li>chunked</li>
 *          <li>singlerequest</li>
 *       </ul>
 *      or one of the enumeration values in {@josmclass org.openstreetmap.josm.gui.io.UploadStrategy}.
 *      Default falue: UploadStrategy.DEFAULT_UPLOAD_STRATEGY
 *   </dd>
 *   
 *    <dt><code class="signature">changeset: number|{@josmclass org.openstreetmap.josm.data.osm.Changset}</code></dt>
 *    <dd>The changeset to which the data is uploaded. Either a number (the changeset id) or a 
 *    {@josmclass org.openstreetmap.josm.data.osm.Changset} instance. Default: creates a new changeset.</dd>
 *    
 *    <dt><code class="signature">chunkSize: number</code></dt>
 *    <dd>The size of an upload chunk, if the data is uploaded with the upload strategy 
 *    {@josmclass org.openstreetmap.josm.gui.io.UploadStrategy}.CHUNKED_DATASET_STRATEGY.</dd>
 *    
 *    <dt><code class="signature">closeChangeset: boolean</code></dt>
 *    <dd>If true, closes the changeset after the upload. Default: true</dd>    
 * </dl>
 *
 * @example
 * var ds = new org.openstreetmap.josm.data.osm.DataSet();
 * ds.wayBuilder.withNodes(
 *     ds.nodeBuilder.withTags({name: 'node1'}).create(),
 *     ds.nodeBuilder.withTags({name: 'node2'}.create()
 * ).withTags({name: 'way1'}).create();
 * var api = require("josm/api").Api;
 * 
 * // uploads the data in a new changeset in one chunk)
 * var processed = api.upload(ds, "just testing");
 * 
 * @param {org.openstreetmap.josm.data.osm.DataSet, org.openstreetmap.josm.data.APIDataSet, array, java.util.Collection} data the data to upload
 * @param {string} comment the upload comment 
 * @param {object} options (optional) various options (see above) 
 * @method
 * @static
 * @name upload
 * @memberOf Api
 * @type java.util.Collection
 * @summary Uploads objects
 */
exports.Api.upload = function(data, comment, options) {
	var UploadStrategy = org.openstreetmap.josm.gui.io.UploadStrategy;
	var Changeset = org.openstreetmap.josm.data.osm.Changeset;
	var APIDataSet = org.openstreetmap.josm.data.APIDataSet;
	var DataSet = org.openstreetmap.josm.data.osm.DataSet;
	var UploadStrategySpecification = org.openstreetmap.josm.gui.io.UploadStrategySpecification;
	var Collection = java.util.Collection;
	var OsmServerWriter = org.openstreetmap.josm.io.OsmServerWriter;

	comment = comment || "";
	comment = String(comment);

	util.assertSomething(data, "data: must not be null or undefined");
	options = options || {};
	util.assert(typeof options === "object", "options: expected an object with named arguments, got {0}", options);
	
	
	function normalizeChunkSize(size) {
		util.assert(util.isNumber(size), "chunksize: expected a number, got {0}", size);
		util.assert(size >= -1, "chunksize: expected -1 or a number > 0, got {0]", size);
		return size;
	};
	
	function normalizeChangeset(changeset) {
		if (util.isNothing(changeset)) {
			return new Changeset();
		} else if (util.isNumber(changeset)) {
			util.assert(changeset > 0, "changeset: expected a changeset id > 0, got {0}", changeset);
			return new Changeset(changeset);
		} else if (changeset instanceof Changeset) {
			return changeset;
		} else {
			util.assert(false, "changeset: unexpected value, got {0}", changeset);
		}
	};
	
	function uploadSpecFromOptions(options) {
		var strategy = options.strategy || UploadStrategy.DEFAULT_UPLOAD_STRATEGY;
		strategy = UploadStrategy.from(strategy);
		
		var chunkSize = options.chunkSize || UploadStrategySpecification.UNSPECIFIED_CHUNK_SIZE;
		chunkSize = normalizeChunkSize(chunkSize);
		
		var closeChangeset = util.isDef(options.closeChangeset) ? options.closeChangeset : true;
		closeChangeset = Boolean(closeChangeset);
				
		var spec = new UploadStrategySpecification();
		spec.setStrategy(strategy);
		spec.setChunkSize(chunkSize);
		spec.setCloseChangesetAfterUpload(closeChangeset);
		return spec;
	};
	
	
	var apiDataSet;
	if (data instanceof DataSet) {
		apiDataSet = new APIDataSet(data);
	} else if (data instanceof APIDataSet) {
		apiDataSet = data;
	} else if (util.isArray(data)) {
		apiDataSet = new APIDataSet(data);
	} else if (data instanceof Collection) {
		apiDataSet = new APIDataSet(data);
	} else {
		util.assert(false, "data: unexpected type of value, got {0}", data);
	};
	
	if (apiDataSet.isEmpty()) return undefined;
	apiDataSet.adjustRelationUploadOrder();
	var toUpload = apiDataSet.getPrimitives();
	
	var changeset = options.changeset || new Changeset();
	changeset = normalizeChangeset(changeset);
	changeset.put("comment", comment);
	var spec = uploadSpecFromOptions(options);
	var writer = new OsmServerWriter();
	
	writer.uploadOsm(spec, toUpload, changeset, null /* progress monitor */);
	if (spec.isCloseChangesetAfterUpload()) {
		exports.ChangesetApi.close(changeset);
    }
	
	return writer.getProcessedPrimitives();
};



/* ------------------------------------------------------------------------------------------ */
/* ApiConfig                                                                                  */
/* ------------------------------------------------------------------------------------------ */
/**
 * <p>ApiConfig provides methods and properties for configuring API parameters.</p>
 * 
 * @class ApiConfig
 * @memberof josm/api
 */
Object.defineProperty(exports, "ApiConfig", {
	value: {},
	writable: false,
	enumerable: true
});

var Main = org.openstreetmap.josm.Main;
var DEFAULT_URL = org.openstreetmap.josm.gui.preferences.server.OsmApiUrlInputPanel.defaulturl;
var URL = java.net.URL;
/**
 * <p>Get or set the API server URL.</p>
 * 
 * <dl>
 *   <dt><code class="signature">get</code></dt>
 *   <dd>Replies the currently configured server URL or undefinend, if no server URL is
 *   configured.</dd>
 *   <dt><code class="signature">set</code></dt>
 *   <dd>Sets the current server URL. If null or undefined, removes the current configuration. 
 *   Accepts either a string or a {@class java.net.URL}. Only accepts http or https URLs.
 *   </dd>
 * </dl>
 * 
 * @example
 * var conf = require("josm/api").ApiConfig;
 * conf.serverUrl;   // -> the current server url
 * 
 * // set a new API url
 * conf.serverUrl = "http://api06.dev.openstreetmap.org";  
 * 
 * @field
 * @memberOf ApiConfig
 * @static
 * @summary Get or set the API server URL.
 * @type string
 * @name serverUrl
 */
Object.defineProperty(exports.ApiConfig, "serverUrl", {
	enumerable: true,
	get: function() {
		var url = Main.pref.get("osm-server.url", null);
		if (url == null) url = DEFAULT_URL;
		return url == null ? undefined: util.trim(url);
	},
	
	set: function(value) {
		if (util.isNothing(value)) {
			Main.pref.put("osm-server.url", null);
		} else if (value instanceof URL) {
			util.assert(value.getProtocol() == "http" || value.getProtocol() == "https", "url: expected a http or https URL, got {0}", value);
			Main.pref.put("osm-server.url", value.toString());
		} else if (util.isString(value)) {
			value = util.trim(value);
			try {
				var url = new URL(value);
				util.assert(url.getProtocol() == "http" || url.getProtocol() == "https", "url: expected a http or https URL, got {0}", url.toString());
				Main.pref.put("osm-server.url", url.toString());
			} catch(e) {				
				util.assert(false, "url: doesn''t look like a valid URL, got {0}. Error: {1}", value, e);
			}
		} else {
			util.assert(false, "Unexpected type of value, got {0}", value);
		}
	}
});

/**
 * <p>Get the default server URL.</p>
 * @example
 * var conf = require("josm/api").ApiConfig;
 * conf.defaultServerUrl;   // -> the default server url
 * 
 * @field
 * @memberOf ApiConfig
 * @static
 * @summary Get the default server URL-
 * @type string
 * @name defaultServerUrl
 * @readOnly
 */
Object.defineProperty(exports.ApiConfig, "defaultServerUrl", {
	value: DEFAULT_URL,
	writable: false,
	enumerable: true
});

function normalizeAuthMethod(authMethod) {
	util.assert(util.isString(authMethod), "authMethod: expected a string, got {0}", authMethod);
	authMethod = util.trim(authMethod).toLowerCase();
	util.assert(authMethod == "basic" || authMethod == "oauth", "Unsupported value for authMethod, got {0}", authMethod);
	return authMethod;
};

/**
 * <p>Get or set the authentication method.</p>
 * 
 * <p>JOSM uses two authentication methods:</p>
 * <dl>
 *    <dt><code class="signature">basic</code></dt>
 *    <dd>Basic authentication with a username and a password</dd>
 *    <dt><code class="signature">oauth</code></dt>
 *    <dd>Authentication with the <a href="http://oauth.net/">OAuth</a> protocol.</dd>
 * </dl>
 * 
 * @example
 * var conf = require("josm/api").ApiConfig;
 * conf.authMethod;   // -> the current authentication method
 * 
 * // set OAuth as authentication method
 * conf.authMethod = "oauth";
 * 
 * @field
 * @memberOf ApiConfig
 * @static
 * @summary Get or set the authentication method.
 * @type string
 * @name authMethod
 */
Object.defineProperty(exports.ApiConfig, "authMethod", {
	enumerate: true,
	get: function() {
		var authMethod = Main.pref.get("osm-server.auth-method", "basic");
		authMethod = util.trim(authMethod).toLowerCase();
		if (authMethod == "basic" || authMethod == "oauth") return authMethod;
		// unsupported value for authMethod in the preferences. Returning
		// "basic" as default.
		return "basic";		
	},
	set: function(value) {
		value = normalizeAuthMethod(value);
		Main.pref.put("osm-server.auth-method", value);		
	}	
});

/**
 * <p>Gets the credentials, i.e. username and password for the basic authentication method.</p>
 * 
 * <strong>Named options</strong>
 * <dl>
 *    <dt><code class="signature">host:string</dt>
 *    <dd>The host name of the API server for which credentials are retrieved. If missing,
 *    the host name of the currently configured OSM API server is assumed.</dd>
 * </dl>
 * 
 * @example
 * var conf = require("josm/api").ApiConfig;
 * 
 * // get username/password for the current OSM API server 
 * var credentials = conf.getCredentials("basic");
 * 
 * @param {string} authMethod  the authentication method. Either "basic" or "oauth".
 * @param {object} options  (optional) additional options (see above)
 * @method
 * @memberOf ApiConfig
 * @static
 * @summary Gets the credentials.
 * @type object
 * @name getCredentials
 */
exports.ApiConfig.getCredentials = function(authMethod, options) {
	var CredentialsManager = org.openstreetmap.josm.io.auth.CredentialsManager;
	var OsmApi = org.openstreetmap.josm.io.OsmApi;
	var RequestorType = java.net.Authenticator.RequestorType;
	
	options = options || {};
	util.assert(typeof options === "object", "options: expected an object with named options, got {0}", options);
	
	function getBasicCredentials() {
		var cm = CredentialsManager.getInstance();
		if (options.host) options.host = util.trim(String(options.host));
		var host = options.host ? options.host : OsmApi.getOsmApi().getHost();
		var pa = cm.lookup(RequestorType.SERVER, host);
		return pa ? {host: host, user: pa.getUserName(), password: java.lang.String.valueOf(pa.getPassword())}
		          : {host: host, user: undefined, password: undefined};
	};
	
	function getOAuthCredentials() {
		var cm = CredentialsManager.getInstance();
		var token = cm.lookupOAuthAccessToken();
		if (token == null) return undefined;
		return {key: token.getKey(), secret: token.getSecret()};
	};
	
	authMethod = normalizeAuthMethod(authMethod);
	if (authMethod == "basic") return getBasicCredentials();
	if (authMethod == "oauth") return getOAuthCredentials();
	util.assert(false, "Unsupported authentication method, got {0}", authMethod);
};


function normalizeBasicCredentials(credentials) {
	var PasswordAuthentication = java.net.PasswordAuthentication;
	
	if (util.isNothing(credentials)) return null;
	util.assert(credentials instanceof PasswordAuthentication || typeof credentials === "object", "basic credentials: expected an object or an instance of PasswordAuthentication , got {0}", credentials);
	if (credentials instanceof PasswordAuthentication) {
		return credentials;
	} else {
		var user = String(credentials.user || "");
		var password = credentials.password || null;
		password = password ? new java.lang.String(password).toCharArray() : password;
		return new PasswordAuthentication(user, password);
	}	
};

function normalizeOAuthCredentials(credentials) {
	var OAuthToken = org.openstreetmap.josm.data.oauth.OAuthToken;
	if (util.isNothing(credentials)) return null;
	util.assert(credentials instanceof OAuthToken || typeof credentials === "object", "oauth credentials: expected an object or an instance of OAuthToken , got {0}", credentials);
	if (credentials instanceof OAuthToken) {
		return credentials;
	} else {
		var key = String(credentials.key || "");
		var secret = String(credentials.secret || "");
		return new OAuthToken(key,secret);
	}
}

/**
 * <p>Set the credentials, i.e. username and password for the basic authentication method.</p>
 * 
 * <p>Basic authentication credentials are either an instance of java.net.PasswordAuthentication or
 * an object <code>{user: string, password: string}</code>.</p>
 * 
 * <p>OAuth authentication credentials are either an instance of {@josmclass org.openstreetmap.josm.data.oauth.OAuthToken} or
 * an object <code>{key: string, secret: string}</code>.</p>

 * <strong>Named options</strong>
 * <dl>
 *    <dt><code class="signature">host:string</dt>
 *    <dd>The host name of the API server for which credentials are set. If missing,
 *    the host name of the currently configured OSM API server is assumed.</dd>
 * </dl>
 * 
 * @example
 * var conf = require("josm/api").ApiConfig;
 * 
 * // set the credentials
 * conf.setCredentials("basic", {user:"test", password:"apassword"});
 * 
 * @param {string} authMethod  the authentication method. Either "basic" or "oauth".
 * @param {object,org.openstreetmap.josm.data.oauth.OAuthToken,java.net.PasswordAuthentication} credentials  the credentials.
 * @param {object} options  (optional) additional options (see above)
 * @method
 * @memberOf ApiConfig
 * @static
 * @summary Set the credentials.
 * @type object
 * @name setCredentials
 */
exports.ApiConfig.setCredentials = function(authMethod, credentials, options) {
	var CredentialsManager = org.openstreetmap.josm.io.auth.CredentialsManager;
	var RequestorType = java.net.Authenticator.RequestorType;
	var OsmApi = org.openstreetmap.josm.io.OsmApi;
	var out = java.lang.System.out;
	
	options = options || {};
	util.assert(typeof options === "object", "options: expected an object with named options, got {0}", options);
	authMethod = normalizeAuthMethod(authMethod);
	if (authMethod == "basic") {
		credentials = normalizeBasicCredentials(credentials);
		util.assert(credentials != null, "credentials: can''t store null credentials");
		var host = options.host ? String(options.host) : null;
		host = host ? host : OsmApi.getOsmApi().getHost();
		var cm = CredentialsManager.getInstance();
		cm.store(RequestorType.SERVER, host, credentials);
	} else if (authMethod == "oauth") {
		credentials = normalizeOAuthCredentials(credentials);
		util.assert(credentials != null, "credentials: can''t store null credentials");
		var cm = CredentialsManager.getInstance();
		cm.storeOAuthAccessToken(credentials);		
	} else {
		util.assert(false, "Unsupported authentication method, got {0}", authMethod);
	}
};

	
}());