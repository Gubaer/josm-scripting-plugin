/**
 * <p>Additional properties and functions for JOSMs internal class 
 * {@link http://josm.openstreetmap.de/browser/josm/trunk/src/org/openstreetmap/josm/data/osm/DataSet.java|DatSet}.</p> 
 * 
 * <ul>
 *   <li>[mixin DataSetMixin]{@link DataSetMixin}</li>
 *   <li>[class DataSetSelectionFacade]{@link DataSetSelectionFacade}</li>
 * </ul>
 *
 * This module is auto-loaded. 
 * 
 * @module josm/mixin/DataSetMixin
 */
var util = require("josm/util");
var NodeBuilder = require("josm/builder").NodeBuilder;
var WayBuilder  = require("josm/builder").WayBuilder;
var RelationBuilder = require("josm/builder").RelationBuilder;


var OsmPrimitive = org.openstreetmap.josm.data.osm.OsmPrimitive;
var Node         = org.openstreetmap.josm.data.osm.Node;
var Way          = org.openstreetmap.josm.data.osm.Way;
var Relation     = org.openstreetmap.josm.data.osm.Relation;
var PrimitiveId     = org.openstreetmap.josm.data.osm.PrimitiveId;
var SimplePrimitiveId     = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
var OsmPrimitiveType     = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
var DataSet      = org.openstreetmap.josm.data.osm.DataSet;
var Collection   = java.util.Collection;
var HashMap   = java.util.HashMap;
var Collections   = java.util.Collections;

/**
 * <p>DataSetMixin provides additional properties and methods which you can invoke on an instance of
 * {@link http://josm.openstreetmap.de/browser/josm/trunk/src/org/openstreetmap/josm/data/osm/DataSet.java|DatSet}. 
 * The native methods of DataSet are still available for scripting. Prepend their name with '$' if they are
 * hidden by a property or method in this mixin.</p>
 * 
 *  @example 
 *  var DataSet = org.openstreetmap.josm.data.osm.DataSet;
 *  var wbuilder = require("josm/builder").WayBuilder;
 *  var out = java.lang.System.out;
 *  var ds = new DataSet();
 *  var n1 = ds.nodeBuilder.create();
 *  var n2 = ds.nodeBuilder.create();
 *  var w1 = wbuilder.create(1234, {nodes: [n1,n2]});
 *  
 *  // access and manipulate the objects in the dataset
 *  ds.add(w1);
 *  ds.has(w1);   // -> true
 *  ds.has(n1);   // -> true 
 * 
 *  // access and manipulate the selected objects 
 *  ds.selection.add(n2); 
 *  ds.selection.isSelected(n2);  // -> true
 *  ds.selection.toogle(n2); 
 *  
 * @mixin DataSetMixin 
 */
var mixin = {};


function each(collection, delegate) {
	if (util.isArray(collection) || util.isArguments(collection)) {
		for(var i=0; i<collection.length;i++) delegate(collection[i]);
	} else if (collection instanceof Collection){
		for(var it=collection.iterator(); it.hasNext();) delegate(it.next());
	} else {
		util.assert(false, "Expected list or collection, got {0}", collection);
	}
}

function isCollection(collection){
	return util.isArray(collection) || util.isArguments(collection) || collection instanceof Collection;
}

function colToArray(col) {
	var ret = [];
	for(var it=col.iterator(); it.hasNext();) ret.push(it.next());
	return ret;
}

/**
 * <p>Adds objects to the dataset.</p>
 * 
 * <strong>Signatures</strong>
 * <dl> 
 *   <dt><code>add(o1,o2, ...)</code></dt>
 *   <dd>Adds a variable number of objects. null or undefined are ignored. An object is either an
 *   instance of Node, Way, or Relation.</dd>
 *   
 *   <dt><code>add(array|collection)</code></dt>
 *   <dd>Adds an  javascript array or a java collection of objects. null or undefined are ignored. 
 *   A list element is an instance of Node, a Way, or Relation. null and undefined are
 *   ignored.</dd>
 * </dl>
 * 
 * @example
 * var DataSet = org.openstreetmap.josm.data.osm.DataSet;
 * var rbuilder = require("josm/builder").RelationBuilder;
 * var nbuilder = require("josm/builder").NodeBuilder;
 * var wbuilder = require("josm/builder").WayBuilder;
 * 
 * // add two nodes and a way to the dataset 
 * ds.add(
 *    var n1 = nb.create(),
 *    var n2 = nb.create(),
 *    wb.withNodes(n1,n2).create()
 * );
 * 
 * // add a array of objects to the dataset 
 * var l = [nb.create(1), nb.create(2), wb.create()];
 * relation.add(l);
 * 
 * @memberOf DataSetMixin
 * @method
 * @instance
 * @name add
 */
