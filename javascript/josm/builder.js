/**
 * Collection of builders for creating OSM nodes, ways and relations.
 * 
 * @module josm/builder
 */	

// -- imports	
var Node              = org.openstreetmap.josm.data.osm.Node;
var Way               = org.openstreetmap.josm.data.osm.Way;
var Relation          = org.openstreetmap.josm.data.osm.Relation;
var DataSet           = org.openstreetmap.josm.data.osm.DataSet;
var OsmPrimitiveType  = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
var SimplePrimitiveId = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
var LatLon            = org.openstreetmap.josm.data.coor.LatLon;

var util = require("josm/util");

function assertGlobalId(id) {
	util.assertSomething(id, "Expected a defined, non-null object id, got {0}", id);
	util.assertNumber(id, "Expected a number as object id, got {0}", id);
	util.assert(id > 0, "Expected a positive id, got {0}", id);
};

function rememberId(builder, id, version){
	assertGlobalId(id);	
	builder.id = id;
	version = util.isDef(version) ? version: 1; 
	util.assertNumber(version, "Expected a number for 'version', got {0}", version);
	util.assert(version > 0, "Expected a positive number for 'version', got {0}", version);
	builder.version = version; 
};

function rememberTags(builder, tags) {
	if (util.isNothing(tags)) return;
	util.assert(typeof tags === "object", "Expected a hash with tags, got {0}", tags);
	builder.tags = builder.tags || {};
	for (var name in tags) {
		if (! tags.hasOwnProperty(name)) break;
		name = util.trim(name);
		var value = tags[name];
		if (util.isNothing(value)) break;
		value = util.trim(value + "");	
		builder.tags[name] = value; 
	}
}

function assignTags(primitive, tags) {
	for (var name in tags) {
		var value = tags[name];
		if (util.isNothing(value)) continue;
		value = util.trim(value + "");
		primitive.put(name, value);
	}
};

