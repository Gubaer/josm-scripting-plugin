/**
 * <p>Provides access to the JOSM layers.</p>
 * 
 * @module josm/layers
 * 
 */

//-- imports 
var Main = org.openstreetmap.josm.Main;
var OsmDataLayer = org.openstreetmap.josm.gui.layer.OsmDataLayer;	
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var Layer = org.openstreetmap.josm.gui.layer.Layer;
var util = require("josm/util");
	
var mv = function() {
	if (Main.map == null) return null;
	return Main.map.mapView;	
};

/**
 * Replies the number of currently open layers. 
 * 
 * @name length
 * @memberOf export.layers
 * @type {Number}
 */ 
Object.defineProperty(exports, "length", {
	get: function() {
		return mv() == null? 0 : mv().getNumLayers();
	}
});

function getLayerByName(key) {
	var undefined;
	key = util.trim(key).toLowerCase();
	if (exports.length == 0) return undefined;
	var layers = mv().getAllLayersAsList();
	for(var it=mv().getAllLayersAsList().iterator(); it.hasNext();) {
		var l = it.next();
		if (l.getName().trim().toLowerCase().equals(key)) return l; 
	}
	return undefined;
}

function getLayerByIndex(idx) {
	var undefined;
	if (idx < 0 || idx >= exports.length) return undefined;
	return mv().getAllLayersAsList().get(idx);
}

/**
 * <p>Replies one of the layers given a key.</p>
 * <ul>
 *   <li>If <code>key</code> is a <em>Number</em>, replies the layer with index key, or
 *   undefined, if no layer for this index exists.</li>
 *    <li>If <code>key</code> is a <em>String</em>, replies the first layer whose name is identical to key (
 *    case insensitive, without leading/trailing whitespace), or undefined, if no layer with such a name exists.</li>
 * <ul>
 * 
 * @example
 * var layers = require("josm/layers");
 * 
 * // get the first layer  
 * var layer1  = layers.get(0);
 * 
 * // get the first layer with name "data layer"
 * var layer2 = layers.get("data layer"); 
 * 
 * 
 * @param key {number|string}  the key to retrieve the layer 
 */
exports.get = function(key) {
	var undefined;
	if (util.isNothing(key)) return undefined;
	if (util.isString(key)) return getLayerByName(key);
	if (util.isNumber(key)) return getLayerByIndex(key);
	return undefined;
};

/**
 * <p>Checks whether <var>layer</var> is a currently registered layer.</p>
 * 
 * @example
 * var layers = require("josm/layers");
 * 
 * // is there a layer with name "my layer"? 
 * var b = layers.has("my layer");
 * 
 * // is there a layer at index position 2
 * b = layers.has(2);
 *
 * // is there a specific layer? 
 * var l = layers.get(0); 
 * b = layers.has(l);
 * 
 * @param {org.openstreetmap.josm.gui.layer.Layer|string|number} layer  a layer, a layer name, or a layer index 
 * @return true, if the layer or at least one layer with the given name exists. False, otherwise. 
 * @type boolean 
 * 
 */
exports.has = function(layer) {
	if (util.isNothing(layer)) return false;
	if (layer instanceof Layer) {
		return mv().hasLayer(layer);
	} else if (util.isString(layer)) {
		return util.isSomething(exports.get(layer));
	} else if (util.isNumber(layer)) { 
		return layer >= 0 && layer < exports.length;
	} else {
		return false; 
	}
};

/**
 * <p>Adds a layer.</p>
 * 
 * @example
 * var layers = require("josm/layers");
 * var OsmDataLayer = org.openstreetmap.josm.gui.layer.OsmDataLayer;	
 * var DataSet = org.openstreetmap.josm.data.osm.DataSet;
 *
 * var dataLayer = new OsmDataLayer(new DataSet(), null, null);
 * // add a layer 
 * layers.add(dataLayer);
 * 
 * @param layer {org.openstreetmap.josm.gui.layer.Layer}  a layer to add. Ignored if null or undefined.
 */
exports.add = function(layer) {
	if (util.isNothing(layer)) return;
	util.assert(layer instanceof Layer, "Expected an instance of Layer, got {0}", layer);
	Main.main.addLayer(layer);
};

var removeLayerByIndex = function(idx) {
	var layer = exports.get(idx);
	if (util.isNothing(layer)) return;
	Main.main.removeLayer(layer);
};

var removeLayerByName = function(name) {
	var layer = exports.get(name);
	if (util.isNothing(layer)) return;
	Main.main.removeLayer(layer);
};

/**
 * <p>Removes a layer with the given key.</p>
 * <ul>
 *   <li>If <var>key</var> is a <var>Number</var>, removes the layer with the index key. If the index doesn't
 *   isn't a valid layer index, nothing is removed.</li>
 *   <li>If <var>key</var> is a <var>string</var>, removes the layer with the name <var>key</var>. Leading 
 *   and trailing white space is removed, matching is a case-insensitive sub-string match.</li>
   
 * <ul>
 * 
 * @example
 * var layers = require("josm/layers");
 * 
 * // remove the first layer 
 * layers.remove(0);
 * 
 * // remove the first layer matching with the supplied name
 * layers.remove("myLayerName");   
 * 
 * @param key {number|string} indicates the layer to remove 
 */
exports.remove = function(key) {
	if (util.isNothing(key)) return;
	if (util.isNumber(key)) {
		removeLayerByIndex(key);
		return;
	} else if (util.isString(key)) {
		removeLayerByName(key);
		return;
	} else {
		util.assert(false, "Expected a number or a string, got {0}", key);
	}
};

/**
 * <p>Creates and adds a new data layer. The new layer becomes the new edit layer.</p>
 *
 * @example
 * // creates a new data layer 
 * var layer = josm.layers.addDataLayer();
 * 
 * // creates a new data layer with name 'test'
 * layer = josm.layers.addDataLayer("test");
 * 
 * @param {string} name (optional) the name of the layer. If missing, null or undefined, automatically
 * creates a name. 
 * @return {org.openstreetmap.josm.gui.layer.OsmDataLayer}  the new data layer 
 */
exports.addDataLayer = function() {
	var name;
	switch(arguments.length){
	case 0: 
		name = OsmDataLayer.createNewName();
		break;
	case 1: 
		name = arguments[0];
		if (util.isNothing(name)) {
			name = OsmDataLayer.createNewName();
		} else {
			name = util.trim(name + "");
		}
		break;
		
	 default:
		 util.assert(false, "Too many arguments. Expected 0 or 1, got {0}", arguments.length);
	}		
	var layer = new OsmDataLayer(new DataSet(), name, null /* no file */); 
	exports.add(layer);
	return layer;
};