mixin.add = function() {
	var objs = [];
	function remember(obj){
		if(util.isNothing(obj)) return;
		if (obj instanceof OsmPrimitive) {
			objs.push(obj);
		} else if (isCollection(obj)) {
			each(obj, function(that){remember(that);});
		} else {
			util.assert(false, "Can''t add object {0} to a dataset", obj);
		}
	}
	remember(arguments);
	var ds = this;
	this.batch(function() {
		each(objs, function(obj) {
			ds.addPrimitive(obj);
		});		
	});
};

function normalizeId(id) {
	util.assert(util.isSomething(id), "id: must not be null or undefined");
	util.assert(util.isNumber(id), "id: expected a number, got {0}", id);
	util.assert(id != 0, "id: must not be 0", id);
	return id;
};

function normalizeType(type) {
	util.assert(util.isSomething(type), "type: must not be null or undefined");
	if (util.isString(type)) {
		type = util.trim(type).toLowerCase();
		try {
			return OsmPrimitiveType.fromApiTypeName(type);
		} catch(e) {
			util.assert(false, "type: unsupported OSM primitive type ''{0}''", type);
		}
	} else if (type instanceof OsmPrimitiveType) {
		util.assert(type == OsmPrimitiveType.NODE || type == OsmPrimitiveType.WAY || type == OsmPrimitiveType.RELATION, "type: unsupported OSM primitive type, got {0}", type);
		return type;
	} else {
		util.assert(false, "type: unsupported value, got {0}", type);
	}
};

function primitiveIdFromObject(obj) {
	util.assert(util.isDef(obj.id), "missing mandatory property ''{0}'' in {1}", "id", obj);
	util.assert(util.isDef(obj.type), "missing mandatory property ''{0}'' in {1}", "type", obj);
	var id = normalizeId(obj.id);
	var type = normalizeType(obj.type);
	return new SimplePrimitiveId(id, type);
};

/**
 * <p>Replies an OSM object from the dataset, or undefined, if no such object exists.</p>
 * 
 * <strong>Signatures</strong>
 * <dl> 
 *   <dt><strong>get(id, type)</strong></dt>
 *   <dd>Replies an object given by its unique numeric ID (nid) and a type. The type is either a string 
 *   "node", "way", or "relation", or one of the symbols OsmPrimitiveType.NODE, OsmPrimitiveType.WAY, or
 *   OsmPrimitiveType.RELATIOn.</dd>
 *   
 *   <dt><strong>get(id)</strong></dt>
 *   <dd>Replies an object given an ID. <var>id</var> is either an instance of PrimitiveId or an object with 
 *   the properties <var>id</var> and <var>type</var>, i.e. <code>{id: 1234, type: "node"}</code>.</dd>
 * </dl>
 * 
 * @example
 * var DataSet = org.openstreetmap.josm.data.osm.DataSet;
 * var SimplePrimitiveId = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
 * var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
 * var rbuilder = require("josm/builder").RelationBuilder;
 * var nbuilder = require("josm/builder").NodeBuilder;
 * var wbuilder = require("josm/builder").WayBuilder;
 * 
 * // get a node with a global id 
 * var o1  = ds.get(1234, "node");
 * 
 * // get a way with a global id 
 * var o2 =  ds.get(3333, OsmPrimitiveType.WAY);
 * 
 * // get a relation with an id object  
 * var o3 = ds.get({id: 5423, type: "relation"});
 * 
 * // pass in a SimplePrimitiveId
 * var id = new SimplePrimitiveId(-5, OsmPrimitiveType.NODE);
 * var o4 = ds.get(id);
 * 
 * // pass in a primitive to get it
 * var way = wbuilder.create(987);
 * var o5 = ds.get(way);
 * 
 * @memberOf DataSetMixin
 * @method
 * @instance
 * @name get
 */
