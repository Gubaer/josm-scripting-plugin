(function() {
	
/**
 * <p>Provides access to the JOSM layers.</p>
 * 
 * @module josm/layers
 */

//-- imports 
var Main = org.openstreetmap.josm.Main;
var OsmDataLayer = org.openstreetmap.josm.gui.layer.OsmDataLayer;	
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var Layer = org.openstreetmap.josm.gui.layer.Layer;
var util = require("josm/util");
	
/**
 * Replies the number of currently open layers. 
 * 
 * @name length
 * @memberOf josm/layers
 * @type {number}
 * @readOnly
 * @field
 * @summary Replies the number of currently open layers. 
 */ 
Object.defineProperty(exports, "length", {
	get: function() {
	    Main.getLayerManager().getLayers().size();
	}
});

/**
 * <p>Set or get the active layer.</p> 
 * 
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the active layer or undefined.</dd>
 *   
 *   <dt>set</dt>
 *   <dd>Assign either an existing {@class org.openstreetmap.josm.layer.Layer} , the name of a layer as string, 
 *   or a layer index as number.</dd>
 * </dl>
 * 
 * @name activeLayer
 * @memberOf josm/layers
 * @type {org.openstreetmap.josm.layer.Layer}
 * @field
 * @summary Set or get the active layer.
 */ 
Object.defineProperty(exports, "activeLayer", {
	get: function() {
	    return Main.getLayerManager().getActiveLayer();
	},
	set: function(value) {
		util.assert(util.isSomething(value), "Value must not be null or undefined)");
		var layer;
		if (value instanceof Layer) {
			layer = value; 
		} else if (util.isNumber(value) || util.isString(value)) {
			layer = exports.get(value);
		} else {
			util.assert(false, "Unexpected type of value, got {0}", value);
		}
		util.assert(util.isSomething(layer), "Layer ''{0}'' doesn''t exist. It can''t be set as active layer.", value);
		Main.getLayerManager().setActiveLayer(layer);
	}
});

function getLayerByName(key) {
	key = util.trim(key).toLowerCase();
	if (exports.length == 0) return undefined;
	var layers = Main.getLayerManager().getLayers();
	for(var it=layers.iterator(); it.hasNext();) {
		var l = it.next();
		if (l.getName().trim().toLowerCase().equals(key)) return l; 
	}
	return undefined;
}

function getLayerByIndex(idx) {
	if (idx < 0 || idx >= exports.length) return undefined;
	var layers = Main.getLayerManager().getLayers();
	return layers.get(idx);
}

/**
 * <p>Replies one of the layers given a key.</p>
 * <ul>
 *   <li>If <code>key</code> is a number, replies the layer with index key, or
 *   undefined, if no layer for this index exists.</li>
 *    <li>If <code>key</code> is a string, replies the first layer whose name is identical to key (
 *    case insensitive, without leading/trailing whitespace), or undefined, if no layer with such a name exists.</li>
 * </ul>
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
 * @param {number|string} key the key to retrieve the layer 
 * @memberof josm/layers
 * @name get
 * @method
 * @summary Replies one of the layers given a key.
 */
exports.get = function(key) {
	var undefined;
	if (util.isNothing(key)) return undefined;
	if (util.isString(key)) return getLayerByName(key);
	if (util.isNumber(key)) return getLayerByIndex(key);
	return undefined;
};

/**
 * <p>Checks whether <code>layer</code> is a currently registered layer.</p>
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
 * @memberof josm/layers
 * @method
 * @name has
 * @summary Checks whether <code>layer</code> is currently registered layer. 
 */
exports.has = function(layer) {
	if (util.isNothing(layer)) return false;
	var layerManager = Main.getLayerManager();
	if (layer instanceof Layer) {
		return layerManager.getLayers().contains(layer);
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
 * <p>Either pass in a layer object or a data set. In the later case, an 
 * {@class org.openstreetmap.josm.gui.layer.OsmDataLayer} is
 * automatically created.</p>
 * 
 * @example
 * var layers = require("josm/layers");
 * var OsmDataLayer = org.openstreetmap.josm.gui.layer.OsmDataLayer;	
 * var DataSet = org.openstreetmap.josm.data.osm.DataSet;
 *
 * var dataLayer = new OsmDataLayer(new DataSet(), null, null);
 * // add a layer ...
 * layers.add(dataLayer);
 * 
 * // or add a dataset, which will create a data layer
 * var ds = new DataSet();
 * layer.add(ds); 
 * 
 * @param {org.openstreetmap.josm.gui.layer.Layer|org.openstreetmap.josm.data.osm.DataSet} obj  a layer to add, or a dataset. 
 *  Ignored if null or undefined.  
 * @summary Adds a layer.
 * @memberof josm/layers
 * @method
 * @type org.openstreetmap.josm.gui.layer.Layer
 * @name add
 */
exports.add = function(obj) {
	util.println("obj: " + obj);
	if (util.isNothing(obj)) return;
	var layerManager = Main.getLayerManager();
	if (obj instanceof Layer) {
		layerManager.addLayer(obj);
	} else if (obj instanceof DataSet){
		layerManager.addLayer(new OsmDataLayer(obj, null, null));
	} else {
		util.println("wrong argument: " + obj);
		util.assert(false, "Expected an instance of Layer or DataSet, got {0}", obj);
	}	
};

var removeLayerByIndex = function(idx) {
	var layer = exports.get(idx);
	if (util.isNothing(layer)) return;
	Main.getLayerManager().removeLayer(layer);
};

var removeLayerByName = function(name) {
	var layer = exports.get(name);
	if (util.isNothing(layer)) return;
	Main.getLayerManager().removeLayer(layer);
};

/**
 * <p>Removes a layer with the given key.</p>
 * <ul>
 *   <li>If <code>key</code> is a <code>Number</code>, removes the layer with the index key. If the index doesn't
 *   isn't a valid layer index, nothing is removed.</li>
 *   <li>If <code>key</code> is a <code>string</code>, removes the layer with the name <code>key</code>. Leading 
 *   and trailing white space is removed, matching is a case-insensitive sub-string match.</li>
 * </ul>
 * @example
 * var layers = require("josm/layers");
 * 
 * // remove the first layer 
 * layers.remove(0);
 * 
 * // remove the first layer matching with the supplied name
 * layers.remove("myLayerName");   
 * 
 * @param {number|string} key  indicates the layer to remove 
 * @memberof josm/layers
 * @name remove
 * @method
 * @summary Removes a layer.
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
 * <string>Signatures</string>
 * <dl>
 *   <dt><code class="signature">addDataLayer()</code></dt>
 *   <dd>create data layer with a new dataset and default name</dd>
 *   <dt><code class="signature">addDataLayer(ds)</code></dt>
 *   <dd>create data layer with dataset ds and default name</dd>
 *   <dt><code class="signature">addDataLayer(name)</code></dt>
 *   <dd>create data layer with a new  dataset and name <code>name</code></dd>
 *   <dt><code class="signature">addDataLayer({name: ..., ds: ...})</code></dt>
 *   <dd>create data layer with a new  dataset and name <code>name</code></dd>
 * </dl>
 * @example
 * // creates a new data layer 
 * var layer = josm.layers.addDataLayer();
 * 
 * // creates a new data layer with name 'test'
 * layer = josm.layers.addDataLayer("test");
 * 
 * // creates a new data layer for the dataset ds
 * var ds = new DataSet();
 * layer = josm.layers.addDataLayer(ds);
 *
 * @name addDataLayer
 * @memberof josm/layers
 * @type org.openstreetmap.josm.gui.layer.OsmDataLayer
 * @method
 * @summary Adds a data layer
 */
exports.addDataLayer = function() {
	var name, ds;
	switch(arguments.length){
	case 0: break;		
	case 1: 
		if (util.isString(arguments[0])) {
			name = util.trim(arguments[0]);
		} else if (arguments[0] instanceof DataSet) {
			ds = arguments[0];
		} else if (typeof arguments[0] === "object") {
		    if (util.isString(arguments[0].name)) {
		    	name = util.trim(arguments[0].name);
		    }
		    if (arguments[0].ds instanceof DataSet) {
		    	ds = arguments[0].ds;		    	
		    }
		} else {
			util.assert(false, "unsupported type of argument, got {0}", arguments[0]);
		}
		break;
	default:
		util.assert(false, "Unsupported number of arguments, got {0}", arguments.length);
	}
	ds = ds || new DataSet();
	name = name ||  OsmDataLayer.createNewName();
	var layer = new OsmDataLayer(ds, name, null /* no file */); 
	exports.add(layer);
	return layer;
};

}());