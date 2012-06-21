(function(){
	
/**
 * <p>A collection of functions to create commands which can be applied, undone and redone on
 * {@class org.opoenstreetmap.josm.gui.layer.OsmDataLayer}s.</p>
 * 
 * @module josm/command
 */		
var util = require("josm/util");
var layers = require("josm/layers");
var OsmPrimitive = org.openstreetmap.josm.data.osm.OsmPrimitive;
var OsmDataLayer = org.openstreetmap.josm.gui.layer.OsmDataLayer;

function checkAndFlatten(primitives) {
	var HashSet = java.util.HashSet;
	var ret = new HashSet();
	function visit(value) {
		if (util.isNothing(value)) return;
		if (util.isCollection(value)) {
			util.each(value, visit);
		} else if (value instanceof OsmPrimitive) {
			ret.add(value);
		} else {
			util.assert(false, "Unexpected object to add as OSM primitive, got {0}", value);
		}
	};
	visit(primitives);
	return ret;
};

function applyTo(layer) {
	var OsmDataLayer = org.openstreetmap.josm.gui.layer.OsmDataLayer;
	util.assert(util.isSomething(layer), "layer: must not be null or undefined");
	util.assert(layer instanceof OsmDataLayer, "layer: expected OsmDataLayer, got {0}", layer);
	layer.apply(this);
};


function toArray(collection){
	if (util.isArray(collection)) return collection;
	if (collection instanceof java.util.Collection) {
		var ret = [];
		for(var it=collection.iterator();it.hasNext();) ret.push(it.next());
		return ret;
	}
};


/**
 * A command to add a collection of objects to a data layer.
 * 
 * @class
 * @name AddCommand
 * @memberOf josm/command
 * @param {java.util.Collection|array} objs the objects to add
 */
exports.AddCommand = function (objs) {
	util.assert(objs, "objs: mandatory parameter missing");
	this._objs = toArray(checkAndFlatten(objs));
};

/**
 * Applies the command to a layer.
 * 
 * @method
 * @instance
 * @name applyTo
 * @summary Applies the command to a layer.
 * @memberOf AddCommand
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 */
exports.AddCommand.prototype.applyTo = applyTo;

/**
 * Creates the internal JOSM command for this command
 * 
 * @method
 * @instance
 * @name createJOSMCommand
 * @summary Creates the internal JOSM command for this command
 * @memberOf AddCommand
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 * @type {org.openstreetmap.josm.command.Command}
 */
exports.AddCommand.prototype.createJOSMCommand = function(layer) {
	var OsmDataLayer = org.openstreetmap.josm.gui.layer.OsmDataLayer;
	var AddMultiCommand = org.openstreetmap.josm.plugins.scripting.js.api.AddMultiCommand;	
	util.assert(util.isSomething(layer), "layer: must not be null or undefined");
	util.assert(layer instanceof OsmDataLayer, "layer: expected OsmDataLayer, got {0}", layer);
	return new AddMultiCommand(layer, this._objs);	
};

/**
 * <p>Creates a command to add a collection of objects to a data layer.</p>
 * 
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><code class="signature">add(obj, obj, ...)</code> </dt>
 *   <dd><code>obj</code> are {@class org.openstreetmap.josm.data.osm.Node}s,
 *   {@class org.openstreetmap.josm.data.osm.Way}s, or
 *   {@class org.openstreetmap.josm.data.osm.Relations}s. Or javascript array
 *   or Java collections thereof.</dd>
 * </dl>
 * 
 * @example
 * var cmd = require("josm/command");
 * var layers = require("josm/layer");
 * var layer  = layers.get("Data Layer 1");
 * 
 * // add two nodes 
 * cmd.add(n1,n1).applyTo(layer);
 * 
 * // add an array of two nodes and a way  
 * layer.apply(
 *    cmd.add([n1,n2,w2])
 * );
 * 
 * @method
 * @name add
 * @summary Creates a command to add a collection of objects
 * @memberOf josm/command
 */
exports.add = function() {
	return new exports.AddCommand(checkAndFlatten(arguments));
};

/**
 * A command to delete a collection of objects in a data layer.
 * 
 * @class
 * @name DeleteCommand
 * @memberOf josm/command
 * @param {java.util.Collection|array} objs the objects to add 
 */
exports.DeleteCommand = function (objs) {
	this._objs = checkAndFlatten(objs);
};

/**
 * Applies the command to a layer.
 * 
 * @method
 * @instance
 * @name applyTo
 * @summary   Applies the command to a layer.
 * @memberOf DeleteCommand
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 */
exports.DeleteCommand.prototype.applyTo = applyTo;

/**
 * Creates the internal JOSM command for this command
 * 
 * @method
 * @instance
 * @name createJOSMCommand
 * @summary Creates the internal JOSM command for this command
 * @memberOf DeleteCommand
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 * @type {org.openstreetmap.josm.command.Command}
 */
exports.DeleteCommand.prototype.createJOSMCommand = function(layer) {
	var OsmDataLayer = org.openstreetmap.josm.gui.layer.OsmDataLayer;
	var DeleteCommand = org.openstreetmap.josm.command.DeleteCommand;	
	util.assert(util.isSomething(layer), "layer: must not be null or undefined");
	util.assert(layer instanceof OsmDataLayer, "layer: expected OsmDataLayer, got {0}", layer);
	return new DeleteCommand.delete(layer, this._objs, true /* alsoDeleteNodesInWay */, true /* silent */);	
};

/**
 * <p>Creates a command to delete a collection of objects in  a data layer.</p>
 * 
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><code class="signature">delete(obj,obj,..., ?options)</code> </dt>
 *   <dd><code>obj</code> are {@class org.openstreetmap.josm.data.osm.Node}s,
 *   {@class org.openstreetmap.josm.data.osm.Way}s, or
 *   {@class org.openstreetmap.josm.data.osm.Relations}s. Or javascript array
 *   or Java collections thereof.</dd>
 * </dl>
 * 
 * @example
 * var cmd = require("josm/command");
 * var layers= require("josm/layer");
 * var layer = layers.get("Data Layer 1");
 * 
 * // delete two nodes 
 * cmd.delete(n1,n1).applyTo(layer);
 * 
 * // delete an array of two nodes and a way  
 * layer.apply(
 *    cmd.delete([n1,n2,w2])
 * );
 * 
 * @method
 * @name delete
 * @summary Creates a command to delete a collection of objects
 * @memberOf josm/command
 */
exports.delete = function() {
	return new exports.DeleteCommand(checkAndFlatten(arguments));
};

function scheduleLatChangeFromPara(para, change) {
	var LatLon = org.openstreetmap.josm.data.coor.LatLon;	
	if (!para || ! util.isDef(para.lat)) return;
	util.assert(util.isNumber(para.lat), "lat: lat must be a number, got {0}", para.lat);
	util.assert(LatLon.isValidLat(para.lat), "lat: expected a valid lat, got {0}", para.lat);
	change.withLatChange(para.lat);
};

function scheduleLonChangeFromPara(para, change) {
	var LatLon = org.openstreetmap.josm.data.coor.LatLon;
	if (!para || ! util.isDef(para.lon)) return;
	util.assert(util.isNumber(para.lon), "lon: lon must be a number, got {0}", para.lon);
	util.assert(LatLon.isValidLon(para.lon), "lon: expected a valid lon, got {0}", para.lon);
	change.withLonChange(para.lon);
};

function schedulePosChangeFromPara(para, change) {
	var LatLon = org.openstreetmap.josm.data.coor.LatLon;
	if (!para || ! util.isDef(para.pos)) return;
	util.assert(para.pos, "pos must no be null");
	var pos = para.pos;
	if (pos instanceof LatLon) {
		// OK
	} else if (typeof pos === "object") {
		pos = LatLon.make(pos);
	} else {
		util.assert(false, "pos: unexpected value, expected LatLon or object, got {0}", pos);
	}
	change.withPosChange(pos);
};

function scheduleNodeChangeFromPara(para, change) {
	if (!para || ! util.isDef(para.nodes)) return;	
	change.withNodeChange(para.nodes);
};

function scheduleMemberChangeFromPara(para, change) {
	if (!para || ! util.isDef(para.members)) return;	
	change.withMemberChange(para.members);
};

function scheduleTagsChangeFromPara(para, change) {
	var Map = java.util.Map;
	var HashMap = java.util.HashMap;
	
	if (!para || ! util.isDef(para.tags)) return;
	util.assert(para.tags, "tags must no be null");
	var tags = para.tags;
	if (tags instanceof Map) {
		// OK
	} else if (typeof tags === "object") {
		var map = new HashMap();
		for (var key in tags) {
			if (!tags.hasOwnProperty(key)) continue;
			key = util.trim(key);
			value = tags[key];
			map.put(key,value);
		}
		tags = map;
	} else {
		util.assert(false, "tags: unexpected value, expected Map or object, got {0}", tags);
	}
	change.withTagsChange(tags);
};

function changeFromParameters(para) {
	var Change = org.openstreetmap.josm.plugins.scripting.js.api.Change;
	var change = new Change();
	scheduleLatChangeFromPara(para,change);
	scheduleLonChangeFromPara(para,change);
	schedulePosChangeFromPara(para,change);
	scheduleTagsChangeFromPara(para,change);
	scheduleNodeChangeFromPara(para, change);
	scheduleMemberChangeFromPara(para,change);
	return change;
};

/**
 * A command to change a collection of objects in a data layer.
 * 
 * @class
 * @name ChangeCommand
 * @memberOf josm/command
 * @param {java.util.Collection|array}  objs  the objects to change
 * @param {org.openstreetmap.josm.plugins.scripting.js.api.Change} change the change specification
 */
exports.ChangeCommand = function (objs, change) {
	this._objs = checkAndFlatten(objs);
	this._change = change;
};

/**
 * Applies the command to a layer.
 * 
 * @method
 * @instance
 * @name applyTo
 * @summary Applies the command to a layer.
 * @memberOf ChangeCommand
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 */
exports.ChangeCommand.prototype.applyTo = applyTo;

/**
 * Creates the internal JOSM command for this command
 * 
 * @method
 * @instance
 * @name createJOSMCommand
 * @summary Creates the internal JOSM command for this command
 * @memberOf ChangeCommand
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 * @type {org.openstreetmap.josm.command.Command}
 */
exports.ChangeCommand.prototype.createJOSMCommand = function(layer) {
	var OsmDataLayer = org.openstreetmap.josm.gui.layer.OsmDataLayer;
	var ChangeMultiCommand = org.openstreetmap.josm.plugins.scripting.js.api.ChangeMultiCommand;	
	util.assert(util.isSomething(layer), "layer: must not be null or undefined");
	util.assert(layer instanceof OsmDataLayer, "layer: expected OsmDataLayer, got {0}", layer);
	return new ChangeMultiCommand(layer, this._objs, this._change);	
};

/**
 * <p>Creates a command to change a collection of objects in  a data layer.</p>
 * 
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><code class="signature">change(obj,obj,..., options)</code> </dt>
 *   <dd><code>obj</code> are {@class org.openstreetmap.josm.data.osm.Node}s,
 *   {@class org.openstreetmap.josm.data.osm.Way}s, or
 *   {@class org.openstreetmap.josm.data.osm.Relations}s. Or javascript array
 *   or Java collections thereof.</dd>
 * </dl>
 * 
 * The mandatory last argument is an object with named parameters. It accepts the following
 * named parameters:
 * <dl>
 *   <dt><code class="signature">lat:number</code></dt>
 *   <dd>Changes the latitude of the target nodes to <code>lat</code>.</dd>
 *   
 *   <dt><code class="signature">lon:number</code></dt>
 *   <dd>Changes the longitude of the target nodes to <code>lon</code>.</dd>
 *   
 *   <dt><code class="signature">pos:{@class org.openstreetmap.josm.data.coor.LatLon}|object</code></dt>
 *   <dd>Changes the position of the target nodes to <code>pos</code>. pos is either a
 *   {@class org.openstreetmap.josm.data.coor.LatLon} or an object <code>{lat:..., lon:...}</code> 
 *   </dd>
 *   
 *   <dt><code class="signature">tags:{@class java.util.Map}|object</code></dt>
 *   <dd>Changes the tags of the target objects to <code>tags</code>.</dd>
 *   
 *   <dt><code class="signature">nodes:{@class java.util.List}|array</code></dt>
 *   <dd>Changes the nodes of the target way sto <code>nodes</code>.</dd>
 *   
 *   <dt><code class="signature">members:{@class java.util.List}|array</code></dt>
 *   <dd>Changes the nodes of the target relations to <code>members</code>.</dd>
 * </dl>
 * 
 * @example
 * var cmd = require("josm/command");
 * var layers = require("josm/layer");
 * var layer = layers.get("Data Layer 1");
 * 
 * // change the position of a node  
 * cmd.change(n1,n1, {lat: 123.45, lon: 44.234}).applyTo(layer);
 * 
 * // change the nodes of a way 
 * layer.apply(
 *    cmd.change(w2, {nodes: [n1,n2,3]})
 * );
 * 
 * // change the tags of a collection of primitives
 * cmd.change(n1,n3, w1,r1, {
 *    tags: {"mycustommtag": "value"}
 * }).applyTo(layer);
 * 
 * @method
 * @name change
 * @summary Creates a command to change a collection of objects
 * @static
 * @memberOf josm/command
 */
exports.change = function() {
	var objs = [];
	var change;
	switch(arguments.length) {
	case 0: 
		util.assert(false, "Unexpected number of arguments, got {0} arguments", arguments.length);
	default:
		var a = arguments[arguments.length -1];
		if (a instanceof OsmPrimitive) {
			util.assert(false, "Argument {0}: unexpected last argument, expected named parameters, got {0}", a);
		} else if (typeof a === "object") {
			// last argument is an object with named parameters
			objs = Array.prototype.slice.call(arguments,0,-1);
			change = changeFromParameters(a);
		} else {
			util.assert(false, "Argument {0}: unexpected type of value, got {1}", arguments.length -1, a);
		}
		break;
	}
	
	var tochange = checkAndFlatten(objs);
	return new exports.ChangeCommand(tochange, change);
};
	
}());

