goog.provide("josm.layers");
goog.require("josm");
goog.require("josm.util");

/**
 * <p>Provides access to the layers in JOSM.</p>
 * 
 * @namespace 
 * @name josm.layers
 */
josm.layers = function(my) {
	
	//-- imports 
	var Main = org.openstreetmap.josm.Main;
	var OsmDataLayer = org.openstreetmap.josm.gui.layer.OsmDataLayer;	
	var DataSet = org.openstreetmap.josm.data.osm.DataSet;
	var util = josm.util;
		
	var mv = function() {
		if (Main.map == null) return null;
		return Main.map.mapView;	
	};
	
	/**
	 * Replies the number of currently open layers. 
	 * 
	 * @name length
	 * @description 
	 * @memberOf josm.layers
	 * @type {Number}
	 */ 
	Object.defineProperty(my, "length", {
		get: function() {
			return mv() == null? 0 : mv().getNumLayers();
		}
	});
		
	var getLayerByIndex = function(idx) {
		if (mv() == null) return undefined;
		if (idx < 0 || idx >= my.length) return undefined;
		return mv().getAllLayersAsList().get(idx);		
	};
	
	var getLayerByName = function(name) {
		name = josm.util.trim(name).toLowerCase();
		if (mv() == null) return undefined;
		var layers = mv().getAllLayersAsList();
		for (var i=0; i< layers.size(); i++) {
			var l = layers.get(i);
			if (l.getName().equalsIgnoreCase(name)) return l;
		}
		return undefined;
	};
	
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
	 * // get the first layer  
	 * var layer  = josm.layers.get(0);
	 * 
	 * // get the first layer with name "data layer"
	 * var layer = josm.layers.get("data layer"); 
	 * 
	 * 
	 * @name get 
	 * @function
	 * @memberOf josm.layers
	 * @param key {Number | String}  the key to retrieve the layer 
	 */
	my.get = function(key) {
		if (key == null || key == undefined) return undefined;
		if (typeof key == "string") return getLayerByName(key);
		if (typeof key == "number") return getLayerByIndex(key);
		return undefined;
	};
	
	/**
	 * <p>Adds a layer.</p>
	 * 
	 * @example
	 * // remove the first layer 
	 * josm.layers.remove(0);
	 * 
	 * @name add 
	 * @function
	 * @memberOf josm.layers
	 * @param layer {org.openstreetmap.josm.gui.layer.Layer}  a layer to add. Ignored if null or undefined.
	 */
	my.add = function(layer) {
		if (util.isNothing(layer)) return;
		Main.main.addLayer(layer);
	};
	
	var removeLayerByIndex = function(idx) {
		var layer = my.get(idx);
		if (layer === undefined) return;
		mv().removeLayer(layer);
	};
	
	var removeLayerByName = function(name) {
		var layer = my.get(name);
		if (layer === undefined) return;
		mv().removeLayer(layer);
	};
	
	/**
	 * <p>Removes a layer with the given key.</p>
	 * <ul>
	 *   <li>If <code>key</code> is a <em>Number</em>, removes the layer with the index key. If the index doesn't
	 *   isn't a valid layer index, nothing is removed.</li>
	 *   <li>If <code>key</code> is a <em>String</em>, removes the layer with the name <code>key</code>. Leading 
	 *   and trailing white space is removed, matching is a case-insensitive sub-string match.</li>
	   
	 * <ul>
	 * 
	 * @example
	 * // remove the first layer 
	 * josm.layers.remove(0);
	 * 
	 * // remove the first layer matching with the supplied name
	 * josm.layers.remove("myLayerName");   
	 * 
	 * @name remove 
	 * @function
	 * @memberOf josm.layers
	 * @param key {Number | String} the key denoting the layer to remove 
	 */
	my.remove = function(key) {
		if (util.isNothing(key)) return;
		switch(typeof key) {
		case "number": 
			removeLayerByIndex(key);
			return;
		case "string":
			removeLayerByName(key);
			return;
		default:
			util.assert(false, "Expected a {Number} or a {String}, got {0}", key);
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
	 * @name addDataLayer 
	 * @function
	 * @memberOf josm.layers
	 * @param {String} name (optional) the name of the layer. If missing, null or undefined, automatically
	 * creates a name. 
	 * @return {org.openstreetmap.josm.gui.layer.OsmDataLayer}  the new data layer 
	 */
	my.addDataLayer = function() {
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
				name = String(name);
				name = util.trim(name);
			}
			break;
			
		 default:
			 util.assert(false, "Too many arguments. Expected 0 or 1, got {0}", arguments.length);
		}		
		var layer = new OsmDataLayer(new DataSet(), name, null /* no file */); 
		my.add(layer);
		return layer;
	};
		
	return my; 
}({});