mixin.get = function() {	
	var args = Array.prototype.slice.call(arguments,0);
	
	function get_1(ds) {
		var id = args[0];
		util.assert(util.isSomething(id), "id: must not be null or undefined");
		if (id instanceof PrimitiveId) {
			var obj = ds.getPrimitiveById(id);
			return obj == null ? undefined : obj;
		} else if (typeof id === "object") {
			return primitiveIdFromObject(id);
		} else {
			util.assert(false, "id: unexpected value, got {0}", id);
		}		
	};
	
	function get_2(ds) {
		var id = normalizeId(args[0]);
		var type = normalizeType(args[1]);
		var osmId = new SimplePrimitiveId(id, type);
		var obj = ds.getPrimitiveById(osmId);
		return obj == null ? undefined : obj;
	};
	
	switch(arguments.length){
	case 0:  util.assert(false, "Expected 1 or 2 arguments, got none");
	case 1:  return get_1(this);
	case 2:  return get_2(this);
	default: util.assert(false, "Expected 1 or 2 arguments, got {0}", args.length);
	}	
};

/**
 * <p>Run a sequence of operations against the dataset in "batch mode". Listeners to
 * data set events are only notified at the end of the batch.</p>
 * 
 * @example
 * var DataSet = org.openstreetmap.josm.data.osm.DataSet;
 * var ds = new DataSet();
 * ds.batch(function() {
 *    var n1 = ds.nodeBuilder.create();
 *    var n2 = ds.nodeBuilder.create();
 *    ds.wayBuilder.withNodes(n1,n2).create();    
 * }); 
 * 
 * @param {function} delegate  the function implementing the batch processes. Ignored if null or undefined.
 * @memberOf DataSetMixin
 * @method
 * @instance
 * @name batch  
 */
mixin.batch = function(delegate) {
	if (util.isNothing(delegate)) return;
	util.assert(util.isFunction(delegate), "delegate: expected a function, got {0}", delegate);
	try {
		this.beginUpdate();
		delegate();
	} finally {
		this.endUpdate();
	}
};

function normalizeIds(ids) {
	function walk(set, ids) {
		if (util.isNothing(ids)) return;
		if (ids instanceof PrimitiveId) {
			set.add(ids);			
		} else if (isCollection(ids)) {
			each(ids, function(that) {
				walk(set, that);
			});
		} else if (typeof ids === "object") {
			set.add(primitiveIdFromObject(ids));
		} else {
			util.assert(false, "Can''t select an object described with the id {0}", ids);
		}
	}
	var set = new HashSet();
	walk(set, ids);
	return set;
};

function normalizeObjId(id, type){
	id = normalizeId(id);
	type = normalizeType(type);
	return new SimplePrimitiveId(id, type);
};

/**
 * <p>Removes objects from the dataset.</p>
 * 
 * <strong>Signatures</strong>
 * <dl> 
 *   <dt><strong>remove(id, type)</strong></dt>
 *   <dd>Removes a single object given by its unique numeric ID (nid) and a type. The type is either a string 
 *   "node", "way", or "relation", or one of the symbols OsmPrimitiveType.NODE, OsmPrimitiveType.WAY, or
 *   OsmPrimitiveType.RELATION.</dd>
 *   
 *   <dt><strong>remove(id, id, ...)</strong></dt>
 *   <dd>Removes a collection of objects given by the ids. <var>id</var> is either an instance of PrimitiveId or an object with 
 *   the properties <var>id</var> and <var>type</var>, i.e. <code>{id: 1234, type: "node"}</code>.
 *   null and undefined are ignored.</dd>
 *   
 *   <dt><strong>remove(array|collection)</strong></dt>
 *   <dd>Removes a collection of objects given by the an array ora java.util.Collection of ids. 
 *   The collection elemeents are either instances of PrimitiveId or an object with 
 *   the properties <var>id</var> and <var>type</var>, i.e. <code>{id: 1234, type: "node"}</code>.
 *   null or undefined elements are ignored.
 *   </dd>
 * </dl>
 * 
 * @example
 * var DataSet = org.openstreetmap.josm.data.osm.DataSet;
 * var SimplePrimitiveId = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
 * var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
 * var rbuilder = require("josm/builder").RelationBuilder;
 * var nbuilder = require("josm/builder").NodeBuilder;
 * var wbuilder = require("josm/builder").WayBuilder;
 * 
 * // get a node with a global id 
 * ds.remove(1234, "node");
 * 
 * // remove a node and a way 
 * var id1 = new SimplePrimitiveId(1234, "node")
 * var id2 = new SimplePrimitiveId(3333, OsmPrimitiveType.WAY)
 * ds.remove(id1, id2);
 * 
 * // remove a relation and a node
 * ds.remove({id: 1234, type: "relation"}, id1);
 * 
 * // remove an array of nodes
 * ds.remove([id1,id2]);
 *  
 * // remove a set of objects 
 * var ids = new HashSet();
 * ids.add(id1); ids.add(id1);
 * ds.remove(ids);
 * 
 * @memberOf DataSetMixin
 * @method
 * @instance
 * @name remove
 */
