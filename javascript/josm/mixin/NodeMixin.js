/**
 * <p>This module is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@josmclass org/openstreetmap/josm/data/osm/Node}.</p>
 * 
 * @module josm/mixin/NodeMixin 
 */
var util = require("josm/util");
var LatLon = org.openstreetmap.josm.data.coor.LatLon;

/**
 * <p>This mixin is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@josmclass org.openstreetmap.josm.data.osm.Node}. It
 * provides additional properties and methods which you can invoke on an instance of
 * {@josmclass org.openstreetmap.josm.data.osm.Node}.</p>
 *  
 * <p>The native methods of {@josmclass org.openstreetmap.josm.data.osm.Node} are still 
 * available for scripting. Just prefix their name with <code>$</code> if they are hidden
 * by properties or functions defined in this mixin.</p>
 * 
 * <p>NodeMixin inherits the properties and methods of [OsmPrimitiveMixin]{@link OsmPrimitiveMixin}.</p>
 *   
 * @mixin NodeMixin
 * @extends OsmPrimitiveMixin
 * @forClass org.openstreetmap.josm.data.osm.Node
 * @memberof josm/mixin/NodeMixin 
 */
var mixin = {};

/**
 * <p>Get or set the node latitude.</p>
 * 
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the current latitude, or undefined, if the latitude isn't known.</dd>
 *   
 *   <dt>set</dt>
 *   <dd>Assign the latitude. Expects a number in the range [-90,90]. Raises an error,
 *    if the node is a proxy node.</dd>
 * </dl>
 * 
 * @example
 * var nb = require("josm/builder").NodeBuilder;
 * 
 * // set members 
 * var n = nb.create();
 * n.lat = 23.245;
 * n.lat;  // -> 23.245
 * 
 * @memberOf NodeMixin
 * @name lat
 * @field
 * @instance
 * @type {number} 
 * @summary Get or set the node latitude.
 */
mixin.lat =  {
	get: function() {
		if (this.isIncomplete() || this.getCoor() == null) return undefined;
		return this.getCoor().$lat();
	},
	set: function(lat) {
		util.assert(! this.isIncomplete(), "Can''t set lat on an incomplete node");
		util.assert(util.isNumber(lat), "Expected a number, got {0}", lat);
		util.assert(LatLon.isValidLat(lat), "Expected a valid lat in the range [-90,90], got {0}", lat);
		var coor = this.getCoor();
		if (coor == null) coor = new LatLon(0,0);
		coor = new LatLon(lat, coor.$lon());
		this.setCoor(coor);
	}	
};

/**
 * <p>Get or set the node longitude.</p>
 * 
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the current longitude, or undefined, if the longitude isn't known.</dd>
 * 
 *   <dt>set</dt>
 *   <dd>Assign the longitude. Expects a number in the range [-180,180]. Raises an error,
 *    if the node is a proxy node.</dd>
 * </dl>
 * 
 * @example
 * var nb = require("josm/builder").NodeBuilder;
 * 
 * // set members 
 * var n = nb.create();
 * n.lon = -120.78;
 * n.lon;  // -> -120.78;
 * 
 * 
 * @memberOf NodeMixin
 * @name lon
 * @field
 * @instance
 * @type {number} 
 * @summary Get or set the node longitude.
 */
mixin.lon = {
	get: function() {
		if (this.isIncomplete() || this.getCoor() == null) return undefined;
		return this.getCoor().$lon();
	},
	set: function(lon) {
		util.assert(! this.isIncomplete(), "Can''t set lon on an incomplete node");
		util.assert(util.isNumber(lon), "Expected a number, got {0}", lon);
		util.assert(LatLon.isValidLon(lon), "Expected a valid lon in the range [-180,180], got {0}", lon);
		var coor = this.getCoor();
		if (coor == null) coor = new LatLon(0,0);
		coor = new LatLon(coor.$lon(), lon);
		this.setCoor(coor);
	}
};

