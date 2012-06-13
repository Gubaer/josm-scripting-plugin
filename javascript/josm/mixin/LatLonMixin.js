/**
 * <p>This module is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@josmclass org.openstreetmap.josm.data.coor.LatLon}.</p>
 * 
 * @module josm/mixin/LatLonMixin
 */
var util = require("josm/util");
var LatLon = org.openstreetmap.josm.data.coor.LatLon;

/**
 * <p>This mixin is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@josmclass org.openstreetmap.josm.data.coor.LatLon}. It
 * provides additional properties and methods which you can invoke on an instance of
 * {@josmclass org.openstreetmap.josm.data.coor.LatLon}.</p>
 *  
 * <p>The native methods of {@josmclass org.openstreetmap.josm.data.coor.LatLon} are still 
 * available for scripting. Just prefix their name with <code>$</code> if they are hidden
 * by properties or functions defined in this mixin.</p>
 * 
 * @mixin LatLonMixin 
 * @forClass org.openstreetmap.josm.data.coor.LatLon
 * @memberof josm/mixin/LatLonMixin
 *
 */
var mixin = {};

/**
 * <p>Get the latitude.</p>
 * 
 * @example
 * var LatLon = org.openstreetmap.josm.data.coor.LatLon;
 * 
 * // set members 
 * var pos = new LatLon(1,2);
 * pos.lat;     // -> 1
 * 
 * // you can still invoke the native java method lat(), just add $ as prefix.
 * // Note the parentheses - the native method has to be called as function!
 * pos.$lat();  // -> 1
 * 
 * @memberOf LatLonMixin
 * @name lat
 * @field
 * @readOnly
 * @instance
 * @type {number} 
 * @summary Get the latitude.
 */
mixin.lat = {
	get: function() {
		return this.$lat();
	}	
};

/**
 * <p>Get the longitude.</p>
 * 
 * @example
 * var LatLon = org.openstreetmap.josm.data.coor.LatLon;
 * 
 * // set members 
 * var pos = new LatLon(1,2);
 * pos.lon;     // -> 2
 * 
 * // you can still invoke the native java method lon(), just add $ as prefix.
 * // Note the parentheses - the native method has to be called as function!
 * pos.$lon();  // -> 1
 * 
 * @memberOf LatLonMixin
 * @name lon
 * @field
 * @readOnly
 * @instance
 * @type {number} 
 * @summary Get the longitude.
 */
mixin.lon = {
	get: function() {
		return this.$lon();
	}	
};


/**
 * <p>Creates a {@josmclass org.openstreetmap.josm.data.coor.LatLon} from a javascript object.</p>
 * 
 * @example
 * var pos = LatLon.make({lat: 1, lon: 2});
 * 
 * @param {object} obj  a javascript object with two number properties <code>lat:</code> and 
 *   <code>lon:</code> 
 * @memberOf LatLonMixin
 * @name make
 * @static
 * @method
 * @type org.openstreetmap.josm.data.coor.LatLon 
 * @summary Create a {@josmclass org.openstreetmap.josm.data.coor.LatLon} from a javascript object.
 */
mixin.make = function(obj) {
	util.assert(util.isSomething(obj), "obj: must not be null or undefined");
	util.assert(typeof obj === "object", "obj: expected an object, got {0}", obj);
	util.assert(util.isNumber(obj.lat), "obj.lat: expected a number, got {0}", obj.lat);
	util.assert(util.isNumber(obj.lon), "obj.lon: expected a number, got {0}", obj.lon);	
	util.assert(LatLon.isValidLat(obj.lat), "obj.lat: expected a valid lat in the range [-90,90], got {0}", obj.lat);
	util.assert(LatLon.isValidLon(obj.lon), "obj.lon: expected a valid lon in the range [-180,180], got {0}", obj.lon);
	return new LatLon(obj.lat, obj.lon);
};
mixin.make.static=true;

exports.forClass = org.openstreetmap.josm.data.coor.LatLon;
exports.mixin    = mixin;