mixin.remove = function() {
	var ids;
	if (arguments.length == 2 && util.isNumber(arguments[0])){
		// handling remove(id, type)
		var id = normalizeId(arguments[0]);
		var type = normalizeType(arguments[1]);
		ids = [new SimplePrimitiveId(id, type)];
	} else {
		// handling remove(id, id, id, ...) and remove(array|collection)
		ids = normalizeIds(arguments);
	}
	var ds = this;
	this.batch(function() {
		each(ids, function(id) {
			ds.removePrimitive(id);
		});
	});
};

/**
 * <p>Replies true, if the dataset contains an object.</p>
 * 
 * <strong>Signatures</strong>
 * <dl> 
 *   <dt><strong>has(id, type)</strong></dt>
 *   <dd>Replies true, if an object given by its unique numeric ID (nid) and a type is in the dataset. 
 *    The type is either a string 
 *   "node", "way", or "relation", or one of the symbols OsmPrimitiveType.NODE, OsmPrimitiveType.WAY, or
 *   OsmPrimitiveType.RELATION.</dd>
 *   
 *   <dt><strong>has(id)</strong></dt>
 *   <dd>Replies true, if an object with the id <var>id</var> exists in the dataset. <var>id</var> is either 
 *   an instance of PrimitiveId or an object with 
 *   the properties <var>id</var> and <var>type</var>, i.e. <code>{id: 1234, type: "node"}</code>.</dd>
 * </dl>
 * 
 * @example
 * var DataSet = org.openstreetmap.josm.data.osm.DataSet;
 * var SimplePrimitiveId = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
 * var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
 * var rbuilder = require("josm/builder").RelationBuilder;
 * var nbuilder = require("josm/builder").NodeBuilder;
 * var wbuilder = require("josm/builder").WayBuilder;
 * 
 * // is there a node with id 1234 ?  
 * var ret = ds.has(1234, "node");
 * 
 * // is there a way  with id 3333 ? 
 * ret =  ds.has(3333, OsmPrimitiveType.WAY);
 * 
 *  // is there a relation  with id 5433 ? 
 * ret = ds.has({id: 5423, type: "relation"});
 * 
 * // is there a node  with id -5 ? 
 * var id = new SimplePrimitiveId(-5, OsmPrimitiveType.NODE);
 * ret = ds.has(id);
 * 
 * // does it contain the way w?
 * var way = wbuilder.create(987);
 * ret = ds.has(way);
 * 
 * @memberOf DataSetMixin
 * @method
 * @instance
 * @name has
 */
mixin.has = function() {
	return mixin.get.apply(this, Array.prototype.slice.call(arguments)) !== undefined;
};

/**
 * Replies a node builder to create nodes in this dataset.
 * 
 * @example
 * var ds = new DataSet();
 * var n = ds.nodeBuilder.withId(1234).withTags({amenity: "restaurant"}).create();
 * ds.has(n);  // --> true
 * 
 * @memberOf DataSetMixin
 * @name nodeBuilder
 * @field
 * @type NodeBuilder
 * @instance
 * @readOnly
 */
mixin.nodeBuilder = {
	get: function() {
		return NodeBuilder.forDataSet(this);
	}	
};

/**
 * Replies a way builder to create ways in this dataset.
 * 
 * @example
 * var ds = new DataSet();
 * var w = ds.wayBuilder.create(1234, {tags: {highway: "residential"}});
 * ds.has(w);  // --> true
 * 
 * @memberOf DataSetMixin
 * @name wayBuilder
 * @field
 * @type WayBuilder
 * @instance
 * @readOnly
 */
mixin.wayBuilder = {
	get: function() {
		return WayBuilder.forDataSet(this);
	}
};

/**
 * Replies a relation builder to create relations in this dataset.
 * 
 * @example
 * var ds = new DataSet();
 * var r = ds.relationBuilder.withId(8765).create({tags: {type: "network"}});
 * ds.has(r);  // --> true
 * 
 * @memberOf DataSetMixin
 * @name relationBuilder
 * @field
 * @type RelationBuilder
 * @instance
 * @readOnly
 */