// -------------------------------------------------------------------------------
// NodeBuilder
//--------------------------------------------------------------------------------
(function() {	
	
/**
 * <p>NodeBuilder helps to create OSM nodes.</p>
 * 
 * @class 
 * @name NodeBuilder
 * 
 * @example
 *  var nbuilder = require("josm/builder").NodeBuilder;
 *  
 *  // create a new local node at position (0,0) without tags
 *  var n1 = nbuilder.local();
 *  
 *  // create a new global node at a specific position with tags 
 *  var n2 = nbuilder.withPosition(1,1).withTags({name: 'test'}).global(123456);
 *  
 *  // create a new proxy for a global node (an "incomplete" node in JOSM terminology)
 *  var n3 = nbuilder.proxy(123456);
 */
exports.NodeBuilder = function(ds) {
	if (util.isSomething(ds)) {
		util.assert(ds instanceof DataSet, "Expected a JOSM dataset, got {0}", ds);
		this.ds = ds;
	} 
};

/**
 * <p>Creates a new NodeBuilder which will add created nodes to the dataset <code>ds</code<.</p>
 * 
 * @example
 * 
 * var ds = new org.openstreetmap.josm.data.osm.DataSet();
 * var nb = require("josm/builder").NodeBuilder.forDataSet(ds);
 * 
 * @memberOf NodeBuilder
 * @method
 * @return the node builder
 * @type exports.NodeBuilder 
 */
exports.NodeBuilder.forDataSet = function(ds) {
	util.assert(util.isSomething(ds), "Expected a non-null defined object, got {0}", ds);
	util.assert(ds instanceof DataSet, "Expected a JOSM dataset, got {0}", ds);
	return new exports.NodeBuilder(ds);
};

function receiver(that) {
	return typeof that === "object" ? that : new exports.NodeBuilder();
}

function checkLat(value) {
	if (! util.isSomething(value)) throw "lat must not be null or undefined";
	if (! util.isNumber(value)) throw "lat must be a number";
	if (! LatLon.isValidLat(value)) throw "lat must be a valid latitude";
	return value;
}
function checkLon(value) {
	if (! util.isSomething(value)) throw "lon must not be null or undefined";
	if (! util.isNumber(value)) throw "lon must be a number";
	if (! LatLon.isValidLon(value)) throw "lon must be a valid longitude";
	return value;
}

function initFromObject(builder, args) {
	if (args.hasOwnProperty("id")) {
		var o = args["id"];
		util.assert(util.isSomething(o), "''{0}'': must not be null or undefined", "id");
		util.assert(util.isNumber(o), "''{0}'': expected a number, got {1}", "id", o);
		util.assert(o > 0, "''{0}'': expected a number > 0, got {1}", "id", o);
		builder.id = o;
	}
	if (args.hasOwnProperty("version")) {
		var o = args["version"];
		util.assert(util.isSomething(o), "''{0}'': must not be null or undefined", "version");
		util.assert(util.isNumber(o), "''{0}'': expected a number, got {1}", "version", o);
		util.assert(o > 0, "''{0}'': expected a number > 0, got {1}", "version", o);
		builder.version = o;
	}
		
	if (args.hasOwnProperty("pos")) {
		util.assert(! (args.hasOwnProperty("lat") || args.hasOwnProperty("lon")), "Can''t process both properties ''pos'' and ''lat''/''lon''");
		var o = args["pos"];		
		util.assert(util.isSomething(o), "''{0}'': must not be null or undefined", "pos");
		if (pos instanceof LatLon) {
			builder.lat = pos.lat();
			builder.lon = pos.lon();
		} else if (util.isArray(pos)) {
			util.assert(pos.length == 2, "''{0}'': expected exactly two numbers in array", "pos");
			try {
				builder.lat = checkLat(pos[0]);
			} catch(e) {
				util.assert(false, "''{0}'': {1}", "lat", e);
			}
			try {
				builder.lon = checkLon(pos[1]);
			} catch(e) {
				util.assert(false, "''{0}'': {1}", "lon", e);
			}
		} else if (util.isObject(pos)) {
			util.assert(pos.hasOwnProperty("lat"), "''{0}'': missing mandatory property ''lat''", "pos");
			util.assert(pos.hasOwnProperty("lon"), "''{0}'': missing mandatory property ''lon''", "pos");
			try {
				builder.lat = checkLat(pos["lat"]);
			} catch(e) {
				util.assert(false, "''{0}'': {1}", "pos", e);
			}
			try {
				builder.lon = checkLon(pos["lon"]);
			} catch(e) {
				util.assert(false, "''{0}'': {1}", "pos", e);
			}
		}
	}
	if (args.hasOwnProperty("lat")) {
		var o = args["lat"];
		util.assert(util.isSomething(o), "''{0}'': must not be null or undefined", "lat");
		util.assert(util.isNumber(o), "''{0}'': expected a number, got {1}", "lat", o);
		util.assert(LatLon.isValidLat(), "''{0}'': expected a valid latitude, got {1}", "lat", o);
		builder.lat = lat;
	}
	if (args.hasOwnProperty("lon")) {
		var o = args["lon"];
		util.assert(util.isSomething(o), "''{0}'': must not be null or undefined", "lon");
		util.assert(util.isNumber(o), "''{0}'': expected a number, got {1}", "lon", o);
		util.assert(LatLon.isValidLon(), "''{0}'': expected a valid longitude, got {1}", "lon", o);
		builder.lon = lon;
	}
	if (args.hasOwnProperty("tags")) {
		var o = args["tags"];
		util.assert(util.isSomething(o), "''{0}'': most not be null or undefined", "tags");
		rememberTags(builder, o);
	}
}

function create() {
	var builder = receiver(this);
	switch(arguments.length){
	case 1:
		var o = arguments[0];
		util.assert(util.isSomething(o), "Argument 0: must not be null or undefined");
		if (util.isNumber(o)) {
			util.assert(o > 0, "Argument 0: expected an id > 0, got {0}", o);
			builder.id = o;
		} else if (typeof o == "object") {
			initFromObject(builder, o);
		} else {
			util.assert(false, "Argument 0: unexpected type, got ''{0}''", o.getClass());
		}
		break;
		
	case 2: 
		var o = arguments[0];
		util.assert(util.isSomething(o), "Argument 0: must not be null or undefined");
		util.assert(util.isNumber(o), "Argument 0: expected a number, got {0}", o);
		util.assert(o > 0, "Argument 0: expected a number > 0, got {0}", o);
	    builder.id = o;
		var o = arguments[1];
		util.assert(util.isSomething(o), "Argument 1: must not be null or undefined");
		util.assert(util.isNumber(o), "Argument 1: expected a number, got {0}", o);
		util.assert(o > 0, "Argument 1: expected a number > 0, got {0}", o);
	    builder.version = o;
	}
	
	var node;
	if (util.isNumber(builder.id)) {
		if (util.isNumber(builder.version)){
			node = new Node(builder.id, builder.version);
		} else {
			node = new Node(builder.id, 1);		
		}
		var coor = new LatLon(builder.lat || 0, builder.lon || 0);
		node.setCoor(coor);
	} else {
		node = new Node(new LatLon(builder.lat || 0, builder.lon || 0));
	}
	assignTags(node, builder.tags || {});
	if (builder.ds) builder.ds.addPrimitive(node);
	return node;	
}
exports.NodeBuilder.create = create;
exports.NodeBuilder.prototype.create = create;

/**
 * <p>Creates a new <em>proxy</em> node. A proxy node is a node, for which we only know
 * its global id. In order to know more details (position, tags, etc.), we would have to
 * download it from the OSM server.</p>
 * 
 * <p>The method can be used in a static and in an instance context.</p>
 * 
 * @example
 * var nbuilder = require("josm/builder").NodeBuilder;
 * 
 * // a new proxy node for the global node with id 12345
 * var n1 = nbuilder.proxy(12345);  
 * 
 * @memberOf NodeBuilder
 * @method
 * @param {number} id  (mandatory) the node id (not null, number > 0 expected)
 * @return the new proxy node 
 * @type org.openstreetmap.josm.data.osm.Node
 */
function createProxy(id) {
	var builder = receiver(this);
	util.assert(util.isSomething(id), "Argument 0: must not be null or undefined");
	util.assert(util.isNumber(id), "Argument 0: expected a number, got {0}", id);
	util.assert(id > 0, "Argument 0: id > 0 expected, got {0}", id);
	
	var node = new Node(id);
	if (builder.ds)  builder.ds.addPrimitive(node);
	return node;
};
exports.NodeBuilder.prototype.createProxy = exports.NodeBuilder.createProxy = createProxy;


/**
 * <p>Declares the node position.</p>
 * 
 * <p>The method can be used in a static and in an instance context.</p>
 * 
 * @example
 * var nbuilder = require("josm/builder").NodeBuilder;
 * 
 * // a new global  node with the global id 12345 at position (34,45) 
 * var n1 = nbuilder.withPosition(34,45).global(12345);  
 * 
 * // a new local node at position (23.2, 87.33)
 * var n2 = nbuilder.withPosition(23.3,87.33).local();
 * 
 * @memberOf NodeBuilder
 * @param {Number} lat  (mandatory) the latitude. A number in the range [-90..90].
 * @param {Number} lon (mandatory) the longitude.  A number in the range [-90..90].
 * @return a node builder (for method chaining)
 * @type NodeBuilder
 */
function withPosition(lat, lon){
	var builder = receiver(this);
	util.assert(util.isNumber(lat), "Expected a number for lat, got {0}", lat);
	util.assert(util.isNumber(lon), "Expected a number for lon, got {0}", lon);
	util.assert(LatLon.isValidLat(lat), "Invalid lat, got {0}", lat);
	util.assert(LatLon.isValidLon(lon), "Invalid lon, got {0}", lon);
	builder.lat = lat;
	builder.lon = lon;
	return builder;
}
exports.NodeBuilder.prototype.withPosition = withPosition;
exports.NodeBuilder.withPosition = withPosition;

/**
 * <p>Declares the tags to be assigned to the new node.</p>
 * 
 * <p>The method can be used in a static and in an instance context.</p>
 * 
 * @example
 * var nbuilder = require("josm/builder").NodeBuilder;
 * // a new global  node with the global id 12345 and tags name=test and highway=road
 * var n1 = nbuilder.withTags({"name":"test", "highway":"road"}).global(12345);  
 * 
 * // a new local node tags name=test and highway=road
 * var tags = {
 *      "name"    : "test", 
 *      "highway" : "road"
 * };     
 * var n2 = nbuilder.withTags(tags).local();
 * 
 * @memberOf NodeBuilder
 * @param {Object} tags  (optional) the tags 
 * @return a node builder (for method chaining)
 * @name withTags
 * @type NodeBuilder
 */
function withTags(tags) {
	var builder = typeof this === "object" ? this : new exports.NodeBuilder();
	rememberTags(builder, tags);
	return builder;
};
exports.NodeBuilder.prototype.withTags = withTags;
exports.NodeBuilder.withTags = withTags;



/**
 * <p>Declares the global node id and the global node version.</p>
 * 
 * <p>The method can be used in a static and in an instance context.</p>
 * 
 * @memberOf NodeBuilder
 * @param {Number} id  (mandatory) the global node id. A number > 0.
 * @param {Number} version (optional) the global node version. If present, a number > 0. If missing,
 * the version 1 is assumed.  
 * @return a node builder (for method chaining)
 * @name withId
 */
function withId(id, version) {
	var builder = typeof this === "object" ? this : new exports.NodeBuilder();
	rememberId(builder, id, version);
	return builder;
};
exports.NodeBuilder.prototype.withId = withId;
exports.NodeBuilder.withId = withId;
}());