/**
 * <p>Get the projected east coordinate, or undefined, if the projected east coordinate isn't known.</p>
 * 
 * @example
 * var nb = require("josm/builder").NodeBuilder;
 * 
 * // set members 
 * var n = nb.create();
 * n.east;  
 * 
 * @memberOf NodeMixin
 * @name east
 * @readOnly
 * @field
 * @instance
 * @type {number} 
 * @summary Get the projected east coordinate.
 */
mixin.east= {
	get: function() {
		if (this.isIncomplete() || this.getEastNorth() == null) return undefined;
		return this.getEastNorth().east();
	}
};

/**
 * <p>Get the projected north coordinate, or undefined, if the projected north coordinate isn't known.</p>
 * 
 * @example
 * var nb = require("josm/builder").NodeBuilder;
 * 
 * // set members 
 * var n = nb.create();
 * n.north;  
 * 
 * @memberOf NodeMixin
 * @name north
 * @readOnly
 * @field
 * @instance
 * @type {number} 
 * @summary Get the projected north coordinate.
 */
mixin.north = {
	get: function() {
		if (this.isIncomplete() || this.getEastNorth() == null) return undefined;
		return this.getEastNorth().north();
	}
};

/**
 * <p>Get or set the node position.</p>
 * 
 * <dl>
 *  <dt>get</dt>
 *  <dd>replies an instance of 
 *   {@josmclass org.openstreetmap.josm.data.coor.LatLon} or
 *   undefined, if the position isn't known.</dd>
 *   
 *  <dt>set</dt>
 *  <dd>Assign the position. Either an instance of  {@josmclass org.openstreetmap.josm.data.coor.LatLon}
 *   or an object with the properties <code>{lat: ..., lon: ...}</code></dd>
 * </dl>
 * 
 * @example
 * var LatLon = org.openstreetmap.josm.data.coor.LatLon;
 * var nb = require("josm/builder").NodeBuilder;
 * 
 * // assign a LatLon as position  
 * n.pos = new LatLon(23, 32.33);
 * 
 * // assign an object as position 
 * n.pos = {lat: 23, lon: 32.33};
 * 
 * // get the position
 * n.pos;  // -> a LatLon with the position 
 * 
 * @memberOf NodeMixin
 * @name pos
 * @field
 * @instance
 * @type org.openstreetmap.josm.data.coor.LatLon
 * @summary Get or set the node position.
 */
mixin.pos =  {
	get: function() {
		if (this.isIncomplete() || this.getCoor() == null) return undefined;
		return this.getCoor();
	},
	set: function(coor) {
		util.assert(util.isSomething(coor), "value must not be null or undefined");
		if (coor instanceof LatLon) {
			this.setCoor(coor);
		} else if (typeof coor === "object") {
			util.assert(coor.hasOwnProperty("lat"), "Missing mandatory property 'lat' in {0}", coor);
			util.assert(util.isNumber(coor.lat), "Expected a number in property 'lat' in {0}, got {1}", coor, coor.lat);
			util.assert(LatLon.isValidLat(coor.lat), "Illegal lat value for property 'lat' in {0}, got {1}", coor, coor.lat);
			util.assert(coor.hasOwnProperty("lon"), "Missing mandatory property 'lon' in {0}", coor);
			util.assert(util.isNumber(coor.lon), "Expected a number in property 'lon' in {0}, got {1}", coor, coor.lon);
			util.assert(LatLon.isValidLat(coor.lon), "Illegal lon value for property 'lon' in {0}, got {1}", coor, coor.lon);
			this.setCoor(new LatLon(coor.lat, coor.lon));
		} else {
			util.assert(false, "Unexpected type of value, got {0}", coor);
		}
	}
};

exports.forClass = org.openstreetmap.josm.data.osm.Node;
exports.mixin = util.mix(require("josm/mixin/OsmPrimitiveMixin").mixin, mixin);