mixin.relationBuilder = {
	get: function() {
		return RelationBuilder.forDataSet(this);
	}
};

/**
 * Replies the dataset selection object.
 * 
 * @memberOf DataSetMixin
 * @name selection
 * @field
 * @type DataSetSelectionFacade
 * @instance
 * @readOnly
 */
mixin.selection = {
   get: function() {
	   return new DataSetSelectionFacade(this);
   }
};

/**
 * Facade to access and manipulate the selected objects in a dataset.
 * 
 * @class DataSetSelectionFacade
 * @param {org.openstreetmap.josm.data.osm.DataSet}  ds  the dataset. Must not be null or undefined.
 */
function DataSetSelectionFacade(ds) {
	util.assert(util.isSomething(ds), "ds: must not be null or undefined");
	util.assert(ds instanceof DataSet, "ds: expected a DataSet, got {0}", ds);
	this._ds = ds;	
};

/**
 * <p>Set the selected objects as selected.</p>
 * 
* <strong>Signatures</strong>
 * <dl> 
 *   <dt><strong>set(id,id, ...)</strong></dt>
 *   <dd>Selects a variable number of objects given by their ids.</dd>
 *   
 *   <dt><strong>set(array|collection)</strong></dt>
 *   <dd>Select a variable number of objects given by an array or a java collection of ids.</dd>
 * </dl>
 * 
 * @memberOf DataSetSelectionFacade
 * @method
 * @instance
 * @name set
 */
DataSetSelectionFacade.prototype.set = function() {
	if (arguments.length == 2 && util.isNumber(arguments[0])) {
		this._ds.setSelected(Collections.singleton(normalizeObjId.apply(null, arguments)));
	} else {
		var ids = normalizeIds(arguments);
		if (ids.length == 0){
			this._ds.clearSelection();
		} else {
			this._ds.setSelected(ids);
		}
	}
};

/**
 * <p>Adds selected objects.</p>
 * 
 * <strong>Signatures</strong>
 * <dl> 
 *   <dt><strong>add(id,id, ...)</strong></dt>
 *   <dd>Selects a variable number of objects given by their ids.</dd>
 *   
 *   <dt><strong>add(array|collection)</strong></dt>
 *   <dd>Select a variable number of objects given by an array or a java collection of ids.</dd>
 * </dl>
 * 
 * @memberOf DataSetSelectionFacade
 * @method
 * @instance
 * @name add
 */
DataSetSelectionFacade.prototype.add = function() {
	if (arguments.length == 2 && util.isNumber(arguments[0])) {
		this._ds.addSelected(Collections.singleton(normalizeObjId.apply(null, arguments)));
	} else {
		this._ds.addSelected(normalizeIds(arguments));	
	}
};

/**
 * <p>Unselects a collection of objects.</p>
 * 
 * <strong>Signatures</strong>
 * <dl> 
 *   <dt><strong>clear(id,id, ...)</strong></dt>
 *   <dd>Unselect a variable number of objects given by their ids.</dd>
 *   
 *   <dt><strong>clear(array|collection)</strong></dt>
 *   <dd>Unselect a variable number of objects given by an array or a java collection of ids.</dd>
 * </dl>
 * 
 * @memberOf DataSetSelectionFacade
 * @method
 * @instance
 * @name clear
 */
DataSetSelectionFacade.prototype.clear = function() {
	if (arguments.length == 2 && util.isNumber(arguments[0])) {
		this._ds.clearSelection(Collections.singleton(normalizeObjId.apply(null, arguments)));
	} else {
		var ids = normalizeIds(arguments);
		if (ids.length == 0) return;
		this._ds.clearSelection(ids);	
	}
};

/**
 * <p>Clear the selection.</p>
 * 
 * @memberOf DataSetSelectionFacade
 * @method
 * @instance
 * @name clearAll
 */
DataSetSelectionFacade.prototype.clearAll = function() {
	this._ds.clearSelection();
};

/**
 * <p>Toggle the selecction state of a collection of objects.</p>
 * 
 * <strong>Signatures</strong>
 * <dl> 
 *   <dt><strong>toggle(id,id, ...)</strong></dt>
 *   <dd>Toggle the selection state of a variable number of objects given by their ids.</dd>
 *   
 *   <dt><strong>toggle(array|collection)</strong></dt>
 *   <dd>Toggle the selection state of  variable number of objects given by an array or a java collection of ids.</dd>
 * </dl>
 * 
 * @memberOf DataSetSelectionFacade
 * @method
 * @instance
 * @name toggle
 */