//-------------------------------------------------------------------------------
// WayBuilder
//--------------------------------------------------------------------------------
(function() {

var receiver = function(that) {
	return typeof that === "object" ? that : new exports.WayBuilder();
}
	
exports.WayBuilder = function(ds) {
	this.id = void 0;
	this.version = 0;
	this.tags = undef;
	this.nodes = [];
	this.ds = void(0);
	if (util.isSomething(ds)) {
		util.assert(ds instanceof DataSet, "Expected a DataSet, got {0}", ds);
		this.ds = ds; 
	}
};

function withId(id, version) {
	var builder = receiver(this);
	rememberId(builder, id, version);
	return builder;
};
exports.WayBuilder.prototype.withId = withId;
exports.WayBuilder.withId = withId;

function withTags(tags) {
	var builder = receiver(this);
	rememberTags(builder, tags);
	return builder; 
};
exports.WayBuilder.prototype.withTags = withTags;
exports.WayBuilder.withTags = withTags; 

/**
 * <p>Declares the nodes of the way.</p>
 * 
 * <p>Accepts either a vararg list of nodes, an array of nodes or a Java list 
 * of nodes. At least <strong>two non-identical nodes</strong> have to be supplied.
 * The same node can occure more than once in the list, but a consecutive sequence
 * of the same node is collapsed to one node.
 * </p>
 * 
 * <p>The method can be used in a static and in an instance context.</p>
 * 
 * @example
 * var wbuilder = require("josm/builder").WayBuilder;
 * var nbuilder = require("josm/builder").NodeBuilder;
 * // creates a new local way with two local nodes
 * var way = builder.withNodes(
 *    nbuilder.local(), nbuilder.local()
 * ).local();
 * 
 * @memberOf WayBuilder
 * @param {object...} nodes the list of nodes. See description and examples. 
 * @return a node builder (for method chaining)
 */
function withNodes() {
	var builder = receiver(this);
	var nodes;
	if (arguments.length == 1 && util.isArray(arguments[0])) {
		nodes = arguments[0];
	} else if (arguments.length > 1) {
		nodes = Array.prototype.slice.call(arguments,0);
	}
	var newnodes;
	var last;
	for (var i=0; i < nodes.length; i++) {		
		var n = nodes[i];
		if (util.isNothing(n)) continue;
		util.assert(n instanceof Node, "Expected instances of Node only, got {0}", n);
		if (last && last.getId() == n.getId()) continue; // skip sequence of identical nodes  
		newnodes.push(n);
		last = n;
	}
	builder.nodes = newnodes;
};
exports.WayBuilder.withNodes = exports.WayBuilder.prototype.withNodes = withNodes;



/**
 * <p>Creates a new <em>proxy</em> wawy. A proxy way is a way, for which we only know
 * its global id. In order to know more details (nodes, tags, etc.), we would have to
 * download it from the OSM server.</p>
 * 
 * <p>The method can be used in a static and in an instance context.</p>
 * 
 * @example
 * var wbuilder = require("josm/builder").WayBuilder;
 * 
 * // a new proxy way for the global way with id 12345
 * var w1 = wbuilder.proxy(12345);  
 * 
 * @memberOf WayBuilder
 * @method
 * @return the new proxy way 
 * @type org.openstreetmap.josm.data.osm.Way
 */
function proxy(id) {
	var builder = receiver(this);
	if (util.isDef(id)) {
		util.assert(util.isNumber(id) && id > 0, "Expected a number > 0, got {0}", id);
		builder.id = id;
	}
	util.assert(util.isNumber(builder.id), "Node id is not a number. Use .proxy(id) or .withId(id).proxy()");
	util.assert(builder.id > 0, "Expected way id > 0, got {0}", builder.id);
	var way = new Way(builder.id);
	if (builder.ds) builder.ds.addPrimitive(way);
	return way;
};
exports.WayBuilder.proxy = exports.WayBuilder.prototype.proxy = proxy;

/**
 * <p>Creates a new local way.</p>
 * 
 * <p>The method can be used in a static and in an instance context.</p>
 * 
 * @example
 * var wbuilder = require("josm/builder").WayBuilder;
 * var nbuilder = require("josm/builder").NodeBuilder;
 * // creates a new local way with two local nodes
 * var way = builder.withNodes(
 *    nbuilder.local(), nbuilder.local()
 * ).local();
 * 
 * @memberOf WayBuilder
 * @return the new way
 * @type org.openstreetmap.josm.data.osm.Way
 */
function local() {
	var builder = receiver(this);
	util.assert(arguments.length == 0, "No arguments expected, got {0} arguments", arguments.length);
	var way = new Way();
	assignWayAttributes(builder, way);
	if (builder.ds) builder.ds.addPrimitive(way);
	return way; 
};
exports.WayBuilder.local = exports.WayBuilder.prototype.local = local;
 

function assignWayAttributes(builder, way) {
	if (util.hasProperties(builder.tags)) {
		assignTags(way, builder.tags);
	}
	if (builder.nodes.length > 0) {
		way.setNodes(builder.nodes);
	}	
};

/**
 * <p>Creates a new global way.</p>
 * 
 * <p>The method can be used in a static and in an instance context.</p>
 * 
 * @example
 * var wbuilder = require("josm/builder").WayBuilder;
 * var nbuilder = require("josm/builder").NodeBuilder;
 * // creates a new local way with two local nodes
 * var way = builder.withNodes(
 *    nbuilder.local(), nbuilder.local()
 * ).global(12345);
 * 
 * @memberOf WayBuilder
 * @param {number} id  (optional) the node id. If supplied, must be a number > 0. If missing, the 
 *   the id must have been declared using <code>.withId(id,version)</code>
 * @param {number} version (optional) the global node version. If supplied, must be a number >0. If missing,
 *    1 is assumed, unless the version has been set using <code>.withId(id,version)</code>
 * @return the new way
 * @type org.openstreetmap.josm.data.osm.Way
 */
function global(id, version) {
	var builder = receiver(this);
	if (util.isDef(id)) {
		util.assert(util.isNumber(id) && id > 0, "Expected an id > 0, got {0}", id);
		builder.id = id;
	}
	if (util.isDef(version)) {
		util.assert(util.isNumber(version) && version > 0, "Expected a version > 0, got {0}", version);
		builder.version = version;
	}
	util.assert(util.isDef(builder.id) && builder.id > 0, "Way id not initialized properly. Use .withId(id,version) or .global(id,version). id is {0}", builder.id);
	util.assert(util.isDef(builder.version) && builder.version > 0, "Way version not initialized properly. Use .withId(id,version) or .global(id,version). version is {0}", builder.version);
	var way = new Way(builder.id, builder.version);
	assignWayAttributes(builder, way);
	if (builder.ds) builder.ds.addPrimitive(way);
	return way;
};
exports.WayBuilder.global = exports.WayBuilder.prototype.global = global;

}());