DataSetSelectionFacade.prototype.toggle = function() {
	if (arguments.length == 2 && util.isNumber(arguments[0])) {
		this._ds.toggleSelected(Collections.singleton(normalizeObjId.apply(null, arguments)));
	} else {
		this._ds.toggleSelected(normalizeIds(arguments));	
	}
};

/**
 * <p>Replies true, if an object is currently selected.</p>
 * 
 * <strong>Signatures</strong>
 * <dl> 
 *   <dt><strong>isSelected(id, type)</strong></dt>
 *   <dd>Replies true, if the object with numeric id <var>id</var> and type <var>type</var> is selected.</dd>
 *
 *   <dt><strong>isSelected(id)</strong></dt>
 *   <dd>Replies true, if the object with id <var>id</var> is selected. <var>id</var> is either an instance of PrimitiveId or an object with 
 *   the properties <var>id</var> and <var>type</var>, i.e. <code>{id: 1234, type: "node"}</code></dd>
 *
 *   <dt><strong>isSelected(obj)</strong></dt>
 *   <dd>Replies true, if the object  <var>obj</var> is selected. obj is either a Node, a Way or a Relation.</dd>
 * </dl>
 * 
 * @memberOf DataSetSelectionFacade
 * @method
 * @instance
 * @name isSelected
 */
DataSetSelectionFacade.prototype.isSelected = function() {
	var args = Array.prototype.slice.call(arguments,0);
	function isSelected_1(ds) {
		var id = args[0];
		util.assert(util.isSomething(id), "id: must not be null or undefined");
		if (id instanceof PrimitiveId) {
			var obj = ds.getPrimitiveById(id);
			return obj == null ? false : ds.isSelected(obj);
		} else if (id instanceof OsmPrimitive) {
			return ds.isSelected(obj);
		} else if (typeof id === "object") {
			var obj = ds.getPrimitiveById(primitiveIdFromObject(id));
			return obj == null ? false : ds.isSelected(obj);
		} else {
			util.assert(false, "id: unexpected value, got {0}", id);
		}	
	};
	
	function isSelected_2(ds) {
		var obj = ds.getPrimitiveById(
			new SimplePrimitiveId(
				normalizeId(args[0]), 
				normalizeType(args[1])
			)
		);
		return obj == null ? false : ds.isSelected(obj);
	};
	
	switch(arguments.length){
	case 0:  util.assert(false, "Expected 1 or 2 arguments, got none");
	case 1:  return isSelected_1(this._ds);
	case 2:  return isSelected_2(this._ds);
	default: util.assert(false, "Expected 1 or 2 arguments, got {0}", args.length);
	}	
};
DataSetSelectionFacade.prototype.has = DataSetSelectionFacade.prototype.isSelected;

/**
 * Replies an array with the selected nodes.
 * 
 * @memberOf DataSetSelectionFacade
 * @name nodes
 * @field
 * @type {array}
 * @instance
 * @readOnly
 */
Object.defineProperty(DataSetSelectionFacade.prototype, "nodes", {
	get: function() {
		return colToArray(this.getSelectedNodes());
	}
});

/**
 * Replies an array with the selected ways.
 * 
 * @memberOf DataSetSelectionFacade
 * @name ways
 * @field
 * @type {array}
 * @instance
 * @readOnly
 */
Object.defineProperty(DataSetSelectionFacade.prototype, "ways", {
	get: function() {
		return colToArray(this.getSelectedWays());
	}
});

/**
 * Replies an array with the selected relations.
 * 
 * @memberOf DataSetSelectionFacade
 * @name relations
 * @field
 * @type {array}
 * @instance
 * @readOnly
 */
Object.defineProperty(DataSetSelectionFacade.prototype, "relations", {
	get: function() {
		return colToArray(this.getSelectedRelations());
	}
});

/**
 * Replies an array with the selected objects.
 * 
 * @memberOf DataSetSelectionFacade
 * @name relations
 * @field
 * @type {array}
 * @instance
 * @readOnly
 */
Object.defineProperty(DataSetSelectionFacade.prototype, "objects", {
	get: function() {
		return colToArray(this.getSelected());
	}
});

exports.forClass = org.openstreetmap.josm.data.osm.DataSet;
exports.mixin    = mixin;